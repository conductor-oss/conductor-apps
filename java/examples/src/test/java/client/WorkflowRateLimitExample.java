/*
 * Copyright 2024 Orkes, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package client;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Uninterruptibles;
import com.netflix.conductor.common.metadata.workflow.RateLimitConfig;
import com.netflix.conductor.common.run.SearchResult;
import com.netflix.conductor.common.run.WorkflowSummary;
import io.orkes.conductor.client.MetadataClient;
import io.orkes.conductor.client.OrkesClients;
import io.orkes.conductor.client.TaskClient;
import io.orkes.conductor.client.WorkflowClient;
import io.orkes.conductor.client.http.ApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowRateLimitExample {
    private final MetadataClient metadataClient;
    private final WorkflowClient workflowClient;

    private final TaskClient taskClient;

    static final String taskName = "test_rate_limit_operation";

    static final String workflowName = "rate_limit";

    public WorkflowRateLimitExample() {
        OrkesClients orkesClients = ApiUtil.getOrkesClient();
        metadataClient = orkesClients.getMetadataClient();
        workflowClient = orkesClients.getWorkflowClient();
        taskClient = orkesClients.getTaskClient();
    }

    @Test
    @DisplayName("test static rate limit operation")
    public void testStaticRateLimit() {
        terminateExistingRunningWorkflows(workflowName);
        registerTask();
        registerWorkflow();
        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(1);
        String workflowId1 = workflowClient.startWorkflow(startWorkflowRequest);
        String workflowId2 = workflowClient.startWorkflow(startWorkflowRequest);
        String workflowId3 = workflowClient.startWorkflow(startWorkflowRequest);

        //Only two instance of the workflow will have tasks in running status.
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId1, true).getTasks().size());
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId2, true).getTasks().size());
        Assertions.assertEquals(0, workflowClient.getWorkflow(workflowId3, true).getTasks().size());

        // Complete workflow1 so that workflow3 can start
        completeTask(workflowId1, workflowClient.getWorkflow(workflowId1, true).getTasks().getFirst().getTaskId());
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId3, true).getTasks().size());

    }

    @Test
    @DisplayName("test dynamic rate limit operation based on correlationId")
    public void testDynamicRateLimit() {
        terminateExistingRunningWorkflows(workflowName);
        registerTask();
        registerWorkflow();
        WorkflowDef workflowDef = getWorkflowDef();
        RateLimitConfig rateLimitConfig = workflowDef.getRateLimitConfig();
        rateLimitConfig.setRateLimitKey("${workflow.correlationId}");
        rateLimitConfig.setConcurrentExecLimit(1); // Only one instance is allowed per correlationId
        workflowDef.setRateLimitConfig(rateLimitConfig);
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
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId1, true).getTasks().size());
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId3, true).getTasks().size());

        Assertions.assertEquals(0, workflowClient.getWorkflow(workflowId2, true).getTasks().size());
        Assertions.assertEquals(0, workflowClient.getWorkflow(workflowId4, true).getTasks().size());

        // Complete the task so that next workflow tasks will start running.
        completeTask(workflowId1, workflowClient.getWorkflow(workflowId1, true).getTasks().getFirst().getTaskId());
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId2, true).getTasks().size());

        completeTask(workflowId3, workflowClient.getWorkflow(workflowId3, true).getTasks().getFirst().getTaskId());
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        Assertions.assertEquals(1, workflowClient.getWorkflow(workflowId4, true).getTasks().size());
    }

    private void completeTask(String workflowId, String taskId) {
        TaskResult taskResult = new TaskResult();
        taskResult.setStatus(TaskResult.Status.COMPLETED);
        taskResult.setTaskId(taskId);
        taskResult.setWorkflowInstanceId(workflowId);
        taskClient.updateTask(taskResult);
    }

    void registerTask() {
        TaskDef taskDef = new TaskDef();
        taskDef.setName(taskName);
        taskDef.setOwnerEmail("example@orkes.io");
        List<TaskDef> taskDefs = new ArrayList<>();
        taskDefs.add(taskDef);
        try {
            this.metadataClient.registerTaskDefs(taskDefs);
        } catch (Exception e) {
        }
    }

    void registerWorkflow() {
        WorkflowDef workflowDef = getWorkflowDef();
        try {
            metadataClient.registerWorkflowDef(workflowDef, true);
        } catch (Exception e) {
        }
    }

    static WorkflowDef getWorkflowDef() {
        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName(workflowName);
        workflowDef.setVersion(1);
        workflowDef.setOwnerEmail("example@orkes.io");
        workflowDef.setTimeoutSeconds(600);
        workflowDef.setTimeoutPolicy(WorkflowDef.TimeoutPolicy.TIME_OUT_WF);
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName(taskName);
        workflowTask.setTaskReferenceName(taskName);
        workflowDef.setTasks(List.of(workflowTask));
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setRateLimitKey(workflowName);
        rateLimitConfig.setConcurrentExecLimit(2); // Set the limit to 2.
        workflowDef.setRateLimitConfig(rateLimitConfig);
        return workflowDef;
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
