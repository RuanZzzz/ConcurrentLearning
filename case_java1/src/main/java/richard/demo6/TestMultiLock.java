package richard.demo6;

import lombok.extern.slf4j.Slf4j;
import richard.demo1.util.Sleeper;

import java.util.concurrent.locks.LockSupport;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.TestMultiLock")
public class TestMultiLock {
    public static void main(String[] args) {
        BigRoom bigRoom = new BigRoom();
        new Thread(() -> {
            bigRoom.study();
        }, "小南").start();

        new Thread(() -> {
            bigRoom.sleep();
        }, "小女").start();
    }
}

@Slf4j(topic = "c.BigRoom")
class BigRoom {
    private final Object studyRoom = new Object();
    private final Object bedRoom = new Object();

    public void sleep() {
        // synchronized (this)
        synchronized (bedRoom) {
            log.debug("sleeping 2 小时");
            Sleeper.sleep(2);
        }
    }
    public void study() {
        // synchronized (this)
        synchronized (studyRoom) {
            log.debug("study 1 小时");
            Sleeper.sleep(1);
        }
    }
}
