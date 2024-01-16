package com.study.train.member.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class PassengerSaveReq {
    private Long id;

    private Long memberId;

    @NotBlank(message = "【名字】不能为空")
    private String name;

    @NotBlank(message = "【身份证】不能为空")
    private String idCard;

    @NotBlank(message = "【旅客类型】不能为空")
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "CMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "CMT+8")
    private Date updateTime;
}