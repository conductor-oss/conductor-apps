package org.demo.logic;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import org.demo.util.ClientUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.netflix.conductor.common.run.Workflow.WorkflowStatus.TIMED_OUT;

public class WorkflowTaskTimeoutAfterWorkerPickup {

    /**
     * Setting a task level timeout and a worker having a high run time, thereby timing out ultimately
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static void run() throws ExecutionException, InterruptedException, TimeoutException {
        //Initialise Conductor Client
        var apiClient = ClientUtil.getClient();
        OrkesWorkflowClient workflowAdminClient = new OrkesWorkflowClient(apiClient);
        OrkesMetadataClient metadataAdminClient = new OrkesMetadataClient(apiClient);

        //Initialise WorkflowExecutor and Conductor Workers
        var workflowExecutor = new WorkflowExecutor(apiClient, 10);
        workflowExecutor.initWorkers("org.demo.workflow");


        // GET_USER_INFO
        TaskDef userTaskDef = new TaskDef("get_user_info");
        userTaskDef.setOwnerEmail("test@orkes.io");

        // TIMEOUT CONFIG
        userTaskDef.setTimeoutSeconds(4);
        userTaskDef.setRetryCount(0);
        userTaskDef.setResponseTimeoutSeconds(3);
        userTaskDef.setTimeoutPolicy(TaskDef.TimeoutPolicy.TIME_OUT_WF);

        WorkflowTask userTask = new WorkflowTask();
        userTask.setTaskReferenceName("get_user_info");
        userTask.setName("get_user_info");
        userTask.setTaskDefinition(userTaskDef);

        userTask.setWorkflowTaskType(TaskType.SIMPLE);
        userTask.setInputParameters(Map.of("userId", "${workflow.input.userId}"));


        // EMAIL
        TaskDef emailTaskDef = new TaskDef("send_email");
        emailTaskDef.setOwnerEmail("test@orkes.io");
        WorkflowTask emailTask = new WorkflowTask();
        emailTask.setTaskReferenceName("send_email");
        emailTask.setName("send_email");
        emailTask.setTaskDefinition(emailTaskDef);

        emailTask.setWorkflowTaskType(TaskType.SIMPLE);
        emailTask.setInputParameters(Map.of("email", "${get_user_info.output.email}"));


        // SMS
        TaskDef smsTaskDef = new TaskDef("send_sms");
        smsTaskDef.setOwnerEmail("test@orkes.io");
        WorkflowTask smsTask = new WorkflowTask();
        smsTask.setTaskReferenceName("send_sms");
        smsTask.setName("send_sms");
        smsTask.setTaskDefinition(smsTaskDef);

        smsTask.setWorkflowTaskType(TaskType.SIMPLE);
        smsTask.setInputParameters(Map.of("phoneNumber", "${get_user_info.output.phoneNumber}"));

        // SWITCH
        TaskDef switchTaskDef = new TaskDef("emailorsms");
        smsTaskDef.setOwnerEmail("test@orkes.io");
        WorkflowTask switchTask = new WorkflowTask();
        switchTask.setTaskReferenceName("emailorsms");
        switchTask.setName("emailorsms");
        switchTask.setTaskDefinition(switchTaskDef);

        switchTask.setWorkflowTaskType(TaskType.SWITCH);
        switchTask.setEvaluatorType("value-param");
        switchTask.setExpression("switchCaseValue");
        switchTask.setDecisionCases(Map.of("SMS", List.of(smsTask), "EMAIL", List.of(emailTask)));
        switchTask.setInputParameters(Map.of("switchCaseValue", "${workflow.input.notificationPreference}"));


        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName("user_notification");
        workflowDef.setOwnerEmail("test@orkes.io");
        workflowDef.setTimeoutPolicy(WorkflowDef.TimeoutPolicy.TIME_OUT_WF);
        workflowDef.setInputParameters(Arrays.asList("value", "inlineValue"));
        workflowDef.setDescription("Workflow to send notification to user");
        workflowDef.setTasks(Arrays.asList(userTask, switchTask));
        metadataAdminClient.registerWorkflowDef(workflowDef);
        metadataAdminClient.registerTaskDefs(Arrays.asList(userTaskDef, switchTaskDef));


        // Start the created workflow
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName("user_notification");
        startWorkflowRequest.setVersion(1);

        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("userId", "userA");
        inputParams.put("notificationPreference", "SMS");

        startWorkflowRequest.setInput(inputParams);
        String workflowId = workflowAdminClient.startWorkflow(startWorkflowRequest);
        System.out.println("Started: "+ workflowId);

        while(true){
            Workflow workflow = workflowAdminClient.getWorkflow(workflowId, true);
            if(workflow.getStatus() == TIMED_OUT) {
                System.out.println("Workflow timed out: "+ workflow.getReasonForIncompletion());
                break;
            }
            Thread.sleep(5000);
        }
    }

}
