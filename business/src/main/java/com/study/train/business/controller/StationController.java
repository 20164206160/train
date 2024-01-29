package com.study.train.business.controller;

import com.study.train.business.req.StationQueryReq;
import com.study.train.business.resp.StationQueryResp;
import com.study.train.business.service.StationService;
import com.study.train.common.response.CommonResp;
import com.study.train.common.response.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/station")
public class StationController {

    @Resource
    private StationService stationService;
    @GetMapping("/query-list")
    public CommonResp<PageResp<StationQueryResp>> queryList(@Valid StationQueryReq req) {
        PageResp<StationQueryResp> list = stationService.queryList(req);
        return new CommonResp<>(list);
    }
}
