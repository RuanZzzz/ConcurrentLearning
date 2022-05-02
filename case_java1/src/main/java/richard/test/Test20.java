package richard.test;

import lombok.extern.slf4j.Slf4j;
import richard.demo1.util.Downloader;

import java.io.IOException;
import java.util.List;

@Slf4j(topic = "c.Test20")
public class Test20 {
    // 线程1 等待 线程2的下载结果
    public static void main(String[] args) {
        GuardedObject guardedObject = new GuardedObject();
        new Thread(() -> {
            // 等待结果
            log.debug("等待结果");
            List<String> list = (List<String>) guardedObject.get();
            log.debug("结果的大小是：{}",list.size());
        }, "t1").start();

        new Thread(() -> {
            log.debug("执行下载");
            try {
                List<String> list = Downloader.download();
                guardedObject.complete(list);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "t2").start();

    }
}

class GuardedObject {
    // 结果
    private Object response;

    // 获取结果
    public Object get() {
        synchronized (this) {
            // 还没有结果
            while (response == null) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
