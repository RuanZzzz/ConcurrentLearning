package richard.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test1")
public class Test1 {
    public static void main(String[] args) {
        // 构造方法的参数是给线程指定名字
        Thread t1 = new Thread() {
            @Override
            // run 方法内实现了要执行的任务
            public void run() {
                log.debug("hello");
            }
        };
        t1.setName("t1");
        t1.start();

        log.debug("running");
    }
}
