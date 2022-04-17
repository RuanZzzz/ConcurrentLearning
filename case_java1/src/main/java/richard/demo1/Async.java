package richard.demo1;

import lombok.extern.slf4j.Slf4j;
import richard.Constants;
import richard.demo1.util.FileReader;

/**
 * 异步不等待
 */
@Slf4j(topic = "c.Async")
public class Async {
    public static void main(String[] args) {
        // 放在线程中去执行
        new Thread(() -> FileReader.read(Constants.MP4_FULL_PATH)).start();
        log.debug("do other things ...");
    }
}
