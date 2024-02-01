package com.study.train.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class TestController {

    @Value("${test.nacos}")
    private String testNacos;

    @Autowired
    private Environment environment;

    @GetMapping("/hello")
    public String hello(){
        String property = environment.getProperty("local.server.port");
        return property + " Hello World " + testNacos;
    }
}
