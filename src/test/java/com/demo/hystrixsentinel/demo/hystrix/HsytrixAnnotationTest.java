package com.demo.hystrixsentinel.demo.hystrix;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author:ben.gu
 * @Date:2019/11/3 1:55 PM
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HsytrixAnnotationTest {



    //http://localhost:8080/hystrix
    //http://localhost:8080/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8080%2Fhystrix.stream
    @Test
    public void testDegrade() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        while (true){
            for(int i = 0;i<10;i++){
                executorService.execute(()->{
                    ResponseEntity<String> responseEntity = new RestTemplate()
                            .getForEntity("http://localhost:8080/helloError", String.class);
                    System.err.println("result->"+responseEntity.getBody());

                });
            }
            Thread.sleep(1000);
        }

    }


    @Test
    public void testTimeout() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        while (true){
            for(int i = 0;i<10;i++){
                executorService.execute(()->{
                    ResponseEntity<String> responseEntity = new RestTemplate()
                            .getForEntity("http://localhost:8080/hello", String.class);
                    System.err.println("result->"+responseEntity.getBody());

                });
            }
            Thread.sleep(300);
        }

    }
}
