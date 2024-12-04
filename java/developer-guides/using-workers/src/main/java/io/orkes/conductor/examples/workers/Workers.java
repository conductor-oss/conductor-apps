package io.orkes.conductor.examples.workers;

import com.netflix.conductor.sdk.workflow.task.InputParam;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;

public class Workers {
    @WorkerTask("myTask")
    public String greeting(@InputParam("name") String name) {
        return ("Hello " + name);
    }
}
