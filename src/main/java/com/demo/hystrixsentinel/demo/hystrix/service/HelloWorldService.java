package com.demo.hystrixsentinel.demo.hystrix.service;

/**
 * @author:ben.gu
 * @Date:2019/11/3 12:04 PM
 */
public interface HelloWorldService {


    String openCache(String string);


    void flushCache(String string);
}
