package com.study.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.study.train.business.domain.TrainStation;
import com.study.train.business.domain.TrainStationExample;
import com.study.train.business.mapper.TrainStationMapper;
import com.study.train.business.req.TrainStationQueryReq;
import com.study.train.business.req.TrainStationSaveReq;
import com.study.train.business.resp.TrainStationQueryResp;
import com.study.train.common.exception.BusinessException;
import com.study.train.common.exception.BusinessExceptionEnum;
import com.study.train.common.response.PageResp;
import com.study.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;

    public void save(TrainStationSaveReq req) {
        DateTime now = DateTime.now();
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);
        if (ObjectUtil.isNull(trainStation.getId())) {

            //保存之前，先校验主键是否存在
            TrainStation trainStationDB = selectByUnique(req.getTrainCode(),req.getIndex());
            if(ObjectUtil.isNotNull(trainStationDB)){
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            }

            //保存之前，先校验主键是否存在
            trainStationDB = selectByUnique(req.getTrainCode(),req.getName());
            if(ObjectUtil.isNotNull(trainStationDB)){
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
            }

            trainStation.setId(SnowUtil.getSnowflakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        } else {
            trainStation.setUpdateTime(now);
            trainStationMapper.updateByPrimaryKey(trainStation);
        }
    }
    private TrainStation selectByUnique(String trainCode, Integer index) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainStation> trainStations = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(trainStations)){
            return trainStations.get(0);
        }
        return null;
    }
    private TrainStation selectByUnique(String trainCode, String name) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andNameEqualTo(name);
        List<TrainStation> trainStations = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(trainStations)){
            return trainStations.get(0);
        }
        return null;
    }
    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("train_code,`index`  asc");
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();

        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainStation> trainStationList = trainStationMapper.selectByExample(trainStationExample);

        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStationList, TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainStationMapper.deleteByPrimaryKey(id);
    }
}
