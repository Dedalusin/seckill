package com.seckill.springbootseckill.service.impl;

import com.seckill.springbootseckill.annotation.ServiceLock;
import com.seckill.springbootseckill.dao.DynamicQuery;
import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.model.Seckill;
import com.seckill.springbootseckill.model.SeckillStatEnum;
import com.seckill.springbootseckill.model.SuccessKilled;
import com.seckill.springbootseckill.repository.SeckillRepository;
import com.seckill.springbootseckill.service.ISeckillService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service("seckillService")
public class SeckillServiceImpl implements ISeckillService {

    /**
     * 参数代表是否是公平锁，true代表公平，false代表不公平，默认是false
     * 公平锁：先到先得
     * 不公平：后来也可以先得
     */
    Lock lock = new ReentrantLock(true);

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
     * 代码逻辑基本和秒杀一是一样的，只是在进入时上锁，运行完解锁
     * 但还是会出现超卖的现象，因为锁是在事务里面，解锁时事务不一定提交
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckilLock(long seckillId, long userId) {
        //上锁
        lock.lock();
        try {
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
                /**
                 * 如果没有分表，可以直接使用save
                 * 当然，可以进行水平分表进行优化
                 * String table = "success_killed_"+userId%8;
                 * 使用模运算，利用userId水平分表,然后insert into table 即可
                 */
                dynamicQuery.save(killed);

            } else {
                return Result.error(SeckillStatEnum.END);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return Result.ok(SeckillStatEnum.SUCCESS);
    }
    /**
     * 秒杀 二、程序锁AOP
     * @param seckillId
     * @param userId
     * @return
     * 这里可以通过代理模式，jdk和cglib代理都是在方法的上下进行增强，是在方法的外部进行，因而可以包围事务
     * AOP也是通过代理模式进行实现
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @ServiceLock
    public Result startSeckilAopLock(long seckillId, long userId) {
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
