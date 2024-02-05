package com.study.train.business.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @SentinelResource("hello")
    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        int i = RandomUtil.randomInt(1, 10);
        if (i <= 2) {
            throw new RuntimeException("测试异常");
        }
//        Thread.sleep(500);
        return "Hello World Business";
    }

    @SentinelResource("hello1")
    @GetMapping("/hello1")
    public String hello1() throws InterruptedException {
        Thread.sleep(500);
        return "Hello World Business1";
    }
}
