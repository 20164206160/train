package com.study.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRegisterReq {
    //手机号
    @NotBlank(message = "【手机号】不能为空")
    private String mobile;
}
