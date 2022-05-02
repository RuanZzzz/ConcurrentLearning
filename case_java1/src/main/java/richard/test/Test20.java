package richard.test;

import lombok.extern.slf4j.Slf4j;
import richard.demo1.util.Downloader;
import richard.demo1.util.Sleeper;

import java.io.IOException;
import java.util.List;

@Slf4j(topic = "c.Test20")
public class Test20 {
    // 线程1 等待 线程2的下载结果
    public static void main(String[] args) {
        GuardedObject guardedObject = new GuardedObject();
        new Thread(() -> {
            // 等待结果
            log.debug("begin");
            Object object = guardedObject.get(2000);
            log.debug("结果是：{}",object);
        }, "t1").start();

        new Thread(() -> {
            log.debug("begin");
            Sleeper.sleep(1);
            guardedObject.complete(new Object());
        }, "t2").start();

    }
}

// 增加超市效果
class GuardedObject {
    // 结果
    private Object response;

    // 获取结果
    public Object get(long timeout) {
        synchronized (this) {
            // 开始时间
            long begin = System.currentTimeMillis();
            // 经历的时间
            long passedTime = 0;
            // 还没有结果
            while (response == null) {
                // 应该等待的时间
                long waitTime = timeout - passedTime;
                // 经历的时间超过了设置的最大等待时间，退出循环
                if (waitTime <= 0) {
                    break;
                }
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 求得经历时间
                passedTime = System.currentTimeMillis() - begin;
            }

            return response;
        }
    }

    // 产生结果
    public void complete(Object response) {
        synchronized (this) {
            // 给结果成员变量赋值
            this.response = response;
            this.notifyAll();
        }
    }
}
