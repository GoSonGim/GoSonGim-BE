package com.example.GoSonGim_BE.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GoSonGim_BE.global.constant.ApiVersion;

import java.util.Map;

@RestController
@RequestMapping(ApiVersion.CURRENT)
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "GoSonGim-BE"
        ));
    }
}

