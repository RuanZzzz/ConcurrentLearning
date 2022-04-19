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





# Java线程

## 创建和运行线程

### **方法一：直接使用Thread**

```java
// 创建线程对象
Thread t = new Thread() {
    public void run() {
        // 要执行的任务
    }
}
```

例如：

```java
// richard.test.Test1
public static void main(String[] args) throws ExecutionException, InterruptedException {
    // 构造方法的参数是给线程指定名字
    Thread t1 = new Thread() {
        @Override
        // run 方法内实现了要执行的任务（重写）
        public void run() {
            log.debug("hello");
        }
    };
    t1.setName("t1");
    t1.start();

}
```

输出：

> 23:12:48.117 c.ThreadStarter [t1] - hello



### **方法二：使用Runnable配合Thread**

把【线程】和【任务（要执行的代码）】分开

- Thread 代表线程
- Runnable 可运行的任务（线程要执行的代码）

```java
Runnable runnable = new Runnable() {
    public void run() {
        // 要执行的任务
    }
};
// 创建线程对象
Thread t = new Thread(runnable);
// 启动线程
t.start();
```

例如：

```java
// richard.test.Test2
public static void main(String[] args) {
    // 任务对象
    Runnable r = new Runnable() {
        @Override
        public void run() {
            log.debug("running");
        }
    };

    // 参数1 是任务对象， 参数2 是线程名字
    Thread t = new Thread(r, "t2");
    t.start();
}
```

输出：

> 23:24:16.936 c.Test2 [t2] - running

Java8以后可以使用lambda精简代码：

```java
// 创建任务对象
Runnable task2 = () -> log.debug("hello");

// 参数1 是任务对象， 参数2 是线程名字
Thread t = new Thread(r, "t2");
t.start();
```

注意：

①、查看Runnable接口可以得知，该接口仅有一个抽象方法，并在最开始加上了注解 `@FunctionalInterface` ，就可以使用lambda表达式进行精简

②、快捷键，光标放到 `new Runnable()` 处，使用 Alt + Enter快速生成

```java
Runnable r = () -> log.debug("running");
Runnable r = () -> {log.debug("running");};	// 如果有多行逻辑，最外层需要再加一对 {};
```



### 原理之 Thread 与 Runnable 的关系

小结：

①、方法1 是把线程和任务合并在了一起；方法2 是把线程和任务分开了

②、用Runnable 更容易与线程池等高级API配合

③、用Runnable 让任务类脱离了 Thread 继承体系，更灵活



### 方法三：FutureTask配合Thread

FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况

```java
// 创建任务对象
FutureTask<Integer> task3 = new FutureTask<>(() -> {
    log.debug("hello");
    return 100;
});

new Thread(task3, "t3").start();

Integer result = task3.get();
log.debug("结果是:{}"，result);
```

例如：

```java
public static void main(String[] args) throws ExecutionException, InterruptedException {
    FutureTask<Integer> task = new FutureTask<>(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            log.debug("running ...");
            // 睡1s
            Thread.sleep(1000);
            return 100;
        }
    });

    Thread t1 = new Thread(task, "t1");
    t1.start();

    // 主线程阻塞，同步等待 task 执行完毕的结果
    log.debug("{}",task.get());
}
```

输出：

> 00:03:08.757 c.Test3 [t1] - running ...
>
> 00:03:09.767 c.Test3 [main] - 100（等了1s后才会显示）





## 观察多个线程同时运行

```java
public static void main(String[] args) {
    new Thread(() -> {
        while (true) {
            log.debug("running ...");
        }
    }, "t1").start();

    new Thread(() -> {
        while (true) {
            log.debug("running ...");
        }
    }, "t2").start();
}
```

交替执行





## 查看进程线程的方法

### Windows

- `tasklist` 查看进程
- `taskkill` 杀死进程



### Linux

- `ps -fe` 查看所有进程
- `ps -fT -p <PID>` 查看某个进程（PID）的所有进程
- `kill` 杀死进程
- `top` 按大写H切换是否显示线程
- `top -H -p <PID>` 查看某个进程（PID）的所有线程



### Java

- `jps` 命令查看所有Java进程
- `jstack <PID>` 查看某个Java进程的所有线程状态
- `jconsole` 来查看某个Java进程中线程的运行情况（图形界面）
