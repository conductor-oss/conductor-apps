package org.demo.workflow;

import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;

public class ConductorWorkers {

    @WorkerTask("get_user_info")
    public WorkflowOutput get_user_info(@InputParam("userId") String userId) throws InterruptedException {
        Thread.sleep(35000);
        return new WorkflowOutput("999999999", userId + "@example.com");
    }

    @WorkerTask("send_email")
    public String send_email(@InputParam("email") String name) throws InterruptedException {
        return "EMAIL: " + name;
    }

    @WorkerTask("send_sms")
    public String send_sms(@InputParam("phoneNumber") String phoneNumber) throws InterruptedException {
        return "SMS: " + phoneNumber;
    }

}
