package io.orkes.conductor.logic;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import io.orkes.conductor.util.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.netflix.conductor.common.run.Workflow.WorkflowStatus.TIMED_OUT;
import static org.awaitility.Awaitility.await;
import static io.orkes.conductor.util.Commons.GetUserTask;
import static io.orkes.conductor.util.Commons.GetWorkflowDef;

/**
 * Poll Timeout: The maximum duration in seconds that a worker has to poll a task before it gets marked as TIMED_OUT.
 * The polling timeout occurs when a task is unable to find an available worker within the defined pool size or time limit.
 *  Example scenario:
 *  - If workers are not available or unable to start, tasks in the queue will wait for workers to become available.
 *  - If the timeout threshold is exceeded without any workers being able to execute the tasks, a pooling timeout occurs.
 *
 * @throws ExecutionException
 * @throws InterruptedException
 * @throws TimeoutException
 */
public class WorkflowTaskTimeoutBeforeWorkerPickup {

   private static final Logger log = LoggerFactory.getLogger(WorkflowTaskTimeoutBeforeWorkerPickup.class);

   public static void run() {
        //Initialise Conductor Client
        var apiClient = ClientUtil.getClient();
        OrkesWorkflowClient workflowAdminClient = new OrkesWorkflowClient(apiClient);
        OrkesMetadataClient metadataAdminClient = new OrkesMetadataClient(apiClient);

        // GET_USER_INFO
        TaskDef userTaskDef = new TaskDef("get_user_info");
        userTaskDef.setOwnerEmail("test@orkes.io");

        // TIMEOUT CONFIG
        userTaskDef.setPollTimeoutSeconds(6);
        userTaskDef.setRetryCount(0);
        userTaskDef.setTimeoutPolicy(TaskDef.TimeoutPolicy.TIME_OUT_WF);

        WorkflowTask userTask = GetUserTask(userTaskDef);

        WorkflowDef workflowDef = GetWorkflowDef();
        workflowDef.setTimeoutPolicy(WorkflowDef.TimeoutPolicy.TIME_OUT_WF);
        workflowDef.setTasks(Arrays.asList(userTask));
        metadataAdminClient.registerWorkflowDef(workflowDef);
        metadataAdminClient.registerTaskDefs(Arrays.asList(userTaskDef));

        // Start the created workflow
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName("user_notification");
        startWorkflowRequest.setVersion(1);

        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("userId", "userA");

        startWorkflowRequest.setInput(inputParams);
        String workflowId = workflowAdminClient.startWorkflow(startWorkflowRequest);
        log.info("Started: {}", workflowId);

        await().atMost(35, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until(() ->
        {
           Workflow workflow = workflowAdminClient.getWorkflow(workflowId, true);
           if(workflow.getStatus() == TIMED_OUT) {
              log.info("Workflow timed out: {}", workflow.getReasonForIncompletion());
              return true;
           }
           return false;
        });
    }
}
