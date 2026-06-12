package com.ruoyi.common.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compatibility endpoint for external service alive checks.
 */
@RestController
public class HealthCheckController
{
    @GetMapping("/checkAliveServer")
    public String checkAliveServer()
    {
        return "ok";
    }
}
