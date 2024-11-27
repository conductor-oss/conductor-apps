package io.orkes.demo.service;

import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.RateLimitConfig;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.SearchResult;
import com.netflix.conductor.common.run.WorkflowSummary;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesTaskClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final OrkesWorkflowClient workflowClient;
    private final OrkesMetadataClient metadataClient;
    private final OrkesTaskClient taskClient;

    private final String workflowName = "rate_limit_workflow";

    public void triggerDynamicRateLimitWorkflow() {
        log.info("Triggering dynamic rate limit workflow");
        terminateExistingRunningWorkflows();

        WorkflowDef workflowDef = getWorkflowDef(false);
        metadataClient.registerWorkflowDef(workflowDef, true);

        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(1);

        startWorkflowRequest.setCorrelationId("correlationId1");
        List<String> workflowIds1 = List.of(
            workflowClient.startWorkflow(startWorkflowRequest),
            workflowClient.startWorkflow(startWorkflowRequest),
            workflowClient.startWorkflow(startWorkflowRequest)
        );
        workflowIds1.forEach(this::logRunningTasks);

        startWorkflowRequest.setCorrelationId("correlationId2");
        List<String> workflowIds2 = List.of(
            workflowClient.startWorkflow(startWorkflowRequest),
            workflowClient.startWorkflow(startWorkflowRequest),
            workflowClient.startWorkflow(startWorkflowRequest)
        );
        workflowIds2.forEach(this::logRunningTasks);

        // Complete the task so that next workflow tasks will start running.
        log.info("Completing task for Workflow 1: {}", workflowIds1.get(0));
        completeFirstTask(workflowIds1.get(0));
        log.info("Workflow: {} has {} running tasks", workflowIds1.get(2), getRunningTaskCount(workflowIds1.get(2)));

        log.info("Completing task for Workflow 3: {}", workflowIds2.get(0));
        completeFirstTask(workflowIds2.get(0));
        log.info("Workflow: {} has {} running tasks", workflowIds2.get(2), getRunningTaskCount(workflowIds2.get(2)));
    }

    public void triggerStaticRateLimitWorkflow() {
        log.info("Triggering static rate limit workflow");
        terminateExistingRunningWorkflows();

        WorkflowDef workflowDef = getWorkflowDef(true);
        metadataClient.registerWorkflowDef(workflowDef, true);
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(1);

        List<String> workflowIds = List.of(
            workflowClient.startWorkflow(startWorkflowRequest),
            workflowClient.startWorkflow(startWorkflowRequest),
            workflowClient.startWorkflow(startWorkflowRequest)
        );

        // Only two instance of the workflow will have tasks in running status.
        workflowIds.forEach(this::logRunningTasks);

        // Complete Workflow 1 so that Workflow 3 can start
        log.info("Completing task for Workflow 1: {}", workflowIds.get(0));
        completeFirstTask(workflowIds.get(0));

        log.info("Workflow 3: {} has {} running tasks", workflowIds.get(2), getRunningTaskCount(workflowIds.get(2)));
    }

    private WorkflowDef getWorkflowDef(boolean staticRateLimit) {
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("rate_limit_task");
        workflowTask.setTaskReferenceName("rate_limit_task");
        workflowTask.setType(TaskType.TASK_TYPE_SIMPLE);

        String rateLimitKey = staticRateLimit ? "static_rate_limit_key" : "${workflow.correlationId}";
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setConcurrentExecLimit(2);
        rateLimitConfig.setRateLimitKey(rateLimitKey);

        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName(workflowName);
        workflowDef.setVersion(1);
        workflowDef.setTasks(List.of(workflowTask));
        workflowDef.setRateLimitConfig(rateLimitConfig);

        return workflowDef;
    }

    private void completeFirstTask(String workflowId) {
        String taskId = workflowClient.getWorkflow(workflowId, true).getTasks().get(0).getTaskId();

        TaskResult taskResult = new TaskResult();
        taskResult.setStatus(TaskResult.Status.COMPLETED);
        taskResult.setTaskId(taskId);
        taskResult.setWorkflowInstanceId(workflowId);
        taskClient.updateTask(taskResult);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("Error sleeping", e);
        }
    }

    private void terminateExistingRunningWorkflows() {
        try {
            SearchResult<WorkflowSummary> found = workflowClient.search("workflowType IN (" + workflowName + ") AND status IN (RUNNING)");
            log.info("Found {} running workflows to be cleaned up", found.getResults().size());
            found.getResults().forEach(workflowSummary -> {
                log.info("Going to terminate {} with status {}", workflowSummary.getWorkflowId(), workflowSummary.getStatus());
                workflowClient.terminateWorkflow(workflowSummary.getWorkflowId(), "terminate");
            });
        } catch (Exception e) {
            log.error("Error cleaning up workflows", e);
        }
    }

    private int getRunningTaskCount(String workflowId) {
        return workflowClient.getWorkflow(workflowId, true).getTasks().size();
    }

    private void logRunningTasks(String workflowId) {
        log.info("Workflow: {} started with {} running tasks", workflowId, getRunningTaskCount(workflowId));
    }
}
