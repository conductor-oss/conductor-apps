package io.orkes.conductor.examples;

import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import io.orkes.conductor.client.ApiClient;

public class Main {

    public static void main(String[] args) {
        var client = ApiClient.builder()
                // Sign up on https://developer.orkescloud.com and create an application.
                // Use your application key id and key secret
                .basePath("https://developer.orkescloud.com/api")
                .credentials("_CHANGE_ME_", "_CHANGE_ME_")
                .build();
        int pollingInterval = 50;
        var executor = new WorkflowExecutor(client, pollingInterval);
        // List of packages  (comma separated) to scan for annotated workers.
        // Please note, the worker method MUST be public and the class in which they are defined
        // MUST have a no-args constructor
        executor.initWorkers("io.orkes.conductor.examples.workers");
    }
}
