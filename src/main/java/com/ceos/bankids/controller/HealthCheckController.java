package com.ceos.bankids.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthCheckController {

    @ApiOperation(value = "Health Check")
    @GetMapping(value = "")
    @ResponseBody
    public String healthCheck() {
        log.info("health");
        return "Healthy!!";
    }
}