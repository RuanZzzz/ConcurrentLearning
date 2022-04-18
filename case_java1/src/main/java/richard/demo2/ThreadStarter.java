package richard.demo2;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j(topic = "c.ThreadStarter")
public class ThreadStarter {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 构造方法的参数是给线程指定名字
        Thread t1 = new Thread("t1") {
            @Override
            // run 方法内实现了要执行的任务
            public void run() {
                log.debug("hello");
            }
        };
        t1.start();

    }
}
