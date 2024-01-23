package com.study.train.business.req;

import com.study.train.common.req.PageReq;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DailyTrainStationQueryReq extends PageReq {

    private String trainCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @Override
    public String toString() {
        return "DailyTrainStationQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                ", date=" + date +
                "} " + super.toString();
    }
}
