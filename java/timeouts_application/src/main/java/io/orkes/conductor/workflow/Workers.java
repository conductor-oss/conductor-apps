package io.orkes.conductor.workflow;

import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;

public class Workers {

    @WorkerTask("get_user_info")
    public WorkflowOutput get_user_info(@InputParam("userId") String userId) throws InterruptedException {
        Thread.sleep(35000);
        return new WorkflowOutput("999999999", userId + "@example.com");
    }

}
