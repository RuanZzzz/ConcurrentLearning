package richard.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test13")
public class Test13 {

    public static void main(String[] args) {
        test1();
    }

    public static void test1() {
        Thread t1 = new Thread(() -> {
            log.debug("park ...");
            LockSupport.park();
            log.debug("unpark ...");
            log.debug("打断状态：{}",Thread.interrupted());

            LockSupport.park();
            log.debug("unpark ...");
        }, "t1");

        t1.start();

        sleep(1);
        t1.interrupt();
    }
}
