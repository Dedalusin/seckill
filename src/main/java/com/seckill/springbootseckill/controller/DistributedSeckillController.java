package com.seckill.springbootseckill.controller;

import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.queue.kafka.KafkaSender;
import com.seckill.springbootseckill.service.ISeckillDistributedService;
import com.seckill.springbootseckill.service.ISeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

/**
 * @author 迪达勒斯
 */
@RestController
@Api
@RequestMapping("/distributedSeckill")
public class DistributedSeckillController {

    private final static Logger LOGGER = LoggerFactory.getLogger(DistributedSeckillController.class);

    private final static int corePoolSize = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize + 1, 10,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
    @Autowired
    ISeckillDistributedService seckillDistributedService;
    @Autowired
    ISeckillService seckillService;
    @Autowired
    KafkaSender kafkaSender;

    @ApiOperation(value = "分布式秒杀一(基于Redisson实现)")
    @PostMapping("/startRedissonLock")
    public Result startRessionLock(Long seckillId){
        seckillService.deleteSeckill(seckillId);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        LOGGER.info("开始秒杀");
        for (int i=1; i <=10; i++){
            final long userId = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result = seckillDistributedService.startSeckillRedisLock(seckillId,userId);
                    LOGGER.info("用户:{}{}",userId,result.get("msg"));
                    countDownLatch.countDown();
                }
            };
            executor.execute(task);
        }
        try {
            countDownLatch.await();
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "分布式秒杀二(基于zookeeper实现)")
    @PostMapping("/startZkLock")
    public Result startZkLock(Long seckillId){
        seckillService.deleteSeckill(seckillId);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        LOGGER.info("开始秒杀");
        for (int i=1; i <=10; i++){
            final long userId = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result = seckillDistributedService.startSeckillZkLock(seckillId,userId);
                    LOGGER.info("用户:{}{}",userId,result.get("msg"));
                    countDownLatch.countDown();
                }
            };
            executor.execute(task);
        }
        try {
            countDownLatch.await();
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.ok();
    }
    @ApiOperation(value = "分布式秒杀三(基于kafka队列实现)")
    @PostMapping("/startKafkaQueue")
    public Result startKafkaQueue(Long seckillId){
        seckillService.deleteSeckill(seckillId);

        LOGGER.info("开始秒杀");
        for (int i=1; i <=10; i++){
            final long userId = i;
            Runnable task =  () -> {
                try {
                    kafkaSender.sendMessage("seckillKafka",seckillId+";"+userId);
                }catch (Exception e) {
                    e.printStackTrace();
                }

            };
            executor.execute(task);
        }
        try {
            //这里使用countDownLatch进行同步没用了，consumer那边是另外的线程了，所以这里使用sleep后再进行查询
            Thread.sleep(3000);
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.ok();
    }
}
