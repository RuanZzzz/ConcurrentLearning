package richard.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test2")
public class Test2 {
    public static void main(String[] args) {
        // 任务对象
        Runnable r = () -> {log.debug("running");};

        // 参数1 是任务对象， 参数2 是线程名字
        Thread t = new Thread(r, "t2");
        t.start();
    }
}
