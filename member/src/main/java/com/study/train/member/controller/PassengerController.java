package com.study.train.member.controller;

import com.study.train.common.context.LoginMemberContext;
import com.study.train.common.response.CommonResp;
import com.study.train.common.response.PageResp;
import com.study.train.member.req.PassengerQueryReq;
import com.study.train.member.req.PassengerSaveReq;
import com.study.train.member.resp.PassengerQueryResp;
import com.study.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/query-list")
    public CommonResp<PageResp<PassengerQueryResp>> queryList(@Valid @RequestBody PassengerQueryReq passengerQueryReq) {
        passengerQueryReq.setMemberId(LoginMemberContext.getId());
        return new CommonResp<>(passengerService.queryList(passengerQueryReq));
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        passengerService.delete(id);
        return new CommonResp<>();
    }
}
