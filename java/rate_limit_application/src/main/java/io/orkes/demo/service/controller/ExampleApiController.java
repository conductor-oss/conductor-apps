package io.orkes.demo.service.controller;

import io.orkes.demo.service.WorkflowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class ExampleApiController {

    private final WorkflowService workflowService;

    @PostMapping(value = "/triggerStaticRateLimitWorkflow", produces = "application/json")
    public void checkStaticRateLimit() {
        workflowService.triggerStaticRateLimitWorkflow();
    }


    // docs-marker-start-1
    @PostMapping(value = "/triggerDynamicRateLimitWorkflow", produces = "application/json")
    public void checkDynamicRateLimit() {
        workflowService.triggerDynamicRateLimitWorkflow();
    }

}
