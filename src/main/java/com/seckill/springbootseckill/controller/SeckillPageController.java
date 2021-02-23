package com.seckill.springbootseckill.controller;


import com.seckill.springbootseckill.model.Result;
import com.seckill.springbootseckill.model.Seckill;
import com.seckill.springbootseckill.queue.kafka.KafkaSender;
import com.seckill.springbootseckill.service.ISeckillService;
import com.seckill.springbootseckill.utils.HttpClient;
import com.seckill.springbootseckill.utils.IPUtils;
import com.seckill.springbootseckill.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;


@Api(tags = "秒杀商品")
@RestController
@RequestMapping("/seckillPage")
public class SeckillPageController {

    private final static Logger LOGGER = LoggerFactory.getLogger(SeckillPageController.class);

    @Autowired
	private ISeckillService seckillService;


    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private KafkaSender kafkaSender;
	@Autowired
	private HttpClient httpClient;
	@Value("${qq.captcha.url}")
	private String url;
	@Value("${qq.captcha.aid}")
	private String aid;
	@Value("${qq.captcha.AppSecretKey}")
	private String appSecretKey;
	
	
	@ApiOperation(value = "秒杀商品列表", nickname = "小柒2012")
	@PostMapping("/list")
	public Result list() {
		//返回JSON数据、前端VUE迭代即可
		List<Seckill>  List = seckillService.getSeckillList();
		return Result.ok(List);
	}
	
	@PostMapping("/startSeckill")
    public Result  startSeckill(String ticket,String randstr,HttpServletRequest request) {
        /**
         * 前端通过qq验证发送ticket和randstr，IPUtil通过请求得到ip，
         * 封装后再利用RestTemplate（即这里的httpClient，一般都是前端请求，而通过这后端也可以进行请求）
         * 请求qq验证前端发来的ticket进行二次验证，得到msg，再在这里进行验证，进行相应判断
         */
        HttpMethod method =HttpMethod.POST;
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        params.add("aid", aid);
        params.add("AppSecretKey", appSecretKey);
        params.add("Ticket", ticket);
        params.add("Randstr", randstr);
        params.add("UserIP", IPUtils.getIpAddr());
        String msg = httpClient.client(url,method,params);
        LOGGER.info(msg);
        /**
         * response: 1:验证成功，0:验证失败，100:AppSecretKey参数校验错误[required]
         * evil_level:[0,100]，恶意等级[optional]
         * err_msg:验证错误信息[optional]
         */
        //{"response":"1","evil_level":"0","err_msg":"OK"}
        JSONObject json = JSONObject.parseObject(msg);
        String response = (String) json.get("response");
        if("1".equals(response)){
        	//进入队列、假数据而已
//        	Destination destination = new ActiveMQQueue("seckill.queue");
//        	activeMQSender.sendChannelMess(destination,1000+";"+1);
            LOGGER.info("验证通过");
            kafkaSender.sendMessage("seckillKafka",1000+";"+6);
        	return Result.ok();
        }else{
            LOGGER.info("验证失败");
        	return Result.error("验证失败");
        }
    }

    @ApiOperation(value="最佳实践)",nickname="爪哇笔记")
    @PostMapping("/startRedisCount")
    public Result startRedisCount(long secKillId,long userId){
        /**
         * 原子递减
         */
        long number = redisUtil.decr(secKillId+"-num",1);
        if(number>=0){
            seckillService.startSeckilDBPCC_TWO(secKillId, userId);
            LOGGER.info("用户:{}秒杀商品成功",userId);
        }else{
            LOGGER.info("用户:{}秒杀商品失败",userId);
        }
        return Result.ok();
    }
}
