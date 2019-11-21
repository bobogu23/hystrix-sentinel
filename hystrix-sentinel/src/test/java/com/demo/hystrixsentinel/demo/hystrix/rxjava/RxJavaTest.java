package com.demo.hystrixsentinel.demo.hystrix.rxjava;

import org.junit.Test;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.internal.schedulers.ImmediateScheduler;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.TimeUnit;

/**
 * rxjava:
 * a library for composing asynchronous and event-based programs using observable sequences for the Java VM
 * 一个JAVA 虚拟机上 使用observable序列 合并异步和基于事件的程序 库
 *
 * 观察者模式：
 *角色：
 * Observable(被观察者)、Observer(观察者)
 * Subscriber(订阅者)、Subject
 *
 * Observable，Subject 是生产者
 *
 * Subscriber，Observer是消费者
 *
 * Observer 通过subscribe ()方法订阅Observable
 *
 * 回调方法：
 * onNext(T item)
 * Observable 通过这个方法发射(emit)数据，方法的参数就是Observable发射的数据。该方法可以调用多次
 *
 *onComplete
 正常终止，如果没有遇到错误，Observable在最后一次调用onNext之后调用此方法
 *
 *onError(Exception ex)
 * 当Observable遇到错误或者无法返回期望的数据时会调用这个方法.
 * 这个调用会终止Observable，后续不会再调用onNext和onCompleted，onError方法的参数是抛出的异常
 *
 * @author:ben.gu
 * @Date:2019/11/3 9:45 PM
 */
public class RxJavaTest {

    @Test public void createObservable() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                //                System.err.println("OnSubscribe,thread name :"+ Thread.currentThread().getName());
                subscriber.onNext("1");
                subscriber.onNext("2");

                subscriber.onCompleted();

            }
        }).subscribe(new Subscriber<String>() {
            @Override public void onCompleted() {
                //                System.err.println("Subscriber,thread name :"+ Thread.currentThread().getName());

                System.err.println("completed");
            }

            @Override public void onError(Throwable e) {
                System.err.println("error:" + e.getMessage());

            }

            @Override public void onNext(String s) {
                //                System.err.println("Subscriber,thread name :"+ Thread.currentThread().getName());

                System.err.println("data:" + s);
            }
        });
    }

    @Test public void createObservableDefer() throws InterruptedException {
        Observable<String> observable = Observable.defer(new Func0<Observable<String>>() {
            @Override public Observable<String> call() {
                return Observable.just("hello");
            }
        });
        System.err.println("sleep...");
        Thread.sleep(2000);

        observable.subscribeOn(new Scheduler() {
            @Override public Worker createWorker() {
                return new Worker() {
                    @Override public void unsubscribe() {

                    }

                    @Override public boolean isUnsubscribed() {
                        return false;
                    }

                    @Override public Subscription schedule(Action0 action) {
                        action.call();
                        return Subscriptions.unsubscribed();
                    }

                    @Override public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
                        return null;
                    }
                };
            }
        }).subscribe(new Action1<String>() {
            @Override public void call(String s) {
                System.err.println("what is this ?:" + s);
            }
        });
    }


    @Test
    public void testWindowFlatMap() throws InterruptedException {
        Observable
                .interval(1, TimeUnit.SECONDS) // 为了简化，假设每秒发射一个球
                .window(2, TimeUnit.SECONDS) // 2秒钟一个桶
                .flatMap(b -> b.reduce(0, (x, y) -> ++x))
                .window(3, 1) // 3个桶一个窗口
                .flatMap(w -> w.reduce((x, y) -> x + y))
                .subscribe(s -> System.out.println("window sum: " + s));

        Thread.sleep(10000);
    }
}
