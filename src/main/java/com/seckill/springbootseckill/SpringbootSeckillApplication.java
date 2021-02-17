package com.seckill.springbootseckill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * @author 迪达勒斯
 */
@EnableAsync
@SpringBootApplication
public class SpringbootSeckillApplication {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringbootSeckillApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SpringbootSeckillApplication.class, args);
        LOGGER.info("项目启动");
    }

}
