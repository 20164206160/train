package com.study.train.business.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Train implements Serializable {
    private Long id;

    private String code;

    private String type;

    private String start;

    private String startPinyin;

    private Date startTime;

    private String end;

    private String endPinyin;

    private Date endTime;

    private Date createTime;

    private Date updateTime;
}