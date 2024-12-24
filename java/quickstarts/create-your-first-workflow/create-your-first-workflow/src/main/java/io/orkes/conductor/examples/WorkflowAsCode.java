package io.orkes.conductor.examples;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.Http;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Switch;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import io.orkes.conductor.client.ApiClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WorkflowAsCode {

    public static void main(String[] args) {
        // Sign up on https://developer.orkescloud.com and create an application.
        // Use your application key id and key secret
        ApiClient client = ApiClient.builder()
                .basePath("https://developer.orkescloud.com/api")
                .credentials("_CHANGE_ME_", "_CHANGE_ME_")
                .build();
        int pollingInterval = 50;
        WorkflowExecutor executor = new WorkflowExecutor(client, pollingInterval);

        ConductorWorkflow<Object> workflow = new WorkflowBuilder<>(executor)
                .name("myFirstWorkflow")
                .version(1)
                .description("Workflow that greets a user. Uses a Switch task, HTTP task, and Simple task.")
                .add(new Http("get-user_ref").url("https://randomuser.me/api/"))
                // This switch task will execute the "helloWorld" task if the user's country is "United States"
                .add(new Switch("user-criteria_ref", "${get-user_ref.output.response.body.results[0].location.country}")
                        .switchCase("United States", new SimpleTask("helloWorld", "simple_ref")
                                .input("user", "${get-user_ref.output.response.body.results[0].name.first}")))
                .build();

        //1. Register the workflow with overwrite = true and registerTasks = true
        workflow.registerWorkflow(true, true);

        //2. Start the workflow
        String id = executor.startWorkflow(workflow.getName(), workflow.getVersion(), Map.of());
        System.out.printf("Started workflow %s%n", id);
        // Alternatively, call execute which will return once the workflow reaches a terminal state
        // (COMPLETED, FAILED or TERMINATED)
        // syncExecution(workflow);

        executor.shutdown();
    }

    private static void syncExecution(ConductorWorkflow<Object> workflow) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Workflow> execution = workflow.execute(Map.of());
        Workflow workflowExecution = execution.get(1, TimeUnit.MINUTES);
        Workflow.WorkflowStatus status = workflowExecution.getStatus();
        System.out.printf("Workflow: %s, status: %s%n", workflowExecution.getWorkflowId(), status);
    }

}
