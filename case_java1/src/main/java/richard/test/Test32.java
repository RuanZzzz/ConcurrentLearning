package richard.test;

import lombok.extern.slf4j.Slf4j;

import static richard.demo1.util.Sleeper.sleep;

@Slf4j(topic = "c.Test32")
public class Test32 {
    static boolean run = true;
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            while (run) {
                //
            }
        });
        t.start();

        sleep(1);
        log.debug("停止 t");
        run = false;
    }
}


