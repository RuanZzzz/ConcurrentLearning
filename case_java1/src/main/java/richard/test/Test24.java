package richard.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test24")
public class Test24 {
    static final Object room = new Object();
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;
    static ReentrantLock ROOM = new ReentrantLock();
    // 等待烟的休息室
    static Condition waitCigaretteSet = ROOM.newCondition();
    // 等外卖的休息室
    static Condition waitTakeOutSet = ROOM.newCondition();

    public static void main(String[] args) {

        new Thread(() -> {
            ROOM.lock();
            try {
                log.debug("有烟没？[{}]", hasCigarette);
                while (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        waitCigaretteSet.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("可以开始干活了");
            } finally {
                ROOM.unlock();
            }
        }, "小南").start();

        new Thread(() -> {
            ROOM.lock();
            try {
                log.debug("外卖送到没？[{}]", hasTakeout);
                while (!hasTakeout) {
                    log.debug("没外卖，先歇会！");
                    try {
                        waitTakeOutSet.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("可以开始干活了");
            } finally {
                ROOM.unlock();
            }
        }, "小女").start();

        sleep(1);
        new Thread(() -> {
            ROOM.lock();
            try {
                hasTakeout = true;
                waitTakeOutSet.signal();
            } finally {
                ROOM.unlock();
            }
        }, "送外卖的").start();

        sleep(1);
        new Thread(() -> {
            ROOM.lock();
            try {
                hasCigarette = true;
                waitCigaretteSet.signal();
            } finally {
                ROOM.unlock();
            }
        }, "送烟的").start();
    }
}
