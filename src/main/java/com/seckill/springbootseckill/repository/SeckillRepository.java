package com.seckill.springbootseckill.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.seckill.springbootseckill.model.Seckill;

/**
 * 由约定大于配置，自动根据关键词解析成sql，进行基本的增删查改
 */
public interface SeckillRepository extends JpaRepository<Seckill, Long> {
	
	
}
