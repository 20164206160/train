package com.study.train.member.controller;

import com.study.train.common.response.CommonResp;
import com.study.train.member.req.MemberRegisterReq;
import com.study.train.member.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/count")
    public CommonResp<Integer> count(){
        int count  = memberService.count();
        return new CommonResp<>(count);
    }

    @PostMapping("/register")
    public CommonResp<Long> register(MemberRegisterReq memberRegisterReq){
        return new CommonResp<>(memberService.register(memberRegisterReq));
    }
}
