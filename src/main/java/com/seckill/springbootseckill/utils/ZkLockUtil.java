package com.seckill.springbootseckill.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

public class ZkLockUtil {
    private static String address = "127.0.0.1:2181";
    public static CuratorFramework client;

    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        client = CuratorFrameworkFactory.newClient(address,retryPolicy);
        client.start();
    }

    /**
     * 单例模式，使用静态内部类
     */
    private ZkLockUtil(){}
    private static class SingletonHolder{
        /**
         * 在/curator/lock目录下建立一个持久节点，
         * 而后的加锁就是在该节点下再建立临时顺序节点，每一个连接都注册watcher去watch前一个临时顺序节点
         * 解锁后第一个节点的连接会调用删除临时节点，而第二个由于watcher会判断自己是否是第一个了，再获取锁
         * 第三个由于watch第二个，而第二个仍在，所以继续等待，后面的同样如此，不会发生羊群效应
         */
        private  static InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
    }
    public static InterProcessMutex getMutex(){
            return SingletonHolder.mutex;
        }
    //获得了锁
    public static boolean acquire(long time, TimeUnit unit){
        try {
            return getMutex().acquire(time,unit);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //释放锁
    public static void release(){
        try {
            getMutex().release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
