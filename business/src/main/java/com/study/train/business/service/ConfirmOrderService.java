package com.study.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.study.train.business.domain.*;
import com.study.train.business.enums.ConfirmOrderStatusEnum;
import com.study.train.business.enums.RedisKeyPreEnum;
import com.study.train.business.enums.SeatColEnum;
import com.study.train.business.enums.SeatTypeEnum;
import com.study.train.business.mapper.ConfirmOrderMapper;
import com.study.train.business.req.ConfirmOrderDoReq;
import com.study.train.business.req.ConfirmOrderQueryReq;
import com.study.train.business.req.ConfirmOrderTicketReq;
import com.study.train.business.resp.ConfirmOrderQueryResp;
import com.study.train.common.context.LoginMemberContext;
import com.study.train.common.exception.BusinessException;
import com.study.train.common.exception.BusinessExceptionEnum;
import com.study.train.common.response.PageResp;
import com.study.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

     @Autowired
     private RedissonClient redissonClient;

     @Resource
     private SkTokenService skTokenService;

    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    //@SentinelResource("doConfirm"）
    //public synchronized void doConfirm(ConfirmOrderDoReq req) {
    @SentinelResource(value = "doConfirm", blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderDoReq req) {

         // 校验令牌余量
         boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
         if (validSkToken) {
             LOG.info("令牌校验通过");
         } else {
             LOG.info("令牌校验不通过");
             throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
         }

        // 获取分布式锁
        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
        // setIfAbsent就是对应redis的setnx
//        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
//        if (Boolean.TRUE.equals(setIfAbsent)) {
//            LOG.info("恭喜，抢到锁了！lockKey：{}", lockKey);
//        } else {
//            // 只是没抢到锁，并不知道票抢完了没，所以提示稍候再试
//            LOG.info("很遗憾，没抢到锁！lockKey：{}", lockKey);
//            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
////            LOG.info("没抢到锁，有其它消费线程正在出票，不做任何处理");
////            return;
//        }

        RLock lock = null;
        try {
             // 使用redisson，自带看门狗
             lock = redissonClient.getLock(lockKey);

//             // 红锁的写法 多台redis集群 解决单机redis宕机问题
//              RedissonRedLock redissonRedLock = new RedissonRedLock(lock, lock, lock);
//              boolean tryLock1 = redissonRedLock.tryLock(0, TimeUnit.SECONDS);

             /**
               waitTime – the maximum time to acquire the lock 等待获取锁时间(最大尝试获得锁的时间)，超时返回false
               leaseTime – lease time 锁时长，即n秒后自动释放锁
               time unit – time unit 时间单位
              */
             // boolean tryLock = lock.tryLock(30, 10, TimeUnit.SECONDS); // 不带看门狗
             boolean tryLock = lock.tryLock(0, TimeUnit.SECONDS); // 带看门狗
             if (tryLock) {
                 LOG.info("恭喜，抢到锁了！");
                 // 可以把下面这段放开，只用一个线程来测试，看看redisson的看门狗效果
                 // for (int i = 0; i < 30; i++) {
                 //     Long expire = redisTemplate.opsForValue().getOperations().getExpire(lockKey);
                 //     LOG.info("锁过期时间还有：{}", expire);
                 //     Thread.sleep(1000);
                 // }
             } else {
                 // 只是没抢到锁，并不知道票抢完了没，所以提示稍候再试
                 LOG.info("很遗憾，没抢到锁");
                 throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
             }

            //业务校验，车次是否存在，余票是否存在，tickets条数>0,同乘客同车次是否已买过
            Date date = req.getDate();
            String trainCode = req.getTrainCode();
            String start = req.getStart();
            String end = req.getEnd();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();
            //保存确认订单，状态初始
            DateTime now = DateTime.now();
            ConfirmOrder confirmOrder = new ConfirmOrder();
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setMemberId(LoginMemberContext.getId());

            confirmOrder.setDate(date);
            confirmOrder.setTrainCode(trainCode);
            confirmOrder.setStart(start);
            confirmOrder.setEnd(end);
            confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrder.setTickets(JSON.toJSONString(tickets));
            confirmOrderMapper.insert(confirmOrder);
            //查出余票记录，需要得到真实的库存
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
            LOG.info("查出余票记录：{}",JSON.toJSONString(dailyTrainTicket));
            //扣减余票数量，并判断余票是否足够
            reduceTickets(req, dailyTrainTicket);

            // 最终的选座结果
            List<DailyTrainSeat> finalSeatList = new ArrayList<>();
            // 计算相对第一个座位的偏移值
            // 比如选择的是C1,D2，则偏移值是：[0,5]
            // 比如选择的是A1,B1,C1，则偏移值是：[0,1,2]
            ConfirmOrderTicketReq ticketReq0 = tickets.get(0);
            if(StrUtil.isNotBlank(ticketReq0.getSeat())){
                LOG.info("本次购票有选座");
                // 查出本次选座的座位类型都有哪些列，用于计算所选座位与第一个座位的偏离值
                List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());
                LOG.info("本次选座的座位类型包含的列：{}", colEnumList);

                List<Integer> offsetList = new ArrayList<>();
                // 组成和前端两排选座一样的列表，用于作参照的座位列表，例：referSeatList = {A1, C1, D1, F1, A2, C2, D2, F2}
                for(int i=1,j=0,k=0;i<=2;i++){
                    for(SeatColEnum seatColEnum : colEnumList){
                        if(j<tickets.size() && tickets.get(j).getSeat().equals(seatColEnum.getCode() + i)){
                            offsetList.add(k);
                            j++;
                        }
                        if(!offsetList.isEmpty()) {
                            k++;
                        }
                    }
                }
                LOG.info("计算得到所有座位的相对第一个座位的偏移值：{}", offsetList);

                //"A1->A"
                getSeat(finalSeatList,date, trainCode, ticketReq0.getSeatTypeCode(), ticketReq0.getSeat().split("")[0], offsetList, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());

            }else{
                LOG.info("本次购票没有选座");
                for(ConfirmOrderTicketReq ticketReq: tickets){
                    getSeat(finalSeatList,date, trainCode, ticketReq.getSeatTypeCode(),null, null,dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
                }
            }

            LOG.info("最终选座：{}", finalSeatList);

            //选座座位后事务处理
            //座位表修改售卖情况sell
            //余票详情表修改余票
            //为会员增加购买记录
            //更新确认订单为成功

            try {
                afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, tickets, confirmOrder);
            } catch (Exception e) {
                LOG.error("保存购票信息失败", e);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
            }
        } catch (InterruptedException e) {
            LOG.error("购票异常", e);
            throw new RuntimeException(e);
        } finally {
            // try finally不能包含加锁的那段代码，否则加锁失败会走到finally里，从而释放别的线程的锁
//            LOG.info("购票流程结束，释放锁！lockKey：{}", lockKey);
//            redisTemplate.delete(lockKey);
             LOG.info("购票流程结束，释放锁！");
             if (null != lock && lock.isHeldByCurrentThread()) {
                 lock.unlock();
             }
        }

    }

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }

    /**
     * 挑座位，如果有选座，则一次性挑完，如果无选座，则一个一个挑
     */
    private void getSeat(List<DailyTrainSeat> finalSeatList, Date date, String trainCode, String seatType, String column, List<Integer> offsetList, Integer startIndex, Integer endIndex){
        List<DailyTrainCarriage> dailyTrainCarriages = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢", dailyTrainCarriages.size());

        List<DailyTrainSeat> getSeatList = new ArrayList<>();
        //一个车厢一个车厢的获取座位数据
        for(DailyTrainCarriage dailyTrainCarriage : dailyTrainCarriages){
            LOG.info("开始从车厢{}选座", dailyTrainCarriage.getIndex());
            getSeatList.clear();
            List<DailyTrainSeat> dailyTrainSeats = dailyTrainSeatService.selecetByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}", dailyTrainCarriage.getIndex(), dailyTrainSeats.size());
            for(DailyTrainSeat dailyTrainSeat : dailyTrainSeats){
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();
                String col = dailyTrainSeat.getCol();

                // 判断当前座位不能被选中过
                boolean alreadyChooseFlag = false;
                for (DailyTrainSeat finalSeat : finalSeatList){
                    if (finalSeat.getId().equals(dailyTrainSeat.getId())) {
                        alreadyChooseFlag = true;
                        break;
                    }
                }
                if (alreadyChooseFlag) {
                    LOG.info("座位{}被选中过，不能重复选中，继续判断下一个座位", seatIndex);
                    continue;
                }
                // 判断column，有值的话要比对列号
                if (StrUtil.isBlank(column)) {
                    LOG.info("无选座");
                }else{
                    if (!column.equals(col)) {
                        LOG.info("座位{}列值不对，继续判断下一个座位，当前列值：{}，目标列值：{}", seatIndex, col, column);
                        continue;
                    }
                }

                boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if(isChoose){
                    getSeatList.add(dailyTrainSeat);
                    LOG.info("选中座位");
                }else{
                    continue;
                }

                // 根据offset选剩下的座位
                boolean isGetAllOffsetSeat = true;
                if(CollUtil.isNotEmpty(offsetList)){
                    LOG.info("有偏移值：{}，校验偏移的座位是否可选", offsetList);
                    // 从索引1开始，索引0就是当前已选中的票
                    for (int i=1;i<offsetList.size();i++){
                        Integer offset = offsetList.get(i);
                        // 座位在库的索引是从1开始
                        // int nextIndex = seatIndex + offset - 1;
                        int nextIndex = seatIndex + offset - 1;

                        //有选座时，一定是在同一个车箱
                        if(nextIndex >= dailyTrainSeats.size()){
                            LOG.info("座位{}不可选，偏移后的索引超出了这个车箱的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }

                        DailyTrainSeat dailyTrainSeatNext = dailyTrainSeats.get(nextIndex);
                        boolean isChooseNext = calSell(dailyTrainSeatNext, startIndex, endIndex);
                        if(isChooseNext){
                            LOG.info("座位{}被选中", dailyTrainSeatNext.getCarriageSeatIndex());
                            getSeatList.add(dailyTrainSeatNext);
                        }else{
                            LOG.info("座位{}不可选", dailyTrainSeatNext.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }

                    }
                }
                if (!isGetAllOffsetSeat) {
                    getSeatList.clear();
                    continue;
                }

                finalSeatList.addAll(getSeatList);
                // 保存选好的座位
                return;
            }
        }
    }

    /**
     * 计算某座位在区间内是否可卖
     * 例：sell=10001，本次购买区间站1~4，则区间已售000
     * 全部是0，表示这个区间可买；只要有1，就表示区间内已售过票
     *
     * 选中后，要计算购票后的sell，比如原来是10001，本次购买区间站1~4
     * 方案：构造本次购票造成的售卖信息01110，和原sell 10001按位与，最终得到11111
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex){
        String sell = dailyTrainSeat.getSell();
        String sellPart = sell.substring(startIndex, endIndex);
        if(sellPart.contains("1")){
            LOG.info("座位{}在本次车站区间{}~{}已售过票，不可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        }else{
            LOG.info("座位{}在本次车站区间{}~{}未售过票，可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            //  10001,   11111
            String newSell = sell.substring(0,startIndex) + sellPart.replace('0', '1') + sell.substring(endIndex);

            LOG.info("座位{}被选中，原售票信息：{}，车站区间：{}~{}，最终售票信息：{}"
                    , dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
        }
    }
    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq confirmOrderTicketReq : req.getTickets()){
            String seatTypeCode = confirmOrderTicketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum){
                case YDZ -> {
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if(countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ -> {
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if(countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW -> {
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if(countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW -> {
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if(countLeft<0){
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }
}
