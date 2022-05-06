package richard.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test22_2")
public class Test22_2 {
    private static ReentrantLock lock = new ReentrantLock();
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("尝试获得锁");
            try {
                if (!lock.tryLock(2, TimeUnit.SECONDS)) {
                    log.debug("获取不到锁");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            // 如果获取到锁
            try {
                // 临界区代码
                log.debug("获取到锁");
            } finally {
                lock.unlock();
            }
        }, "t1");

        lock.lock();
        log.debug("获取到锁");
        t1.start();
        sleep(1);
        lock.unlock();
        log.debug("释放锁");
    }
}
