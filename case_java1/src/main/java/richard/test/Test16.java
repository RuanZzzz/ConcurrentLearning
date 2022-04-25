package richard.test;

import lombok.extern.slf4j.Slf4j;
import richard.demo1.util.Sleeper;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test16")
public class Test16 {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("洗水壶");
            sleep(1);
            log.debug("烧开水");
            sleep(5);
        },"烧香");

        Thread t2 = new Thread(() -> {
            log.debug("洗茶壶");
            sleep(1);
            log.debug("洗茶杯");
            sleep(2);
            log.debug("拿茶叶");
            sleep(1);
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("泡茶");
        },"阿翔");

        t1.start();
        t2.start();
    }

}
