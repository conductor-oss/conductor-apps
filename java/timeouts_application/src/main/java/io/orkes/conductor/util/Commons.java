package io.orkes.conductor.util;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.Arrays;
import java.util.Map;

public class Commons {
    public static WorkflowTask GetUserTask(TaskDef userTaskDef){
        WorkflowTask userTask = new WorkflowTask();
        userTask.setTaskReferenceName("get_user_info");
        userTask.setName("get_user_info");
        userTask.setTaskDefinition(userTaskDef);

        userTask.setWorkflowTaskType(TaskType.SIMPLE);
        userTask.setInputParameters(Map.of("userId", "${workflow.input.userId}"));
        return userTask;
    }

    public static WorkflowDef GetWorkflowDef(){
        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName("user_notification");
        workflowDef.setOwnerEmail("test@orkes.io");
        workflowDef.setInputParameters(Arrays.asList("value", "inlineValue"));
        workflowDef.setDescription("Workflow to send notification to user");

        return workflowDef;
    }
}
