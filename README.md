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





## 线程运行原理

### 栈与栈帧

Java Virtual Machine Stacks（Java虚拟机栈）

每个线程启动后，虚拟机就会为其分配一块栈内存

- 每个栈由多个栈帧（栈帧[Frame]：**每个栈内线程调用一次方法就会产生栈帧内存**）组成，对应着每次方法调用时所占用的内存

```java
public static void main(String[] args) {
    method1(10);
}

private static void method1(int x) {
    int y = x + 1;
    Object m = method2();
    System.out.println(m);
}

private static Object method2() {
    Object n = new Object();
    return n;
}
```

每次调用一个方法就会产生一个栈帧，此处会依次产生3块栈帧：

> method2->method1->main(先进后出)

- 每个线程只能有一个活动栈帧，对应着当前正在执行的方法



两个线程：

```java
public static void main(String[] args) {
    Thread t1 = new Thread(){
        @Override
        public void run() {
            method1(20);
        }
    };
    t1.setName("t1");
    t1.start();
    method1(10);
}

private static void method1(int x) {
    int y = x + 1;
    Object m = method2();
    System.out.println(m);
}

private static Object method2() {
    Object n = new Object();
    return n;
}
```

> 通过debug可以得知，栈帧是以线程为单位，相互独立



### 线程上下文切换（Thread Context Switch）

因为以下一些原因导致cpu不再执行当前的线程，转而执行另一个线程的代码

- 线程的CPU时间片用完
- 垃圾回收（会暂停当前所有的工作线程）
- 有更高优先级的线程需要运行
- 线程自己调用了sleep、yield、wait、join、park、synchronized、lock等方法

当Context Switch发生时，需要由操作系统保存当前线程的状态，并恢复另一个线程的状态，Java中对应的概念就是程序计数器（Program Counter Register），它的作用是记住下一条jvm指令的执行地址，是线程私有的

- 状态包括程序计数器、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等
- Context Switch频繁发生会影响性能



## 常见的方法

| 方法名           | static | 功能说明                                                     | 注意                                                         |
| ---------------- | ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| start()          |        | 启动一个新线程，在新的线程运行<br />run方法中的代码          | start方法只是让线程进入就绪，里面的代码不一定立刻运行<br />（CPU的时间片还没有分给它）。每个线程对象的start方法只<br />能调用一次，如果调用了多次会出现<br />**<font color=red>IllegalThreadStateException</font>** |
| run()            |        | 新线程启动后会调<br />用的方法                               | 如果在构造Thread对象时传递了Runnable参数，则线程启<br />动后会调用Runnable中的run方法，否则默认不执行任何操<br />作。但可以创建Thread的子类对象，来覆盖默认行为 |
| join()           |        | 等待线程运行结束                                             |                                                              |
| join(long n)     |        | 等待线程运行结束，最多等待n毫秒                              |                                                              |
| getId()          |        | 获取线程长整形的id                                           | id唯一                                                       |
| getName()        |        | 获取线程名                                                   |                                                              |
| setName(String)  |        | 修改线程名                                                   |                                                              |
| getPriority()    |        | 获取线程优先级                                               |                                                              |
| setPriority(int) |        | 修改线程优先级                                               | java中规定线程优先级是1~10的整数，较大的优先级能提高<br />该线程被CPU调度的几率 |
| getState()       |        | 获取线程状态                                                 | java中线程状态是用6个enum表示，分别为：NEW,<br />RUNNABLE,BLOCKED,WAITING,TIMED_WAITING,<br />TERMINATED |
| isInterrupted()  |        | 判断是否被打断                                               | 不会清除打断标记                                             |
| isAlive()        |        | 线程是否存活（还没有运行完毕）                               |                                                              |
| interrupt()      |        | 打断线程                                                     | 如果被打断线程正在sleep，wait，join会导致被打断的线程<br />抛出InterruptedException，并**清除打断标记**；如果打断的正<br />在运行的线程，则会**设置打断标记**；park的线程被打断，也<br />会设置打断标记 |
| interrupted()    | static | 判断当前线程是否被打断                                       | 会清除打断标记                                               |
| currentThread()  | static | 获取当前正在执行的线程                                       |                                                              |
| sleep(long n)    | static | 让当前执行的线程休眠n毫秒，休眠时<br />让出cpu的时间片给其他线程 |                                                              |
| yield()          | static | 提示线程调度器让出当前线程对CPU<br />的使用                  | 主要是为了测试和调试                                         |



## star与run

直接调用run：

```java
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
```

输出：

> 09:35:44.049 c.Test4 [main] - running ...
> 09:35:44.051 c.FileReader [main] - read [test.mp3] start ...
> 09:35:44.066 c.FileReader [main] - read [test.mp3] end ... cost: 14 ms
> 09:35:44.066 c.Test4 [main] - do other thing

并不是起新的线程去执行，而还是主线程去调用相当于实例化的t1中的run()方法，此时 `FileReader.read()` 还是同步的



线程的状态：

