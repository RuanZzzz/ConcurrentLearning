package richard.test;

import lombok.extern.slf4j.Slf4j;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test32")
public class Test32 {
    // 加了volatile后，就不再去缓存中读取，而总是会去主内存中读取
    volatile static boolean run = true;

    // 锁对象
    final static Object lock = new Object();

    public static void main(String[] args) {
        Thread t = new Thread(() -> {
//            while (true) {
//                synchronized (lock) {
//                    if (!run) {
//                        break;
//                    }
//                }
//            }
            while (run) {
                //
            }
        });
        t.start();

        sleep(1);
        log.debug("停止 t");
        run = false;
//        synchronized (lock) {
//            run = false;
//        }
    }
}


