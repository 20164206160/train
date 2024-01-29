package com.study.train.member.controller.admin;

import com.study.train.common.response.CommonResp;
import com.study.train.common.response.PageResp;
import com.study.train.member.req.TicketQueryReq;
import com.study.train.member.resp.TicketQueryResp;
import com.study.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {

    @Resource
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req) {
        PageResp<TicketQueryResp> list = ticketService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return new CommonResp<>();
    }

}
