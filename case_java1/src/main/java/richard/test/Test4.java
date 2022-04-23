package richard.test;

import lombok.extern.slf4j.Slf4j;
import richard.Constants;
import richard.demo1.util.FileReader;

@Slf4j(topic = "c.Test4")
public class Test4 {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("running ...");
                FileReader.read(Constants.MP4_FULL_PATH);
            }
        };

        t1.run();
        log.debug("do other thing");
    }
}
