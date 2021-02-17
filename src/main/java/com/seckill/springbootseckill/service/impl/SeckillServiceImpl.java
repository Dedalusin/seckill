package com.seckill.springbootseckill.service.impl;

import com.seckill.springbootseckill.dao.DynamicQuery;
import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.model.Seckill;
import com.seckill.springbootseckill.model.SeckillStatEnum;
import com.seckill.springbootseckill.model.SuccessKilled;
import com.seckill.springbootseckill.repository.SeckillRepository;
import com.seckill.springbootseckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
@Service("seckillService")
public class SeckillServiceImpl implements ISeckillService {

    @Autowired
    DynamicQuery dynamicQuery;
    @Autowired
    SeckillRepository seckillRepository;
    @Override
    public List<Seckill> getSeckillList() {
        return seckillRepository.findAll();
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillRepository.findById(seckillId).get();
    }

    @Override
    public Long getSeckillCount(long seckillId) {
        String nativeSql = "SELECT count(*) FROM success_killed WHERE seckill_id=?";
        Object object =  dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
        return ((Number) object).longValue();
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckill(long seckillId) {
        String nativeSql = "DELETE FROM  success_killed WHERE seckill_id=?";
        dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
        nativeSql = "UPDATE seckill SET number =100 WHERE seckill_id=?";
        dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
    }
    /**
     * 秒杀 一、会出现数量错误
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckil(long seckillId, long userId) {
        /**
         * 分为三部分:
         * 查询库存
         * 库存大于零，则进行减1
         * 创建订单并存储
         */
        //查询
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
            /**
             * 当然，可以进行水平分表进行优化
             * String table = "success_killed_"+userId%8;
             * 使用模运算，利用userId水平分表,然后insert into table 即可
             */
            return Result.ok(SeckillStatEnum.SUCCESS);
        } else {
            return Result.error(SeckillStatEnum.END);
        }
    }
    /**
     * 秒杀 二、程序锁
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilLock(long seckillId, long userId) {
        return null;
    }
    /**
     * 秒杀 二、程序锁AOP
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilAopLock(long seckillId, long userId) {
        return null;
    }
    /**
     * 秒杀 二、数据库悲观锁
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilDBPCC_ONE(long seckillId, long userId) {
        return null;
    }
    /**
     * 秒杀 三、数据库悲观锁
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilDBPCC_TWO(long seckillId, long userId) {
        return null;
    }
    /**
     * 秒杀 三、数据库乐观锁
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilDBOCC(long seckillId, long userId, long number) {
        return null;
    }
    /**
     * 秒杀 四、事物模板
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilTemplate(long seckillId, long userId, long number) {
        return null;
    }
}
