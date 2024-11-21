package io.orkes.demo.banking.service;

import com.google.common.util.concurrent.Uninterruptibles;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.RateLimitConfig;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.SearchResult;
import com.netflix.conductor.common.run.WorkflowSummary;
import io.orkes.conductor.client.MetadataClient;
import io.orkes.conductor.client.TaskClient;
import io.orkes.conductor.client.WorkflowClient;
import io.orkes.conductor.client.http.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Service
public class WorkflowService {

    private final WorkflowClient workflowClient;

    private final MetadataClient metadataClient;

    private final TaskClient taskClient;

    private final String workflowName = "rate_limit_workflow";

    public void triggerDynamicRateLimitWorkflow() {
        terminateExistingRunningWorkflows(workflowName);
        WorkflowDef workflowDef = getWorkflowDef(true);
        metadataClient.registerWorkflowDef(workflowDef, true);
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(1);
        startWorkflowRequest.setCorrelationId("correlationId1");
        String workflowId1 = workflowClient.startWorkflow(startWorkflowRequest);
        String workflowId2 = workflowClient.startWorkflow(startWorkflowRequest);

        startWorkflowRequest.setCorrelationId("correlationId2");
        String workflowId3 = workflowClient.startWorkflow(startWorkflowRequest);
        String workflowId4 = workflowClient.startWorkflow(startWorkflowRequest);

        // Only 1 workflow with correlationId1 and correlationId2 will have tasks in running status.
        log.info("Workflow1: {} started with {} running tasks", workflowId1, workflowClient.getWorkflow(workflowId1, true).getTasks().size());
        log.info("Workflow2: {} started with {} running tasks", workflowId2, workflowClient.getWorkflow(workflowId2, true).getTasks().size());
        log.info("Workflow3: {} started with {} running tasks", workflowId3, workflowClient.getWorkflow(workflowId3, true).getTasks().size());
        log.info("Workflow4: {} started with {} running tasks", workflowId4, workflowClient.getWorkflow(workflowId4, true).getTasks().size());

        // Complete the task so that next workflow tasks will start running.
        log.info("Completing task for workflow1: {}", workflowId1);
        completeTask(workflowId1, workflowClient.getWorkflow(workflowId1, true).getTasks().get(0).getTaskId());
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        log.info("Workflow3: {} has {} running tasks", workflowId2, workflowClient.getWorkflow(workflowId2, true).getTasks().size());

        log.info("Completing task for workflow3: {}", workflowId3);
        completeTask(workflowId3, workflowClient.getWorkflow(workflowId3, true).getTasks().get(0).getTaskId());
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        log.info("Workflow4: {} has {} running tasks", workflowId4, workflowClient.getWorkflow(workflowId4, true).getTasks().size());
    }

    private WorkflowDef getWorkflowDef(boolean staticRateLimit) {
        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName("rate_limit_workflow");
        workflowDef.setVersion(1);
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("rate_limit_task");
        workflowTask.setTaskReferenceName("rate_limit_task");
        workflowTask.setType(TaskType.TASK_TYPE_SIMPLE);
        workflowDef.setTasks(List.of(workflowTask));
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setConcurrentExecLimit(2);
        if (staticRateLimit) {
            rateLimitConfig.setRateLimitKey("static_rate_limit_key");
        } else {
            rateLimitConfig.setRateLimitKey("${workflow.correlationId}");
        }
        workflowDef.setRateLimitConfig(rateLimitConfig);
        return workflowDef;
    }

    private void completeTask(String workflowId, String taskId) {
        TaskResult taskResult = new TaskResult();
        taskResult.setStatus(TaskResult.Status.COMPLETED);
        taskResult.setTaskId(taskId);
        taskResult.setWorkflowInstanceId(workflowId);
        taskClient.updateTask(taskResult);
    }

    public void triggerStaticRateLimitWorkflow() {
        terminateExistingRunningWorkflows(workflowName);
        WorkflowDef workflowDef = getWorkflowDef(true);
        metadataClient.registerWorkflowDef(workflowDef, false);
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(1);
        String workflowId1 = workflowClient.startWorkflow(startWorkflowRequest);
        String workflowId2 = workflowClient.startWorkflow(startWorkflowRequest);
        String workflowId3 = workflowClient.startWorkflow(startWorkflowRequest);

        //Only two instance of the workflow will have tasks in running status.
        log.info("Workflow1: {} started with {} running tasks", workflowId1, workflowClient.getWorkflow(workflowId1, true).getTasks().size());
        log.info("Workflow2: {} started with {} running tasks", workflowId2, workflowClient.getWorkflow(workflowId2, true).getTasks().size());
        log.info("Workflow3: {} started with {} running tasks", workflowId3, workflowClient.getWorkflow(workflowId3, true).getTasks().size());

        // Complete workflow1 so that workflow3 can start
        log.info("Completing task for workflow1: {}", workflowId1);
        completeTask(workflowId1, workflowClient.getWorkflow(workflowId1, true).getTasks().get(0).getTaskId());
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        log.info("Workflow3: {} has {} running tasks", workflowId3, workflowClient.getWorkflow(workflowId3, true).getTasks().size());
    }
    private void terminateExistingRunningWorkflows(String workflowName) {
        //clean up first
        try {
            SearchResult<WorkflowSummary> found = workflowClient.search("workflowType IN (" + workflowName + ") AND status IN (RUNNING)");
            System.out.println("Found " + found.getResults().size() + " running workflows to be cleaned up");
            found.getResults().forEach(workflowSummary -> {
                System.out.println("Going to terminate " + workflowSummary.getWorkflowId() + " with status " + workflowSummary.getStatus());
                workflowClient.terminateWorkflow(workflowSummary.getWorkflowId(), "terminate");
            });
        } catch(Exception e){
            if (!(e instanceof ApiException)) {
                throw e;
            }
        }
    }
}
