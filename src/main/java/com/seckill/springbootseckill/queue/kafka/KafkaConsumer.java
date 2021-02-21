package com.seckill.springbootseckill.queue.kafka;

import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.model.SeckillStatEnum;
import com.seckill.springbootseckill.service.ISeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author 迪达勒斯
 * 对于多消息的情况，可以考虑对于每一个partition使用一个线程池对消息进行批量消费，每次poll一部分
 * 这里单服务器情况，所以锁服务也没有使用分布式锁
 */
@Component
public class KafkaConsumer {
    @Autowired
    ISeckillService seckillService;

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    /**
     * 在这里也可以指定partition,Id等信息
     */
    @KafkaListener(topics = {"seckillKafka"})
    public void onMessage(String message){
        try {
            Long seckillId = Long.parseLong(message.split(";")[0]);
            Long userId = Long.parseLong(message.split(";")[1]);
            Result result = seckillService.startSeckilAopLock(seckillId, userId);
            if (result.equals(Result.ok(SeckillStatEnum.SUCCESS))){
                LOGGER.info(userId+" 消费 "+seckillId+" 成功");
            }else {
                LOGGER.info(userId+" 消费 "+seckillId+" 失败");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
