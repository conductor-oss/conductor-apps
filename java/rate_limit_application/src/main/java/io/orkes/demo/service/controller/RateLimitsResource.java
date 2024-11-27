package io.orkes.demo.service.controller;

import io.orkes.demo.service.WorkflowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitsResource {
    private final WorkflowService workflowService;

    @PostMapping("/static")
    public void triggerStaticRateLimitWorkflow() {
        workflowService.triggerStaticRateLimitWorkflow();
    }

    @PostMapping("/dynamic")
    public void triggerDynamicRateLimitWorkflow() {
        workflowService.triggerDynamicRateLimitWorkflow();
    }
}
