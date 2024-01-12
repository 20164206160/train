package com.study.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.study.train.common.exception.BusinessException;
import com.study.train.common.exception.BusinessExceptionEnum;
import com.study.train.common.util.JwtUtil;
import com.study.train.common.util.SnowUtil;
import com.study.train.member.domain.Member;
import com.study.train.member.domain.MemberExample;
import com.study.train.member.mapper.MemberMapper;
import com.study.train.member.req.MemberLoginReq;
import com.study.train.member.req.MemberRegisterReq;
import com.study.train.member.req.MemberSendCodeReq;
import com.study.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;

    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq memberRegisterReq){
        String mobile = memberRegisterReq.getMobile();
        //查询手机号是否存在
        Member memberDB = selectByMobile(mobile);

        if(ObjectUtil.isNull(memberDB)){
//            return members.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        Member member = new Member();
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }

    //发送验证码
    public void sendCode(MemberSendCodeReq memberSendCodeReq){
        String mobile = memberSendCodeReq.getMobile();
        //查询手机号是否存在
        Member memberDB = selectByMobile(mobile);

        //如果手机号不存在，则插入一条记录
        if(ObjectUtil.isNull(memberDB)){
            LOG.info("手机号不存在，插入一条记录");
            Member member = new Member();
            member.setId(SnowUtil.getSnowflakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
        }else{
            LOG.info("手机号存在，不插入记录");
        }
        //生成验证码
        String code =  RandomUtil.randomString(4);
        LOG.info("生成短信验证码：{}", code);

        //保存短信记录表: 手机号，短信验证码，有效期，是否已使用，业务类型，发送时间，使用时间
        LOG.info("保存短信记录表");

        //对接短信通道，发送短信
        LOG.info("发送短信验证码");
    }

    //发送验证码
    public MemberLoginResp login(MemberLoginReq memberLoginReq){
        String mobile = memberLoginReq.getMobile();
        String code = memberLoginReq.getCode();
        Member memberDB = selectByMobile(mobile);

        //如果手机号不存在，则插入一条记录
        if(ObjectUtil.isNull(memberDB)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        //校验短信验证码
        if(!"8888".equals(code)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }
        MemberLoginResp memberLoginResp = BeanUtil.copyProperties(memberDB, MemberLoginResp.class);
        memberLoginResp.setToken(JwtUtil.createToken(memberLoginResp.getId(),memberLoginResp.getMobile()));
        return memberLoginResp;
    }

    private Member selectByMobile(String mobile) {
        //查询手机号是否存在
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);

        //如果手机号不存在，则插入一条记录
        if(CollUtil.isEmpty(members)){
            return null;
        }else{
            return members.get(0);
        }
    }
}
