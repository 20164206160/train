package com.study.train.batch.feign;

import com.study.train.common.response.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

//@FeignClient("business")
@FeignClient(name="business", url = "http://localhost:8083/business")
public interface BusinessFeign {

    @GetMapping("/admin/daily-train/gen-daily/{date}")
    CommonResp<Object> genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date);

    @GetMapping("hello")
    String hello();
}
