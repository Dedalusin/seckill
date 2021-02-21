package com.seckill.springbootseckill.queue.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author 迪达勒斯
 */
@Component

public class KafkaSender {
    //这个kafkatemplate不知道为什么会标红，但经过测试没有问题
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void sendMessage(String channel, String message){
        kafkaTemplate.send(channel, message);
    }

}
