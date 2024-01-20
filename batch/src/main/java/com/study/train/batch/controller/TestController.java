package com.study.train.batch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  适合单体应用，不适合集群
 *  没法实时更改定时任务状态和策略
 */
@RestController
public class TestController {
    @GetMapping("/hello")
    public String hello(){
        //  增加分布式锁，解决集群问题
        return "Hello World Batch";
    }
}
