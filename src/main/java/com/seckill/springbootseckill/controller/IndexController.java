package com.seckill.springbootseckill.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用访问拦截匹配
 * 创建者 科帮网
 * 创建时间	2018年4月3日
 */
@Controller
public class IndexController {
	private final static Logger LOGGER = LoggerFactory.getLogger(IndexController.class);
	/**
	 * 页面跳转
	 * @param url
	 * @return
	 */
	@RequestMapping("{url}.shtml")
	public String page(@PathVariable("url") String url) {
		LOGGER.info("url: "+url);
		return  url;
	}
	/**
	 * 页面跳转(二级目录)
	 * @param module
	 * @param url
	 * @return
	 */
	@RequestMapping("{module}/{url}.shtml")
	public String page(@PathVariable("module") String module,@PathVariable("url") String url) {
		return module + "/" + url;
	}
	
}
