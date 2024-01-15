package com.study.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.study.train.common.context.LoginMemberContext;
import com.study.train.common.response.PageResp;
import com.study.train.common.util.SnowUtil;
import com.study.train.member.domain.Passenger;
import com.study.train.member.domain.PassengerExample;
import com.study.train.member.mapper.PassengerMapper;
import com.study.train.member.req.PassengerQueryReq;
import com.study.train.member.req.PassengerSaveReq;
import com.study.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {
    
    private static final Logger LOG = LoggerFactory.getLogger(PassengerService.class);
    
    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq passengerSaveReq){
        Passenger passenger = BeanUtil.copyProperties(passengerSaveReq, Passenger.class);
        passenger.setMemberId(LoginMemberContext.getId());
        passenger.setId(SnowUtil.getSnowflakeNextId());
        DateTime now = DateTime.now();
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }

    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq passengerQueryReq){
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        if(ObjectUtil.isNull(passengerQueryReq.getMemberId())){
            criteria.andMemberIdEqualTo(passengerQueryReq.getMemberId());
        }

        LOG.info("查询页码：{}",passengerQueryReq.getPage());
        LOG.info("每页条数：{}",passengerQueryReq.getSize());
        PageHelper.startPage(passengerQueryReq.getPage(), passengerQueryReq.getSize());
        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);

        PageInfo<Passenger> pageInfo = new PageInfo<>(passengers);
        LOG.info("总行数：{}",pageInfo.getTotal());
        LOG.info("总页数：{}",pageInfo.getPages());

        List<PassengerQueryResp> passengerQueryResps = BeanUtil.copyToList(passengers, PassengerQueryResp.class);
        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageResp.getTotal());
        pageResp.setList(passengerQueryResps);

        return pageResp;
    }
}
