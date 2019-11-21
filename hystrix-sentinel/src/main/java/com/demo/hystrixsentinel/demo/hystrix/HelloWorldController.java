package com.demo.hystrixsentinel.demo.hystrix;

import com.demo.hystrixsentinel.demo.hystrix.service.HelloWorldService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;

/**
 * @author:ben.gu
 * @Date:2019/11/3 10:49 AM
 */
@RestController
public class HelloWorldController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HelloWorldService helloWorldService;


    @RequestMapping(value = "/hello")
    @HystrixCommand(fallbackMethod = "fallbackHello", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "2000")
    },commandKey = "hello")
    @ResponseBody
    public Object hello() throws InterruptedException {
        double random = Math.random();
        if(random > 0.5 ){
            Thread.sleep(3000);
        }
        return "hello world";
    }

    @RequestMapping(value = "/hello1")
    @HystrixCommand(fallbackMethod = "fallbackHello",
    commandKey = "hello1")
    @ResponseBody
    public Object hello1() throws InterruptedException {

        throw new RuntimeException("11");
    }


    @RequestMapping(value = "/helloError")
    @HystrixCommand(fallbackMethod = "fallbackHello1", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "2000")
    },commandKey = "helloError")
    @ResponseBody
    public Object helloError() throws InterruptedException {
         double random = Math.random();
         if(random> 0.5 ){
             throw new RuntimeException("11");
         }
         logger.info("return hello...");
         return "hello";
    }



    @RequestMapping(value = "/helloCache")
    @HystrixCommand(fallbackMethod = "fallbackHello", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000")
    },commandKey = "helloCache")
    @ResponseBody
    public Object helloCache() throws InterruptedException {
        String s1 = helloWorldService.openCache("1");
        String s2 = helloWorldService.openCache("1");
        logger.error("s1-->:{},s2-->:{},s1 equals s2 ?:{}",s1,s2,s1.equals(s2));
        helloWorldService.flushCache("1");
        String s3 = helloWorldService.openCache("2");
        String s4 = helloWorldService.openCache("2");
        logger.error("s3-->:{},s4-->:{},s3 equals s4 ?:{}",s3,s4,s3.equals(s4));
        return s4;
    }



    private String fallbackHello() {
        return "time out !!!";
    }

    private String fallbackHello1() {
        return "demo data";
    }


}