```java
public static void main(String[] args) {
    Thread t1 = new Thread("t1") {
        @Override
        public void run() {
            log.debug("running ...");
        }
    };

    System.out.println(t1.getState());
    t1.start();
    System.out.println(t1.getState());
}
```

输出：

> NEW						// 表示新线程
> RUNNABLE			// 表示线程启动了，可以被CPU调度执行
> 09:40:14.804 c.Test5 [t1] - running ...

假如多次 `start()` 就会报异常 **Exception in thread "main" java.lang.IllegalThreadStateException**， 因此当线程变成 Runnable状态（也就是start()过后），就不能再继续调用start()了



##  sleep与yield

CPU时间片会优先分配给yield后Runnable的线程，而不会分配给Timed Waiting的线程，sleep的线程需要休眠结束后才会分配给线程



### sleep

1、调用sleep会让当前线程从 Running 进入 Timed Waiting 状态（阻塞）

```java
public static void main(String[] args) {
    Thread t1 = new Thread("t1"){
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    t1.start();
    log.debug("t1 state：{}", t1.getState());

    // sleep在哪个线程调用，就让哪个线程休眠
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    log.debug("t1 state：{}", t1.getState());
}
```

输出：

> 09:53:39.270 c.Test6 [main] - t1 state：RUNNABLE
> 09:53:39.773 c.Test6 [main] - t1 state：TIMED_WAITING



2、其他线程可以使用 interrupt 方法打断正在睡眠的线程，这时 sleep 方法会抛出 InterruptedException

```java
public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread("t1") {
        @Override
        public void run() {
            log.debug("enter sleep...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.debug("wake up...");
                e.printStackTrace();
            }
        }
    };
    t1.start();

    Thread.sleep(1000);
    log.debug("interrupt...");
    t1.interrupt();
}
```

输出：

> 09:59:00.213 c.Test7 [t1] - enter sleep...
> 09:59:01.222 c.Test7 [main] - interrupt...
> 09:59:01.222 c.Test7 [t1] - wake up...
> java.lang.InterruptedException: sleep interrupted
> 	at java.lang.Thread.sleep(Native Method)
> 	at richard.test.Test7$1.run(Test7.java:13)



3、睡眠结束后的线程未必会立刻得到执行

4、建议使用 TimeUnit 的 sleep 代替 Thread 的 sleep 来获得更好的可读性

```java
TimeUnit.SECONDS.sleep(1);
```



### yield

1、调用 yield 会让当前线程从Running 进入 Runnable 就绪状态，然后调度执行其他线程

2、具体的实现依赖于**操作系统的任务调度器**



### 线程优先级

1、线程优先级会提示（hint）调度器优先调度该线程，但它仅仅是一个提示，调度器可以忽略它

2、如果CPU比较忙，那么优先级高的线程会获得更多的时间片，但CPU闲时，优先级几乎没作用

```java
public static void main(String[] args) {
    Runnable task1 = () -> {
        int count = 0;
        for (; ;) {
            System.out.println("----->1 " + count++);
        }
    };
    Runnable task2 = () -> {
        int count = 0;
        for (; ;) {
            //Thread.yield();
            System.out.println("         ----->2 " + count++);
        }
    };

    Thread t1 = new Thread(task1, "t1");
    Thread t2 = new Thread(task2, "t2");
    t1.setPriority(Thread.MIN_PRIORITY);
    t2.setPriority(Thread.MAX_PRIORITY);

    t1.start();
    t2.start();
}
```



### sleep应用—防止CPU占用 100%

#### sleep实现

在没有利用CPU来计算时，不要让while(true)空转浪费CPU，这时可以使用yield或sleep来让出CPU的使用权给其他程序

```java
while(true) {
    try {
        Thread.sleep(50);
    }catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

- 可以用wait或条件变量来达到类似的效果
- 不同的是，后两种都需要加锁，并且需要相应的唤醒操作，一般适用于要进行同步的场景
- sleep适用于无锁同步的场景



## join方法

```java
public class Test10 {
    static int r = 0;

    public static void main(String[] args) throws InterruptedException {
        test1();
    }

    private static void test1() throws InterruptedException {
        log.debug("开始");
        Thread t1 = new Thread(() -> {
            log.debug("开始");
            sleep(1);
            log.debug("结束");
            r = 10;
        }, "t1");

        t1.start();
        log.debug("结果为：{}",r);
        log.debug("结束");
    }
}
```

r的结果为 **0**



原因：

- 因为主线程和线程t1是并行执行的，t1线程需要1秒之后才能算出r=10
- 而主线程一开始就要打印r的结果，所以只能打印出r=0

解决办法

- 用sleep的可行性（不太好）
- 用join，加在t1.start()之后即可

```java
t1.start();
// 主线程等待t1线程的结束
t1.join();
log.debug("结果为：{}",r);
log.debug("结束");
```

输出：

> 17:57:15.082 c.Test10 [main] - 开始
> 17:57:15.106 c.Test10 [t1] - 开始
> 17:57:16.109 c.Test10 [t1] - 结束
> 17:57:16.109 c.Test10 [main] - 结果为：10			// 正确的结果
> 17:57:16.110 c.Test10 [main] - 结束



