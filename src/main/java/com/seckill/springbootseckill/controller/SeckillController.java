package com.seckill.springbootseckill.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.service.ISeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.*;

/**
 * @author 迪达勒斯
 */
@Api("秒杀")
@RestController
@RequestMapping("/seckill")
public class SeckillController {
    private final static Logger LOGGER = LoggerFactory.getLogger(SeckillController.class);

    /**
     * 核心线程数,根据已有资源,cpu核数等情况得到
     */
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 创建线程池时最好使用带factory参数的构造函数，这样可以便于我们出错时定位
     */
    private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("test-pool-%d").build();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,corePoolSize+1, 10L, TimeUnit.SECONDS,
    new LinkedBlockingDeque<>(1000), namedThreadFactory);
    @Autowired
    ISeckillService seckillService;

    @ApiOperation(value = "秒杀一(没有锁,会出现超卖)")
    @PostMapping("/startOne")
    public Result startOne(long seckillId){
        int people = 1000;
        //便于同步的信号量
        final CountDownLatch latch = new CountDownLatch(people);
        seckillService.deleteSeckill(seckillId);
        final long killId = seckillId;
        LOGGER.info("秒杀一开始");
        /**
         * 开启新线程之前，将RequestAttributes对象设置为子线程共享
         * 这里仅仅是为了测试，否则 IPUtils 中获取不到 request 对象
         * 用到限流注解的测试用例，都需要加一下两行代码
         */
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(sra, true);
        /**
         * 这里的for能够模拟，因为我们使用的是线程池，并不是单一线程获取阻塞队列中的元素
         */
        for (int i = 1; i <= people; i++){
            long userId = i;
            Runnable task = () -> {
                try {
                    Result result = seckillService.startSeckil(seckillId,userId);
                    if(result!=null){
                        LOGGER.info("用户:{}{}",userId,result.get("msg"));
                    }else{
                        LOGGER.info("用户:{}{}",userId,"哎呦喂，人也太多了，请稍后！");
                    }
                }catch (Exception e ){
                    e.printStackTrace();
                }
                latch.countDown();

            };
            executor.execute(task);
        }
        try {
            //保证全面的线程全部处理完成
            latch.await();
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.ok();
    }
}
