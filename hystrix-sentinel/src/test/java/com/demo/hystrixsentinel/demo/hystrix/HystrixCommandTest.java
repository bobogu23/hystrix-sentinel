package com.demo.hystrixsentinel.demo.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author:ben.gu
 * @Date:2019/10/27 8:35 PM
 */
public class HystrixCommandTest {

    private static final HystrixCommand.Setter cachedSetter =
            HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
//                    andThreadPoolPropertiesDefaults(new HystrixThreadPoolProperties.Setter().)
                    .andCommandPropertiesDefaults( HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3000))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld"));

    public  static  class HelloWorldHystrixCommand extends HystrixCommand {
        private final String name;
        public HelloWorldHystrixCommand(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
//            super(cachedSetter);

            this.name = name;
        }
        @Override
        protected String run() {
            System.err.println("HelloWorldHystrixCommand current thread name:"+Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello " + name;
        }
    }


    public  static  class FailHystrixCommand extends HystrixCommand<String> {
        private final String name;
        public FailHystrixCommand(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
            this.name = name;
        }
        @Override
        protected String run() {
            throw new RuntimeException("this command always fails");
        }

        @Override protected String getFallback() {
            return "Hello Failure " + name + "!";
        }
    }


    public  static  class CacheHystrixCommand extends HystrixCommand<String> {
        private final String name;
        public CacheHystrixCommand(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
            this.name = name;
        }
        @Override
        protected String run() {
           return name;
        }

        @Override protected String getCacheKey() {
            return name;
        }
    }

    public  static  class HelloWorldHystrixObservableCommand extends HystrixObservableCommand {
        private final String name;
        public HelloWorldHystrixObservableCommand(String name) {
            super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
            this.name = name;
        }

        @Override
        protected Observable construct() {

            return Observable.create(new SyncOnSubscribe<String,String>(){
                @Override
                protected String generateState() {
                    return "hello:";
                }

                @Override
                protected  String next(String state, Observer<? super String> observer) {

                    System.err.println("HelloWorldHystrixObservableCommand current thread name:"+Thread.currentThread().getName());

                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    observer.onNext(state+" "+name+"!");

                    observer.onCompleted();
                    return "haha";
                }
            });
        }
    }

    /**
     * 同步执行。execute 会启动新线程
     */
    @Test
    public void testSynchronizeHystrixCommand() {
        System.err.println(" current thread name:" + Thread.currentThread().getName());
        String result = (String) new HelloWorldHystrixCommand("HLX").execute();
        System.out.println("result" + result);  // 打印出Hello HLX
    }

    @Test
    public void testSynchronizeHystrixObservableCommand() throws ExecutionException, InterruptedException {

        Object result = new HelloWorldHystrixObservableCommand("xxaaa").toObservable().toBlocking().toFuture()
                .get();
        System.out.println("result-->" + result);
        Thread.sleep(10000);
    }



    @Test
    public void testASynchronizeHystrixCommand() throws ExecutionException, InterruptedException {
        System.err.println(" current thread name:" + Thread.currentThread().getName());
        Future future = new HelloWorldHystrixCommand("HLX").queue();
        System.out.println("wait..." );

        System.out.println("result" + future.get());  // 打印出Hello HLX
    }

    @Test
    public void testHystrixCommandFallBack() throws ExecutionException, InterruptedException {
        String result = new FailHystrixCommand("ccc").execute();

        System.out.println("result-->" + result);
    }


    @Test
    public void testHystrixCommandCache()  {

        //初始化上下文
        HystrixRequestContext context = HystrixRequestContext.initializeContext();

        CacheHystrixCommand hystrixCommand1 = new CacheHystrixCommand("bbb");

        CacheHystrixCommand hystrixCommand2 = new CacheHystrixCommand("bbb");

        hystrixCommand1.execute();

        assertTrue(!hystrixCommand1.isResponseFromCache());


        hystrixCommand2.execute();

        assertTrue(hystrixCommand2.isResponseFromCache());

        context.shutdown();

    }

    public static  class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>, String, Integer> {

        private final Integer key;

        public CommandCollapserGetValueForKey(Integer key) {
            this.key = key;
        }

        @Override
        public Integer getRequestArgument() {
            return key;
        }

        @Override
        protected HystrixCommand<List<String>> createCommand(final Collection<CollapsedRequest<String, Integer>> requests) {
            return new BatchCommand(requests);
        }

        @Override
        protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> requests) {
            int count = 0;
            for (CollapsedRequest<String, Integer> request : requests) {
                request.setResponse(batchResponse.get(count++));
            }
        }

        private static final class BatchCommand extends HystrixCommand<List<String>> {
            private final Collection<CollapsedRequest<String, Integer>> requests;

            private BatchCommand(Collection<CollapsedRequest<String, Integer>> requests) {
                super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                        .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKey")));
                this.requests = requests;
            }

            @Override
            protected List<String> run() {
                ArrayList<String> response = new ArrayList<String>();
                for (CollapsedRequest<String, Integer> request : requests) {
                    // artificial response for each argument received in the batch
                    response.add("ValueForKey: " + request.getArgument());
                }
                return response;
            }
        }
    }

    @Test
    public void testCollapser() throws Exception {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Future<String> f1 = new CommandCollapserGetValueForKey(1).queue();
            Future<String> f2 = new CommandCollapserGetValueForKey(2).queue();
            Future<String> f3 = new CommandCollapserGetValueForKey(3).queue();
            Future<String> f4 = new CommandCollapserGetValueForKey(4).queue();

            assertEquals("ValueForKey: 1", f1.get());
            assertEquals("ValueForKey: 2", f2.get());
            assertEquals("ValueForKey: 3", f3.get());
            assertEquals("ValueForKey: 4", f4.get());

            // assert that the batch command 'GetValueForKey' was in fact
            // executed and that it executed only once
            assertEquals(1, HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size());
            HystrixCommand<?> command = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().toArray(new HystrixCommand<?>[1])[0];
            // assert the command is the one we're expecting
            assertEquals("GetValueForKey", command.getCommandKey().name());
            // confirm that it was a COLLAPSED command execution
            assertTrue(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED));
            // and that it was successful
            assertTrue(command.getExecutionEvents().contains(HystrixEventType.SUCCESS));
        } finally {
            context.shutdown();
        }
    }

    public static void main(String args[]) {

        double v = Math.random() ;
        System.err.println("v->"+v );
    }
}
