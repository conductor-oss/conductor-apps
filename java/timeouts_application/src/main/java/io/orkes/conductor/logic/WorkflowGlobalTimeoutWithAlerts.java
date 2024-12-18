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
import java.util.concurrent.TimeUnit;

import static com.netflix.conductor.common.run.Workflow.WorkflowStatus.*;
import static org.awaitility.Awaitility.await;
import static io.orkes.conductor.util.Commons.GetUserTask;
import static io.orkes.conductor.util.Commons.GetWorkflowDef;

/**
 * Configures the global workflow-level timeout behavior under the ALERT_ONLY timeout mode.
 *
 * Example usage:
 *  - A global workflow timeout of **3 seconds** is set, and when the timeout occurs, operators are alerted, but the workflow continues without being marked as failed.
 *  - The workflow or task is not marked as **failed**, and the system can take appropriate action based on the alert (e.g., retry, monitor, or log the event)
 */
public class WorkflowGlobalTimeoutWithAlerts {

    private static final Logger log = LoggerFactory.getLogger(WorkflowGlobalTimeoutWithAlerts.class);

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
        WorkflowTask userTask = GetUserTask(userTaskDef);


        WorkflowDef workflowDef = GetWorkflowDef();
        workflowDef.setTimeoutSeconds(3);
        workflowDef.setTimeoutPolicy(WorkflowDef.TimeoutPolicy.ALERT_ONLY);
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

        await().atMost(200, TimeUnit.SECONDS).pollInterval(5000, TimeUnit.MILLISECONDS).until(() ->
        {
            Workflow workflow = workflowAdminClient.getWorkflow(workflowId, true);
            if(workflow.getStatus() == TIMED_OUT) {
                log.info("Workflow timed out: {}", workflow.getReasonForIncompletion());
                return true;
            }

            if(workflow.getStatus() == COMPLETED) {
                log.info("Workflow Completed");
                return true;
            }
            return false;
        });
    }


}
