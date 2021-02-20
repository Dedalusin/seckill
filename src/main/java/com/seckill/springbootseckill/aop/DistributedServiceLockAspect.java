package com.seckill.springbootseckill.aop;


import com.seckill.springbootseckill.utils.RedissLockUtil;
import com.seckill.springbootseckill.utils.ZkLockUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author 迪达勒斯
 */
@Aspect
@Component
@Scope
@Order(1)
public class DistributedServiceLockAspect {

    final static Logger LOGGER = LoggerFactory.getLogger(DistributedServiceLockAspect.class);

    @Pointcut("@annotation(com.seckill.springbootseckill.annotation.DistributedServiceLock)")
    public void distributedServicePoint(){};

    @Pointcut("@annotation(com.seckill.springbootseckill.annotation.ZkLock)")
    public void ZkLockPoint(){};

    @Around("distributedServicePoint()")
    public Object distributedServiceLock(ProceedingJoinPoint point){
        Boolean isLocked = false;
        //获取变量
        Object[] args = point.getArgs();
        Object object = null;
        Long seckillId = ((Number) args[0]).longValue();
        try {
            LOGGER.info("准备开始对 seckillId : "+seckillId+" 上锁");
            isLocked = RedissLockUtil.tryLock(seckillId+"", TimeUnit.SECONDS,3,10);
            if (isLocked){
                object = point.proceed();
            }
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (isLocked){
                RedissLockUtil.unlock(seckillId+"");
                LOGGER.info("解锁");
            }
        }
        return object;
    }

    @Around("ZkLockPoint()")
    public Object ZkLock(ProceedingJoinPoint point){
        Boolean isLocked = false;
        //获取变量
        Object[] args = point.getArgs();
        Object object = null;
        Long seckillId = ((Number) args[0]).longValue();
        try {
            LOGGER.info("准备开始对 seckillId : "+seckillId+" 上锁");
            isLocked = ZkLockUtil.acquire(3,TimeUnit.SECONDS);
            if (isLocked){
                object = point.proceed();
            }
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (isLocked){
                ZkLockUtil.release();
                LOGGER.info("解锁");
            }
        }
        return object;
    }
}
