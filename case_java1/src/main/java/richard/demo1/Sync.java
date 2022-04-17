package richard.demo1;


import lombok.extern.slf4j.Slf4j;
import richard.Constants;
import richard.demo1.util.FileReader;

/**
 * 同步等待
 */
@Slf4j(topic = "c.Sync")
public class Sync {

    public static void main(String[] args) {
        FileReader.read(Constants.MP4_FULL_PATH);   // 同步
        log.debug("do other things ...");
    }

}
