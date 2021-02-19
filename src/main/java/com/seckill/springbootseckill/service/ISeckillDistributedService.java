package com.seckill.springbootseckill.service;


import com.seckill.springbootseckill.model.Result;

/**
 * @author 迪达勒斯
 */
public interface ISeckillDistributedService {

	/**
	 *
	 * 使用redisson分布式锁实现
	 * @param seckillId 秒杀商品ID
	 * @param userId 用户ID
	 * @return
	 */
	Result startSeckillRedisLock(long seckillId, long userId);
	/**
	 *
	 * @param seckillId 秒杀商品ID
	 * @param userId 用户ID
	 * @return
	 */
	Result startSeckillZksLock(long seckillId, long userId);
	
	/**
	 * 秒杀 二 多个商品
	 * @param seckillId 秒杀商品ID
	 * @param userId 用户ID
	 * @param number 秒杀商品数量
	 * @return
	 */
	Result startSeckillLock(long seckillId, long userId, long number);
	
}
