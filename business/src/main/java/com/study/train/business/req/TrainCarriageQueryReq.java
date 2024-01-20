package com.study.train.business.req;

import com.study.train.common.req.PageReq;
import lombok.Data;

@Data
public class TrainCarriageQueryReq extends PageReq {
    private String trainCode;

    @Override
    public String toString() {
        return "TrainCarriageQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                "} " + super.toString();
    }
}
