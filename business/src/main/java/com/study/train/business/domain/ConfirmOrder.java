package com.study.train.business.domain;

import lombok.Data;

import java.util.Date;

@Data
public class ConfirmOrder {
    private Long id;

    private Long memberId;

    private Date date;

    private String trainCode;

    private String start;

    private String end;

    private Long dailyTrainTicketId;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String tickets;
}