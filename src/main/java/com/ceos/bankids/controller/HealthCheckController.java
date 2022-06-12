package com.ceos.bankids.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Log
@Controller
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthCheckController {

    @GetMapping(value = "")
    @ResponseBody
    public String healthCheck() {
        return "Healthy!!";
    }
}