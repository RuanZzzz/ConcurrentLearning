package richard.demo3;

import lombok.extern.slf4j.Slf4j;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.TestWaitNotify")
public class TestWaitNotify {
    final static Object obj = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (obj) {
                log.debug("执行");
                try {
                    obj.wait();     // 让线程在 obj 上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                log.debug("执行其他代码");
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj) {
                log.debug("执行");
                try {
                    obj.wait();     // 让线程在 obj 上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                log.debug("执行其他代码");
            }
        }, "t2").start();

        // 主线程两秒后执行
        sleep(2);
        log.debug("唤醒 obj 上其它线程");
        synchronized (obj) {
            obj.notify();   // 唤醒 obj 上一个线程
            //obj.notifyAll();    // 唤醒 obj 上所有等待线程
        }
    }
}
