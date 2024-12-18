package io.orkes.conductor.logic;

import com.netflix.conductor.common.metadata.tasks.Task;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.netflix.conductor.common.run.Workflow.WorkflowStatus.TIMED_OUT;
import static org.awaitility.Awaitility.await;
import static io.orkes.conductor.util.Commons.GetUserTask;
import static io.orkes.conductor.util.Commons.GetWorkflowDef;

/**
 * Configures the task-level timeout and retry behavior for a long-running worker.
 * This configuration is useful for tasks that are expected to take a long time to complete, but may occasionally experience failures or timeouts due to external conditions. The retry mechanism ensures that the task has a chance to succeed if it fails within the retry window.
 *
 * Example usage:
 *  - Task timeout is set to **4 seconds**.
 *  - If the task times out or fails, it will be retried with a **2-second delay** between each retry attempt.
 *
 * @throws ExecutionException
 * @throws InterruptedException
 * @throws TimeoutException
 */
public class WorkflowTaskTimeoutAfterWorkerPickupWithRetries {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTaskTimeoutAfterWorkerPickupWithRetries.class);

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
        userTaskDef.setRetryCount(1);
        userTaskDef.setResponseTimeoutSeconds(3);
        userTaskDef.setTimeoutPolicy(TaskDef.TimeoutPolicy.RETRY);
        userTaskDef.setRetryDelaySeconds(2);

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

        await().atMost(200, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until(() ->
        {
            Workflow workflow = workflowAdminClient.getWorkflow(workflowId, true);
            if(workflow.getStatus() == TIMED_OUT) {
                List<Task> taskList = workflow.getTasks();
                if(taskList.size()==2){
                    Task failedTask = workflow.getTasks().get(0);
                    Task retriedTask = workflow.getTasks().get(1);
                    log.info("Original task ID: {}", failedTask.getTaskId());
                    log.info("Retried task ID: {}", retriedTask.getRetriedTaskId());
                    log.info("Retry count: {}", retriedTask.getRetryCount());
                    log.info("Task timed out and would be retried: {}", failedTask.getReasonForIncompletion());
                }
                return true;
            }
            return false;
        });
    }
}
