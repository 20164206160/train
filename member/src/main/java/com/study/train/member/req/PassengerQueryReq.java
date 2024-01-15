package com.study.train.member.req;

import com.study.train.common.req.PageReq;
import lombok.Data;

@Data
public class PassengerQueryReq extends PageReq {

    private Long memberId;
}