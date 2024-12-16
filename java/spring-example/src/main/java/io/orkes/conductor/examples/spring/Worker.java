package io.orkes.conductor.examples.spring;

import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Worker {
    @WorkerTask(value = "hello-world", threadCount = 5, pollingInterval = 200)
    public String hello(Map<String, Object> input) {
        return "Hello " + input.get("name");
    }
}
