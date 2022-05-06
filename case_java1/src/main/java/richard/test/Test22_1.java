package richard.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test22_1")
public class Test22_1 {
    private static ReentrantLock lock =new ReentrantLock();
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            // 第一个 try 仅用于捕获异常
            try {
                // 如果没有竞争 那么此方法就会获取 lock 对象锁
                // 如果有竞争就进入阻塞队列，可以被其它线程用 interrupt 方法打断
                log.debug("尝试获得锁");
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.debug("没有获得锁，返回");
                return;
            }
            try {
                // 临界区
                log.debug("获取到锁");
            } finally {
                // 释放锁
                lock.unlock();
            }
        }, "t1");

        lock.lock();
        t1.start();

        sleep(1);
        log.debug("打断 t1");
        t1.interrupt();
    }
}
