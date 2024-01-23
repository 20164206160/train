package com.study.train.business.req;

import com.study.train.common.req.PageReq;
import lombok.Data;

@Data
public class DailyTrainSeatQueryReq extends PageReq {

    private String trainCode;

    @Override
    public String toString() {
        return "DailyTrainSeatQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                "} " + super.toString();
    }
}
