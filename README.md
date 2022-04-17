**并发学习**



# 进程与线程

## 应用

### 应用之异步调用

涉及包：`richard.demo1`

**同步**：需要等待返回结果，才能继续运行的就称为同步

```java
public static void main(String[] args) {
    FileReader.read(Constants.MP4_FULL_PATH);   // 同步
    log.debug("do other things ...");
}
```

结果：

> 17:27:46.884 c.FileReader [main] - read [test.mp3] start ...
> 17:27:46.900 c.FileReader [main] - read [test.mp3] end ... cost: 16 ms
> 17:27:46.900 c.Sync [main] - do other things ...



**异步**：不需要等待结果返回，就能继续运行就是异步

```java
public static void main(String[] args) {
    // 放在线程中去执行
    new Thread(() -> FileReader.read(Constants.MP4_FULL_PATH)).start();
    log.debug("do other things ...");
}
```

结果：

> 17:26:46.828 c.Async [main] - do other things ...
> 17:26:46.828 c.FileReader [Thread-0] - read [test.mp3] start ...
> 17:26:46.843 c.FileReader [Thread-0] - read [test.mp3] end ... cost: 15 ms



### 应用之提高效率

