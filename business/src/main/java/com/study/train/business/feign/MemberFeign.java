package com.study.train.business.feign;

import com.study.train.common.req.MemberTicketReq;
import com.study.train.common.response.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

// @FeignClient("member")
@FeignClient(name = "member", url = "http://127.0.0.1:8082")
public interface MemberFeign {

    @GetMapping("/member/feign/ticket/save")
    CommonResp<Object> save(@RequestBody MemberTicketReq req);

}
