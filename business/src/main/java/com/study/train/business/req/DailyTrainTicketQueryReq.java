package com.study.train.business.req;

import com.study.train.common.req.PageReq;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DailyTrainTicketQueryReq extends PageReq {
    /**
     * 日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    /**
     * 车次编号
     */
    private String trainCode;
    /**
     * 出发站
     */
    private String start;
    /**
     * 到达站
     */
    private String end;
}
