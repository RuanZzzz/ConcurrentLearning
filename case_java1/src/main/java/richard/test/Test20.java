package richard.test;

import lombok.extern.slf4j.Slf4j;
import richard.demo1.util.Downloader;
import richard.demo1.util.Sleeper;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j(topic = "c.Test20")
// 这里的邮递员和居民是一一对应的关系，一个邮递员给一个居民送信
public class Test20 {
    public static void main(String[] args) {
        // 居民收信
        for (int i = 0; i < 3; i++) {
            new People().start();
        }
        Sleeper.sleep(1);
        for (Integer id : MailBoxes.getIds()) {
            new PostMan(id, "内容" + id).start();
        }
    }
}

// 居民
@Slf4j(topic = "c.People")
class People extends Thread {
    @Override
    public void run() {
        // 收信
        GuardedObject guardedObject = MailBoxes.createGuardedObject();
        log.debug("开始收信 id:{}",guardedObject.getId());
        Object mail = guardedObject.get(5000);
        log.debug("收到信 id:{},内容:{}",guardedObject.getId(),mail);
    }
}

// 邮递员
@Slf4j(topic = "c.PostMan")
class PostMan extends Thread{
    // 邮件id
    private int id;
    private String mail;

    public PostMan(int id, String mail) {
        this.id = id;
        this.mail = mail;
    }

    @Override
    public void run() {
        // 发信
        GuardedObject guardedObject = MailBoxes.getGuardedObject(id);
        log.debug("送信 id:{},内容:{}",id,mail);
        guardedObject.complete(mail);
    }
}

class MailBoxes {
    private static Map<Integer,GuardedObject> boxes = new Hashtable<>();

    private static int id = 1;
    // 产生唯一id
    public static synchronized int generateId() {
        return id++;
    }

    public static GuardedObject getGuardedObject(int id) {
        return boxes.remove(id);
    }

    public static GuardedObject createGuardedObject() {
        GuardedObject go = new GuardedObject(generateId());
        boxes.put(go.getId(),go);
        return go;
    }

    public static Set<Integer> getIds() {
        return boxes.keySet();
    }
}

// 增加超市效果
class GuardedObject {
    // 标识 Guarded Object
    private int id;
    public GuardedObject(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    // 结果
    private Object response;
    // 获取结果
    public Object get(long timeout) {
        synchronized (this) {
            // 开始时间
            long begin = System.currentTimeMillis();
            // 经历的时间
            long passedTime = 0;
            // 还没有结果
            while (response == null) {
                // 应该等待的时间
                long waitTime = timeout - passedTime;
                // 经历的时间超过了设置的最大等待时间，退出循环
                if (waitTime <= 0) {
                    break;
                }
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 求得经历时间
                passedTime = System.currentTimeMillis() - begin;
            }
            return response;
        }
    }

    // 产生结果
    public void complete(Object response) {
        synchronized (this) {
            // 给结果成员变量赋值
            this.response = response;
            this.notifyAll();
        }
    }
}
