package io.orkes.conductor.logic;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
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
 * Timeout: The maximum duration in seconds for the task to reach a terminal state before it gets marked as TIMED_OUT
 * Configures the task-level timeout for a long-running worker.
 * Example usage:
 * - Task timeout is set to **4 seconds** to allow long-running tasks to complete without triggering a timeout.
 * - Introduction of **35 seconds** delay under the get_user_info worker
 * - The task is allowed to **time out** after the configured duration if the worker takes too long to complete.
 *
 * @throws ExecutionException
 * @throws InterruptedException
 * @throws TimeoutException
 */
public class WorkflowTaskTimeoutAfterWorkerPickup {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTaskTimeoutAfterWorkerPickup.class);

    public static void run() {
        //Initialise Conductor Client
        var apiClient = ClientUtil.getClient();
        OrkesWorkflowClient workflowAdminClient = new OrkesWorkflowClient(apiClient);
        OrkesMetadataClient metadataAdminClient = new OrkesMetadataClient(apiClient);

        //Initialise WorkflowExecutor and Conductor Workers
        var workflowExecutor = new WorkflowExecutor(apiClient, 10);
        workflowExecutor.initWorkers("io.orkes.conductor.workflow");


        // GET_USER_INFO
        TaskDef userTaskDef = new TaskDef("get_user_info");
        userTaskDef.setOwnerEmail("test@orkes.io");

        // TIMEOUT CONFIG
        userTaskDef.setTimeoutSeconds(4);
        userTaskDef.setRetryCount(0);
        userTaskDef.setResponseTimeoutSeconds(3);
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
