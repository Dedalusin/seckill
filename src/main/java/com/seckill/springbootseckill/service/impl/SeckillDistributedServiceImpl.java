package com.seckill.springbootseckill.service.impl;

import com.seckill.springbootseckill.annotation.DistributedServiceLock;
import com.seckill.springbootseckill.annotation.ZkLock;
import com.seckill.springbootseckill.dao.DynamicQuery;
import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.model.SeckillStatEnum;
import com.seckill.springbootseckill.model.SuccessKilled;
import com.seckill.springbootseckill.service.ISeckillDistributedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
@Service
public class SeckillDistributedServiceImpl implements ISeckillDistributedService {

    @Autowired
    DynamicQuery dynamicQuery;

    @Override
    @DistributedServiceLock
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckillRedisLock(long seckillId, long userId) {
        String nativeSql = "SELECT number FROM seckill WHERE seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(nativeSql,new Object[]{seckillId});
        Long num = ((Number) object).longValue();
        if (num > 0){
            //库存扣除
            nativeSql = "UPDATE seckill  SET number=number-1 WHERE seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
            //创建订单
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short)0);
            killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            //如果没有分表，可以直接使用save
            dynamicQuery.save(killed);
            return Result.ok(SeckillStatEnum.SUCCESS);
        } else {
            return Result.error(SeckillStatEnum.END);
        }
    }

    @Override
    @ZkLock
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckillZkLock(long seckillId, long userId) {
        String nativeSql = "SELECT number FROM seckill WHERE seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(nativeSql,new Object[]{seckillId});
        Long num = ((Number) object).longValue();
        if (num > 0){
            //库存扣除
            nativeSql = "UPDATE seckill  SET number=number-1 WHERE seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
            //创建订单
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short)0);
            killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            //如果没有分表，可以直接使用save
            dynamicQuery.save(killed);
            return Result.ok(SeckillStatEnum.SUCCESS);
        } else {
            return Result.error(SeckillStatEnum.END);
        }
    }

    @Override
    public Result startSeckillLock(long seckillId, long userId, long number) {
        return null;
    }
}
