package io.orkes.conductor.logic;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;

import static org.awaitility.Awaitility.await;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import io.orkes.conductor.util.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.netflix.conductor.common.run.Workflow.WorkflowStatus.TIMED_OUT;
import static io.orkes.conductor.util.Commons.GetUserTask;
import static io.orkes.conductor.util.Commons.GetWorkflowDef;

/**
 * Configures the global workflow-level timeout behavior under the TIME_OUT_WF timeout mode.
 *
 * Example usage:
 *  - A global workflow timeout of **5 seconds** is set, meaning the workflow will automatically time out if it doesn't complete within that time.
 *  - The workflow will be marked as **TIMED_OUT** after the defined time limit
 */
public class WorkflowGlobalTimeout {

    private static final Logger log = LoggerFactory.getLogger(WorkflowGlobalTimeout.class);

    public static void run()  {
        //Initialise Conductor Client
        var apiClient = ClientUtil.getClient();

        OrkesWorkflowClient workflowAdminClient = new OrkesWorkflowClient(apiClient);
        OrkesMetadataClient metadataAdminClient = new OrkesMetadataClient(apiClient);

        // GET_USER_INFO
        TaskDef userTaskDef = new TaskDef("get_user_info");
        userTaskDef.setOwnerEmail("test@orkes.io");
        WorkflowTask userTask = GetUserTask(userTaskDef);


        WorkflowDef workflowDef = GetWorkflowDef();
        log.info("Setting Global timeout to 5 second, timeout workflow policy");
        workflowDef.setTimeoutSeconds(5);
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
