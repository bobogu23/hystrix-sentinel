package com.demo.hystrixsentinel.demo.hystrix.service.impl;

import com.demo.hystrixsentinel.demo.hystrix.service.HelloWorldService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author:ben.gu
 * @Date:2019/11/3 12:05 PM
 */
@Service
public class HelloWorldServiceImpl implements HelloWorldService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @CacheResult(cacheKeyMethod = "getCacheKey")
    @HystrixCommand(commandKey = "cache1")
    @Override
    public String openCache(String string) {
        return UUID.randomUUID().toString();
    }

    @CacheRemove(commandKey = "cache1", cacheKeyMethod = "getCacheKey")
    @HystrixCommand
    @Override
    public void flushCache(String string) {
        logger.error("flushCache!!!!");
    }

    /**
     * 方法签名与@CacheResult 注解方法的入参一致，返回参数类型string
     * @param string
     * @return
     */
    public String getCacheKey(String string){
        return string;
    }
}
