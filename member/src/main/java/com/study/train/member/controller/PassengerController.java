package com.study.train.member.controller;

import com.study.train.common.response.CommonResp;
import com.study.train.member.req.PassengerSaveReq;
import com.study.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/passenger")
public class PassengerController {
    @Resource
    private PassengerService passengerService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody PassengerSaveReq passengerSaveReq) {
        passengerService.save(passengerSaveReq);
        return new CommonResp<>();
    }
}
