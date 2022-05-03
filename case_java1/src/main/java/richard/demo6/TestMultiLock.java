package richard.demo6;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.TestMultiLock")
public class TestMultiLock {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("start ...");
            sleep(1);
            log.debug("park ...");
            LockSupport.park();
            log.debug("resume");
        }, "t1");
        t1.start();

        sleep(2);
        log.debug("unpark ...");
        LockSupport.unpark(t1);
    }
}
