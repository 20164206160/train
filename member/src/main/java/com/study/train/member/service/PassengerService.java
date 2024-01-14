package com.study.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.study.train.common.util.SnowUtil;
import com.study.train.member.domain.Passenger;
import com.study.train.member.mapper.PassengerMapper;
import com.study.train.member.req.PassengerSaveReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PassengerService {

    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq passengerSaveReq){
        Passenger passenger = BeanUtil.copyProperties(passengerSaveReq, Passenger.class);
        passenger.setId(SnowUtil.getSnowflakeNextId());
        DateTime now = DateTime.now();
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }

}
