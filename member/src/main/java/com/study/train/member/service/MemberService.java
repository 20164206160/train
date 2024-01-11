package com.study.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.study.train.member.domain.Member;
import com.study.train.member.domain.MemberExample;
import com.study.train.member.mapper.MemberMapper;
import com.study.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq memberRegisterReq){
        String mobile = memberRegisterReq.getMobile();
        //查询手机号是否存在
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);

        if(CollUtil.isNotEmpty(members)){
//            return members.get(0).getId();
            throw new RuntimeException("手机号已注册");
        }

        Member member = new Member();
        member.setId(System.currentTimeMillis());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }
}
