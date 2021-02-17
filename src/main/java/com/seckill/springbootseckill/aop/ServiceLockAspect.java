package com.seckill.springbootseckill.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 迪达勒斯
 * 用order保证上锁切面对于所有切面优先运行
 */
@Aspect
@Component
@Scope
@Order(1)
public class ServiceLockAspect {

    private static Lock lock = new ReentrantLock(true);
    private final static Logger logger = LoggerFactory.getLogger(ServiceLockAspect.class);

    @Pointcut("@annotation(com.seckill.springbootseckill.annotation.ServiceLock)")
    public void servicePoint(){}

    @Around("servicePoint()")
    public Object serviceLock(ProceedingJoinPoint point){
        //公平锁
        Object object;
        logger.info("Aop准备上锁");
        lock.lock();
        try {
            //因为是实现了代理，所以要将被代理对象的返回结果返回上级
            object = point.proceed();
        }catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            lock.unlock();
            logger.info("aop释放锁");
        }
        return object;
    }
}
