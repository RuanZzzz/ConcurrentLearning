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



### 应用之同步

以调用方角度来看：

- 需要等待结果返回，才能继续运行就是同步
- 不需要等待结果返回，就能继续运行就是异步



等待多个结果的例子：

```java
public class TestJoin {
    static int r = 0;
    static int r1 = 0;
    static int r2 = 0;

    public static void main(String[] args) throws InterruptedException {
        test2();
    }

    private static void test2() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            sleep(1);
            r1 = 10;
        });
        Thread t2 = new Thread(() -> {
            sleep(2);
            r2 = 20;
        });
        t1.start();
        t2.start();
        long start = System.currentTimeMillis();
        log.debug("join begin");
        t2.join();
        log.debug("t2 join end");
        t1.join();
        log.debug("t1 join end");
        long end = System.currentTimeMillis();
        log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
    }

}
```

输出：

> 23:42:38.168 c.TestJoin [main] - join begin
> 23:42:40.180 c.TestJoin [main] - t2 join end
> 23:42:40.180 c.TestJoin [main] - t1 join end
> 23:42:40.180 c.TestJoin [main] - r1: 10 r2: 20 cost: 2013

分析：

- 第一个join：等待t1时，t2并没有停止，也在运行
- 第二个join：执行1s后，t2也运行了1s，因此也只需要再等待1s



### 有实效的join

```java
public static void main(String[] args) throws InterruptedException {
    test3();
}

public static void test3() throws InterruptedException {
    Thread t1 = new Thread(() -> {
        sleep(2);
        r1 = 10;
    });

    long start = System.currentTimeMillis();
    t1.start();

    // 线程执行结束会导致join结束
    log.debug("join begin");
    t1.join(1500);
    long end = System.currentTimeMillis();
    log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
}
```

输出：

> 21:16:46.002 c.TestJoin [main] - join begin
> 21:16:47.506 c.TestJoin [main] - r1: 0 r2: 0 cost: 1505

t1.join(1500)：主线程提前结束了，但是线程sleep(2)还没结束，所以主线程打印r1还是0

t1.join(3000)：如果线程睡2s就结束，主线程也不会等满3秒，线程结束，主线程也会提交结束





## interrupt方法

### 打断sleep、wait、join的线程

- **调用sleep、wait、join，都会让线程进入阻塞状态**

打断sleep的线程，会清空打断状态，以sleep为例

```java
public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
        log.debug("sleep");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }, "t1");

    t1.start();
    Thread.sleep(1000);
    log.debug("interrupt");
    t1.interrupt();
    log.debug("打断标记：{}",t1.isInterrupted());
}
```

输出：

> 21:31:08.823 c.Test11 [t1] - sleep
> 21:31:09.822 c.Test11 [main] - interrupt
> 21:31:09.822 c.Test11 [main] - 打断标记：false
> java.lang.InterruptedException: sleep interrupted
> 	at java.lang.Thread.sleep(Native Method)
> 	at richard.test.Test11.lambda$main$0(Test11.java:13)
> 	at java.lang.Thread.run(Thread.java:748)

打断标记为FALSE的原因：wait、sleep、join被打断后，就以异常的方式表示被打断，就会将打断标记置为FALSE



### 打断正常运行的线程

```java
public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
        while (true) {
            boolean interrupted = Thread.currentThread().isInterrupted();
            if (interrupted) {
                log.debug("被打断了");
                break;
            }
        }
    }, "t1");

    t1.start();
    Thread.sleep(1000);
    log.debug("interrupt");
    t1.interrupt();
}
```

输出：

> 21:38:58.900 c.Test12 [main] - interrupt
> 21:38:58.901 c.Test12 [t1] - 被打断了

根据t1线程获取自己线程当前是否被打断的状态，从而停止线程的继续执行



### 模式之两阶段终止

两阶段终止模式：在一个线程T1中如何优雅的终止线程T2？主要是给T2线程一个料理后事的机会

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/24/c657053036b23d88ce9db78a8e51d3f1_1650808704_vX4XyVAHvv.png)



实现：

```java
public class Test1 {
    public static void main(String[] args) throws InterruptedException {
        TwoPhaseTermination tpt = new TwoPhaseTermination();
        tpt.start();

        Thread.sleep(3500);
        tpt.stop();
    }
}

class TwoPhaseTermination {
    private Thread monitor;

    // 启动监控线程
    public void start() {
        monitor = new Thread(() -> {
            while (true) {
                Thread curThread = Thread.currentThread();
                if (curThread.isInterrupted()) {
                    log.debug("料理后事");
                    break;
                }
                try {
                    Thread.sleep(1000);
                    log.debug("执行监控记录");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // 重新设置打断标记
                    curThread.interrupt();
                }
            }
        });

        monitor.start();
    }

    // 停止监控线程
    public void stop() {
        monitor.interrupt();
    }

}
```

输出：

> 22:12:14.976 c.TwoPhaseTermination [Thread-0] - 执行监控记录
> 22:12:15.984 c.TwoPhaseTermination [Thread-0] - 执行监控记录
> 22:12:16.994 c.TwoPhaseTermination [Thread-0] - 执行监控记录
> java.lang.InterruptedException: sleep interrupted
> 	at java.lang.Thread.sleep(Native Method)
> 	at richard.demo3.TwoPhaseTermination.lambda$start$0(Test1.java:30)
> 	at java.lang.Thread.run(Thread.java:748)
> 22:12:17.487 c.TwoPhaseTermination [Thread-0] - 料理后事

这样就可以，可以监控到各种情况下的打断，并在打断后处理逻辑（释放锁之类的），并结束循环



### 打断park线程

```java
public static void main(String[] args) {
    test1();
}

public static void test1() {
    Thread t1 = new Thread(() -> {
        log.debug("park ...");
        LockSupport.park();
        log.debug("unpark ...");
        log.debug("打断状态：{}",Thread.currentThread().isInterrupted());
    }, "t1");

    t1.start();

    sleep(1);
    t1.interrupt();
}
```

输出：

> 22:23:08.469 c.Test13 [t1] - park ...
> 22:23:09.482 c.Test13 [t1] - unpark ...
> 22:23:09.482 c.Test13 [t1] - 打断状态：true

打断之后，可以输出 `park ... ` 之后的逻辑，但之后再使用park就无效了，如果需要继续使用park，则可以这样使用：

```java
log.debug("打断状态：{}",Thread.interrupted());
```



## 不推荐使用的方法

因为方法已过时，也容易破坏同步代码块，造成死锁

stop()：停止线程运行

suspend()：挂起（暂停）线程运行

resume()：恢复线程运行



## 主线程和守护线程

默认情况下，Java进程需要等待所有线程都运行结束，才会结束。有一种特殊的线程叫做守护线程，只要其它非守护线程运行结束了，即使守护线程的代码没有执行完，也会强制结束

```java
public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        log.debug("结束");
    }, "t1");
    // t1 就变成守护线程
    t1.setDaemon(true);
    t1.start();
    Thread.sleep(1000);
    log.debug("结束");
}
```

输出：

> 22:00:34.199 c.Test15 [main] - 结束

t1 为守护线程，当非守护线程（main主线程）运行结束了，即使 t1 没有执行完（while(true)），也会强制结束



**<font color=red>注意</font>**：

- 垃圾回收器线程就是一种守护线程
- Tomcat中 的 Acceptor（接收请求） 和 Poller（分发请求） 线程都是守护线程，所以 Tomcat 接收到shutdown 命令后，不会等待它们处理完当前请求



## 五种状态

从 **操作系统** 层面来看

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/25/5739b161da8ce845e2800fd3a83590e6_1650897285_CrXBO9nWhQ.png)

- 【初始状态】仅是在语言层面创建了线程对象，还未与操作系统线程关联
- 【可运行状态】（就绪状态）指该线程已经被创建（与操作系统线程关联），可以由CPU调度执行
- 【运行状态】指获取了CPU时间片运行中的状态
  - 当CPU时间片用完，会从【运行状态】转换至【可运行状态】，会导致线程的上下文切换
- 【阻塞状态】
  - 如果调用了阻塞API，如BIO读写文件，这时该线程实际不会用到CPU，会导致线程上下文切换，进入【阻塞状态】
  - 等BIO操作完毕，会由操作系统唤醒阻塞的线程，转换至【可运行状态】
  - 与【可运行状态】的区别是，对【阻塞状态】的线程来说只要它们一直不唤醒，调度器就一直不会考虑调度它们
- 【终止状态】表示线程已经执行完毕，生命周期已经结束，不会再转换为其它状态



## 六种状态

从 Java API层面来描述

根据Thread.State 枚举，分为六种状态

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/25/a32bf8e38f91a41b74e6456b307ae4fc_1650898518_Js1A242gvj.png)

- NEW 线程刚被创建，但还没有调用 `start()` 方法

- RUNNABLE 当调用 start() 方法之后
  - 注意：Java API 层面的 RUNNABLE 状态涵盖了 操作系统 层面的 【可运行状态】、【运行状态】和【阻塞状态】（由于BIO导致的线程阻塞，在Java里无法区分，仍然认为是可运行）
- BLOCKED、WAITING、TIMED_WAITING都是 Java API层面对【阻塞状态】的细分
- TERMINATED 当线程代码运行结束



### 代码实现

```java
public static void main(String[] args) {
    Thread t1 = new Thread("t1") {
        @Override
        public void run() {
            log.debug("running...");
        }
    };

    Thread t2 = new Thread("t2") {
        @Override
        public void run() {
            while(true) { // runnable

            }
        }
    };
    t2.start();

    Thread t3 = new Thread("t3") {
        @Override
        public void run() {
            log.debug("running...");
        }
    };
    t3.start();

    Thread t4 = new Thread("t4") {
        @Override
        public void run() {
            synchronized (TestState.class) {
                try {
                    Thread.sleep(1000000); // timed_waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    t4.start();

    Thread t5 = new Thread("t5") {
        @Override
        public void run() {
            try {
                t2.join(); // waiting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    t5.start();

    Thread t6 = new Thread("t6") {
        @Override
        public void run() {
            synchronized (TestState.class) { // blocked
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    t6.start();

    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    log.debug("t1 state {}", t1.getState());
    log.debug("t2 state {}", t2.getState());
    log.debug("t3 state {}", t3.getState());
    log.debug("t4 state {}", t4.getState());
    log.debug("t5 state {}", t5.getState());
    log.debug("t6 state {}", t6.getState());
}
```

输出：

> 23:17:47.164 c.TestState [main] - t1 state NEW
> 23:17:47.165 c.TestState [main] - t2 state RUNNABLE
> 23:17:47.165 c.TestState [main] - t3 state TERMINATED
> 23:17:47.165 c.TestState [main] - t4 state BLOCKED
> 23:17:47.165 c.TestState [main] - t5 state WAITING
> 23:17:47.165 c.TestState [main] - t6 state TIMED_WAITING



# **共享模型之管程**

## 共享带来的问题

### Java例子

```java
public class Test17 {
    static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter ++;
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter --;
            }
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        log.debug("{}",counter);
    }
}
```

输出：

> 21:58:43.660 c.Test17 [main] - 407



**<font color=red>原因</font>**：

以上的结果存在正数、负数、0,。因为Java中对静态变量的自增，自减并不是原子操作，从字节码来进行分析

例如对于 `i++` 而言（i为静态变量），实际会产生如下的JVM字节码命令：

```java
getstatic		i	// 获取静态变量i的值
iconst_1			// 准备常量1
iadd				// 自增
putstatic		i	// 将修改后的值存入静态变量i
```

而对应 `i--` 也是类似

```java
getstatic		i	// 获取静态变量i的值
iconst_1			// 准备常量1
isub				// 自减
putstatic		i	// 将修改后的值存入静态变量i
```

而Java的内存模型如下，完成静态变量的自增，自减需要在主存和工作内存中进行数据交换：

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/26/975a45d73e048aec9b393518af07ed3f_1650982214_h7zEPeoZMw.png)

如果是单线程以上带吧是顺序执行（不会交错）没有问题：

但因为是多线程，就可能交错运行，结果也会出现负数：

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/26/6338db92be55cf2a0a0e4bdb391d9901_1650983020_FVgVIDElyw.png)



### 临界区Critical Section

- 多线程上下文切换的问题出在多个线程访问 **共享资源**，当多个线程对 **共享资源** 读写操作时发生指令交错，就会出现问题
- 一段代码块内如果存在对 **共享资源** 的多线程读写操作，称这段代码块为 **临界区**

如：

```java
static int counter = 0;

static void increment()
// 临界区
{
    counter ++;
}

static void decrement()
// 临界区
{
    counter --;
}
```



### 竞态条件 Race Condition

多个线程在临界区内执行，由于代码的 **执行序列不同** 而导致结果无法预测，称之为发生了 **竞态条件**



## synchronized 解决方案

### 应用之互斥

为了避免临界区的竞态条件发生，有多重手段可以达到目的

- 阻塞式的解决方案：synchronized，Lock
- 非阻塞式的解决方案：原子变量



synchronized的使用：

synchronized又称【对象锁】，它采用互斥的方式让同一时刻至多只有一个线程能持有 【对象锁】，其他线程再想获取这个【对象锁】时就会阻塞住、这样就能保证拥有锁的线程可以安全的执行临界区内的代码，不用担心线程上下文切换

**<font color=red>注意</font>**：

虽然Java中互斥和同步都可以采用 synchronized 关键字来完成，但它们还是有区别的：

- 互斥是保证临界区的竞态条件发生，同一时刻只能有一个线程执行临界区代码
- 同步是由于线程执行的先后、顺序不同、需要一个线程等待其他线程运行到某个点



### synchronized

语法：

```java
synchronized(对象)
{
    临界区
}
```

解决

```java
public class Test17 {
    static int counter = 0;
    static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (lock) {
                    counter ++;
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (lock) {
                    counter --;
                }
            }
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        log.debug("{}",counter);
    }
}
```

图解：

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/27/a36a1afcd65b59634893f4d74445d0a1_1651066756_UJ3Pe7QyG6.png)

结论：

synchronized 实际是用 **对象锁** 保证了 **临界区内代码的原子性** ，临界区内的代码对外是不可分割的，不会被线程切换所打断的



### 面向对象改进

把需要保护的共享变量放入一个类

```java
public class Test17 {
    public static void main(String[] args) throws InterruptedException {
        Room room = new Room();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                room.increment();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                room.decrement();
            }
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        log.debug("{}",room.getCounter());
    }
}

class Room {
    private int counter = 0;

    public void increment() {
        synchronized (this) {
            counter ++;
        }
    }

    public void decrement() {
        synchronized (this) {
            counter --;
        }
    }

    public int getCounter() {
        synchronized (this) {
            return counter;
        }
    }
}
```



## 方法上的synchronized

```java
class Test {
    public synchronized void test() {
        
    }
}
// 等价于
class Test {
    public void test() {
        synchronized (this) {
            
        }
    }
}
```

```java
class Test {
    public synchronized static void test() {
        
    }
}
// 等价于
class Test {
    public static void test() {
        synchronized (Test.class) {
            
        }
    }
}
```

因此，之前的例子可以改为：

```java
class Room {
    private int counter = 0;

    public synchronized void increment() {
            counter ++;
    }

    public synchronized void decrement() {
            counter --;
    }

    public synchronized int getCounter() {
        return counter;
    }
}
```



### “线程八锁”

情况1：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n1.b();
        }).start();
    }
}

class Number{
    public synchronized void a() {
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
```

输出：`1、2` 或 `2、1`



情况2：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n1.b();
        }).start();
    }
}

class Number{
    public synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
```

输出： **1s 后** `1、2`；`2` **1s后**  `1`



情况3：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n1.b();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n1.c();
        }).start();
    }
}

class Number{
    public synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
    public void c() {
        log.debug("3");
    }
}
```

输出：

`3` **1s后**  `1、2` 或

2、3 **1s后** 1 或

3、2 **1s后** 1

注意：没有1、3的原因是，a方法会睡1秒，这时候3是永远没有获取锁的状态，所以会抢占1之前出来



情况4：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        Number n2 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n2.b();
        }).start();

    }
}

@Slf4j(topic = "c.Number")
class Number{
    public synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
```

输出：

永远都是先输出2，1s后再输出1



情况5：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n1.b();
        }).start();

    }
}

@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
```

输出：

总是先输出2，再输出1

因为a锁住的类对象，b锁住的是n1，因此它们锁住的不是同一个对象



情况6：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n1.b();
        }).start();

    }
}

@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public static synchronized void b() {
        log.debug("2");
    }
}
```

输出： **1s 后** `1、2`；`2` **1s后**  `1`

都是锁的类对象



情况7：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        Number n2 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n2.b();
        }).start();

    }
}

@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
```

输出：

总是先输出2，再输出1

锁的不是同一个对象



情况8：

```java
public class Test8Locks {
    public static void main(String[] args) {
        Number n1 = new Number();
        Number n2 = new Number();
        new Thread(() -> {
            log.debug("begin");
            n1.a();
        }).start();
        new Thread(() -> {
            log.debug("begin");
            n2.b();
        }).start();

    }
}

@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    } 
    public static synchronized void b() {
        log.debug("2");
    }
}
```

输出： **1s 后** `1、2`；`2` **1s后**  `1`

虽然有两个对象，但都是锁的类对象



## 变量的线程安全分析

### 成员变量和静态变量是否线程安全

- 如果他们没有共享，则线程安全
- 如果他们被共享了，根据他们的状态是否能够改变，又分成两种情况
  - 如果只有读操作，则线程安全
  - 如果有读写操作，则这段代码是临界区，需要考虑线程安全



### 局部变量是否线程安全

- 局部变量是线程安全
- 但局部变量引用的对象则未必
  - 如果该对象没有逃离方法的作用访问，它是线程安全的
  - 如果该对象逃离方法的作用范围，需要考虑线程安全



```java
public static void test1() {
    int i = 10;
    i ++;
}
```

每个线程调用 `test1()` 方法时局部变量 i，会在每个线程的栈帧内存中创建多份，因此不存在共享

```java
public static void test1();
	descriptor: ()V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
		stack=1, locals=1, args_size=0
            0: bipush		10
            2: istore_0
            3: iinc         0, 1
            6: return
        LineNumberTable:
			line 10: 0
            line 11: 3
            line 12: 6
        LocalVariableTable:
			Start Length Slot Name Signature
                3	   4    0    i  I     
```



成员变量的例子：

```java
public class TestThreadSafe {
    static final int THREAD_NUMBER = 2;
    static final int LOOP_NUMBER = 200;
    public static void main(String[] args) {
        ThreadUnsafe test = new ThreadUnsafe();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            new Thread(() -> {
                test.method1(LOOP_NUMBER);
            }, "Thread" + (i+1)).start();
        }
    }
}

class ThreadUnsafe {
    ArrayList<String> list = new ArrayList<>();
    public void method1(int loopNumber) {
        for (int i = 0; i < loopNumber; i++) {
            method2();
            method3();
        }
    }

    private void method2() {
        list.add("1");
    }

    private void method3() {
        list.remove(0);
    }
}
```

输出：

> Exception in thread "Thread1" Exception in thread "Thread2" java.lang.ArrayIndexOutOfBoundsException: -1
> 	at java.util.ArrayList.remove(ArrayList.java:501)
> 	at richard.demo3.ThreadUnsafe.method3(TestThreadSafe.java:32)
> 	at richard.demo3.ThreadUnsafe.method1(TestThreadSafe.java:23)
> 	at richard.demo3.TestThreadSafe.lambda$main$0(TestThreadSafe.java:12)
> 	at java.lang.Thread.run(Thread.java:748)
> java.lang.ArrayIndexOutOfBoundsException: -1
> 	at java.util.ArrayList.add(ArrayList.java:459)
> 	at richard.demo3.ThreadUnsafe.method2(TestThreadSafe.java:28)
> 	at richard.demo3.ThreadUnsafe.method1(TestThreadSafe.java:22)
> 	at richard.demo3.TestThreadSafe.lambda$main$0(TestThreadSafe.java:12)
> 	at java.lang.Thread.run(Thread.java:748)

以上就是成员变量，没有考虑线程安全，分别进行读写



但是将上述的list改为局部变量，就不会产生问题

```java
class ThreadSafe {
    public final void method1(int loopNumber) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < loopNumber; i++) {
            method2(list);
            method3(list);
        }
    }

    public void method2(ArrayList<String> list) {
        list.add("1");
    }

    private void method3(ArrayList<String> list) {
        list.remove(0);
    }
}
```

结论：

- list 是局部变量，每个线程调用时会创建其不同实例，没有共享
- 而 method2 的参数是从 method1 中传递过来的，与 method1 中引用同一个对象
- method3 的参数与method2 同理



引用暴露：

```java
public class TestThreadSafe {
    static final int THREAD_NUMBER = 2;
    static final int LOOP_NUMBER = 200;
    public static void main(String[] args) {
        ThreadSafeSubClass test = new ThreadSafeSubClass();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            new Thread(() -> {
                test.method1(LOOP_NUMBER);
            }, "Thread" + (i+1)).start();
        }
    }
}

class ThreadSafe {
    public final void method1(int loopNumber) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < loopNumber; i++) {
            method2(list);
            method3(list);
        }
    }

    public void method2(ArrayList<String> list) {
        list.add("1");
    }

    public void method3(ArrayList<String> list) {
        list.remove(0);
    }
}

class ThreadSafeSubClass extends ThreadSafe{
    @Override
    public void method3(ArrayList<String> list) {
        new Thread(() -> {
            list.remove(0);
        }).start();
    }
}
```

此时会造成线程安全



可以借助private和final提供安全，防止重写

```java
class ThreadSafe {
    public final void method1(int loopNumber) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < loopNumber; i++) {
            method2(list);
            method3(list);
        }
    }

    private void method2(ArrayList<String> list) {
        list.add("1");
    }

    private void method3(ArrayList<String> list) {
        list.remove(0);
    }
}
```



### 常见线程安全类

- String
- Integer
- StringBuffer
- Random
- Vector
- Hashtable
- java.util.concurrnt 包下的类

这里的线程安全是指，多个线程调用它们同一个实例的某个方法时，是线程安全的。也可以理解为：

①、它们的每个方法是原子的

②、**但是** 它们多个方法的组合不是原子的



#### 线程安全类方法的组合

``` java
// 不是在一个方法内执行的
Hashtable table = new Hashtable();
// 线程1，线程2
if (table.get("key") == null) {
    table.put("key", value);
}
```

以上就会出现线程安全的问题，过程如下

```java
sequenceDiagram
participant t1 as 线程1
participant t2 as 线程2
participant table
t1 ->> table : get("key") == null
t2 ->> table : get("key") == null
t2 ->> table : put("key",v2)
t1 ->> table : put("key",v1)
```



#### 不可变类线程安全性

String、Integer 等都是不可变类，因为其内部的状态不可以改变，因此它们的方法都是线程安全的



#### 有趣的例子

例1：

```java
public class MyServlet extends HttpServlet {
    Map<String,Object> map = new HashMap<>();	// 非线程安全
    String S1 = "test";							// 线程安全，String不可变
    final String S2 = "test";					// 线程安全
    Date D1 = new Date();						// 非线程安全
    final Date D2 = new Date();					// 非线程安全（引用值不能变，里面的属性可以改变）
    
    public void doGet(HttpServletRequest request, HttpServeltResponse response) {
        // 使用上面的变量
    }
}
```



例2：

```java
// 非线程安全
public class MyServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();
    
    public void doGet(HttpServletRequest request, HttpServeltResponse response) {
        userService.update();
    }
}

public class UserServiceImpl implements UserService {
    // 记录调用次数
    private int count = 0;
    
    public void update() {
        count ++;
    }
}
```



例3：

```java
// 非线程安全
@Aspect
@Component
public class MyAspect {
    private long start = 0L;
    
    @Before("execution(* *(..))")
    public void before() {
        start = System.nanoTime();
    }
    
    @After("execution(* *(..))")
    public void after() {
        long end = System.nanoTime();
        System.out.println("cost time:" + (end - start));
    }
    
}
```



例4：

```java
public class MyServlet extends HttpServlet {
    // 是线程安全（虽然有成员变量，但是是私有的，也没有其他途径去修改该成员变量）
    private UserService userService = new UserServiceImpl();

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        userService.update(...);
    }
}
public class UserServiceImpl implements UserService {
    // 是线程安全（没有可修改的共享变量）
    private UserDao userDao = new UserDaoImpl();

    public void update() {
        userDao.update();
    }
}
public class UserDaoImpl implements UserDao {
    public void update() {
        String sql = "update user set password = ? where username = ?";
        // 是线程安全
        try (Connection conn = DriverManager.getConnection("","","")){
            // ...
        } catch (Exception e) {
            // ...
        }
    }
}
```



例5：

```java
// 不是线程安全，它的Connection是共享变量，会被多个线程修改，存在问题
public class MyServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        userService.update(...);
    }
}
public class UserServiceImpl implements UserService {
    private UserDao userDao = new UserDaoImpl();

    public void update() {
        userDao.update();
    }
}
public class UserDaoImpl implements UserDao {
    private Connection conn = null;
    public void update() throws SQLException {
        String sql = "update user set password = ? where username = ?";
        conn = DriverManager.getConnection("","","");
        // ...
        conn.close();
    }
}
```



例6：

```java
// 线程安全
public class MyServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        userService.update(...);
    }
}
public class UserServiceImpl implements UserService {
    public void update() {
        // 因为此处是每次new出来，并不是直接使用的内部的成员变量
        UserDao userDao = new UserDaoImpl();
        userDao.update();
    }
}
public class UserDaoImpl implements UserDao {
    private Connection = null;
    public void update() throws SQLException {
        String sql = "update user set password = ? where username = ?";
        conn = DriverManager.getConnection("","","");
        // ...
        conn.close();
    }
}
```



例7：

```java
public abstract class Test {
    public void bar() {
        // 是否安全
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        foo(sdf);
    }
    public abstract foo(SimpleDateFormat sdf);
    public static void main(String[] args) {
        new Test().bar();
    }
}
```

其中 `foo` 的行为是不确定的，可能导致不安全的发生，被称之为 **外形方法**

```java
public void foo(SimpleDateFormat sdf) {
    String dateStr = "1999-10-11 00:00:00";
    for (int i = 0; i < 20; i++) {
        new Thread(() -> {
            try {
                sdf.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```



完成经典售票例子：

```java
public class ExerciseSell {

    // Random 为线程安全
    static Random random = new Random();

    // 随机 1~5
    public static int randomAmount() {
        return random.nextInt(5) + 1;
    }

    public static void main(String[] args) throws InterruptedException {
        // 模拟多人买票
        TicketWindow window = new TicketWindow(1000);

        // 所有线程的集合
        List<Thread> threadList = new ArrayList<>();
        // 卖出的票数统计
        List<Integer> amountList = new Vector<>();
        for (int i = 0; i < 50000; i++) {
           Thread thread = new Thread(() -> {
               // 买票
               int amount = window.sell(randomAmount());
               // 统计买票数
               amountList.add(amount);
           });
           threadList.add(thread);
           thread.start();
        }

        // 让主线程等待所有线程的执行完毕
        for (Thread thread : threadList) {
            thread.join();
        }

        // 统计卖出的票数和剩余的票数
        log.debug("余票：{}",window.getCount());
        log.debug("卖出的票数：{}",amountList.stream().mapToInt(i -> i).sum());
    }
}


// 售票窗口
class TicketWindow {
    // 剩余的票的数量
    private int count;

    public TicketWindow(int count) {
        this.count = count;
    }

    // 获取余票数量
    public int getCount() {
        return count;
    }

    /**
     * 售票
     * @param amount        购买数量
     * @return              0：没卖出去
     */
    public synchronized int sell(int amount) {
        if (this.count >= amount) {
            this.count -= amount;
            return amount;
        }else {
            return 0;
        }
    }
}
```

输出：

> 11:15:20.779 c.ExerciseSell [main] - 余票：0
> 11:15:20.785 c.ExerciseSell [main] - 卖出的票数：1000



## Monitor 概念

### Java对象头

以32位虚拟机为例

普通对象

```ruby
|--------------------------------------------------------------|
| 					Object Header (64 bits) 				   |
|------------------------------------|-------------------------| 
| 				 Mark Word (32 bits) |    Klass Word (32 bits) |    
|------------------------------------|-------------------------|
```

Klass Word：指针，找到类对象



数组对象

```java
|---------------------------------------------------------------------------------|
| 							Object Header (96 bits) 							  |
|--------------------------------|-----------------------|------------------------|
| 		       Mark Word(32bits) |    Klass Word(32bits) |   array length(32bits) |
|--------------------------------|-----------------------|------------------------|
```



其中Mark Word 结构为

```ruby
|--------------------------------------------------------------------|--------------------|
|												 Mark Word (32 bits) | 				State |
|--------------------------------------------------------------------|--------------------|
|             hashcode:25 | hashcode:31 | age:4 | biased_lock:0 | 01 | 			   Normal |
|--------------------------------------------------------------------|--------------------|
| 		           thread:23 | epoch:2  | age:4 | biased_lock:1 | 01 | 			   Biased |
|--------------------------------------------------------------------|--------------------|
| 										  ptr_to_lock_record:30 | 00 | Lightweight Locked |
|--------------------------------------------------------------------|--------------------|
| 								  ptr_to_heavyweight_monitor:30 | 10 | Heavyweight Locked |
|--------------------------------------------------------------------|--------------------|
| 																| 11 | 		Marked for GC |
|--------------------------------------------------------------------|--------------------|
        
 /*
  * 01 没加锁
  * 00 轻量级锁	 ptr_to_lock_record:30—>轻量级锁记录地址 
  * 10 重量级锁  ptr_to_heavyweight_monitor:30—>重量级锁记录地址 
  */
```



### Monitor(锁)

Monitor：称为 **监视器** 或 **管程**

每个Java对象都可以关联一个 monitor 对象，如果使用 synchronized 给对象上锁（重量级）之后，该对象头的 Mark Word 中就被设置指向 Monirot 对象的指针

Monitor 结构如下

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/8de37710641f2478d45003eb1ad237ca_1651302146_PgNhcA2c4v.png)

- 刚开始 Monitor 中的 Owner 为 null
- 当 Thread-2 执行 synchronized(obj) 就会将 Monitor 的所有者 Owner 置为 Thread-2，Monitor 中只能有一个 Owner
- 当 Thread-2 上锁的过程中，如果 Thread-3、Thread4、Thread5也来执行 synchronized(obj)，就会进入 EntryList BLOCKED
- Thread-2 执行完同步代码块的内容，然后唤醒 EntryList 中等待的线程来竞争锁，竞争时是非公平的
- 图中的 WaitSet 中的 Thread-0、Thread-1 是之前获得过锁，但条件不满足进入 WAITING 状态的线程

> **<font color=red>注意</font>**：
>
> - synchronized 必须是进入同一个对象的 monitor 才有上述的效果
> - 不加 synchronized 的对象不会关联监听器，不遵从以上规则



## synchronized 原理

```java
static final Object lock = new Object();
static int counter = 0;

public static void main(String[] args) {
    synchronized (lock) {
        counter ++;
    }
}
```

对应的字节码

```java
public static void main(java.lang.String[]);
	descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=1
         0: getstatic		#2				// <- lock 引用（synchronized 开始）
         3: dup
         4: astore_1						// lock 引用 -> slot 1
         5: monitorenter					// 将lock对象 MaarkWord 置为 Monitor 指针
         6: getstatic		#3				// <- i
         9: iconst_1						// 准备常数1
        10: iadd							// +1
        11: putstatic       #3              // -> i
        14: aload_1          				// <- lock 引用
        15: monitorexit						// 将 lock 对象 MarkWord 重置，唤醒 EntryList
        16: goto			24				
        19: astore_2						// e-> slot 2
        20: aload_1							// <- lock 引用
        21: monitorexit						// 将 lock 对象 MarkWord 重置，唤醒 EntryList
        22: aload_2							// <- slot 2 (e)
       	23: athrow							// throw e
        24: return
     // 检测的异常范围             
     Exception table:
        from	to	target	type
           6    16      19   any
          19    22      19   any
     LineNumberTable:
         line 8: 0
         line 9: 6
         line 10: 14
         line 11: 24
 	 LocalVariableTable:
 		Start Length Slot Name Signature
 			0  25     0   args [Ljava/lang/String;
 	StackMapTable: number_of_entries = 2
 		frame_type = 255 /* full_frame */
 			offset_delta = 19
     	    locals = [ class "[Ljava/lang/String;", class java/lang/Object ]
     		stack = [ class java/lang/Throwable ]
 		frame_type = 250 /* chop */
 			offset_delta = 4               
```



### 轻量级锁

轻量级锁的使用场景：如果一个对象虽然有多线程访问，但多线程访问的时间是错开的（也就是没有竞争），那么可以使用轻量级锁来优化。

轻量级锁对使用者是透明的，即语法仍然是 `synchronized`

假设有两个方法同步块，利用一个对象加锁

```java
static final Object obj = new Object();
public static void method1() {
    synchronized(obj) {
        // 同步块A
        method2();
    }
}

public static void method2() {
    synchronized(obj) {
        // 同步块B
    }
}
```



1、**<font color=red>创建锁记录（Lock Record）对象</font>**，每个线程的栈帧都会包含一个锁记录的结构，内部可以存储锁定对象的Mark Word

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/dff4e56f717d60465ebb0da1cdf6d862_1651314600_M1GeYQzebl.png)

2、让锁记录中 Object reference **指向锁对象**，并常使用 **cas 替换 Object 的 Mark Word**（就是加锁），将 Mark Word的值存入锁记录

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/ded59466bdda9adc712b4a6d34c03c3f_1651314774_w2H30ma7MX.png)

3、如果 cas 替换成功，对象头中存储了 **锁记录地址** 和 状态 **00**，表示由该线程给对象加锁，如下图所示

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/a13f34b36ad630c6fa9fd7212edfa877_1651319705_fwJ68xAYzE.png)

- 如果 cas 失败，有两种情况
  - 如果是其他线程已经持有了该 Object 的轻量级锁，这时表明有竞争，进入锁膨胀过程
  - 如果是自己执行了 synchronized 锁重入，那么再添加一条 Lock Record 作为重入的记数（存储Mark Word的地方，就只是存个null，用来占位记数使用）

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/50d9f307c0e8d53af36a6be65311a309_1651320176_m3yVILlkUZ.png)

4、当退出 synchronized 代码块（解锁时）如果有取值为 null 的锁记录，表示有重入，这是重置锁记录，表示重入记数减一

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/723aa14e6b5f520ee465d1cd8c295f10_1651320396_DcXlgWPg7D.png)

5、当退出 synchronized 代码块（解锁时）锁记录的值不为null，这时使用 cas 将 Mark Word 的值恢复给对象头

- 成功：解锁成功
- 失败：说明轻量级锁进行了锁膨胀或已经升级为重量级锁，进入重量级锁解锁流程



### 膨胀锁

如果在尝试加轻量级锁的过程中，CAS 操作无法成功，这时一种情况就是有其他线程为此对象加上了轻量级锁（有竞争），这时需要进行膨胀锁，将轻量级锁变为重量级锁

```java
static Object obj = new Object();
public static void method1() {
    synchronized( obj ) {
        // 同步块
    } 
}
```

1、当 Thread-1 进行轻量级加锁时，Thread-0 已经对该对象加了轻量级锁

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/1a05ac8b19a1f036ad191d02375c4923_1651322186_ZfhBwv77xp.png)

2、这时 Thread-1 加轻量级锁失败，进入锁膨胀流程

- 即为 Object 对象申请 Monitor 锁，让 Object 执行重量级锁地址
- 然后自己进入 Monitor 的 EntryList BLOCKED

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/183575631056f57c5591a9c1f9ac16b9_1651322357_YK6AAnmlFf.png)

3、当 Thread-0 退出同步块解锁时，使用 cas 将 Mark Word 的值恢复给对象头，失败。这时会进入重量级解锁流程，即按照 Monitor 地址找到 Monitor 对象，将Owner 置为null，唤醒EntryList 中 BLOCKED 的线程



### 自旋优化

重量级锁竞争的时候，还可以使用自旋来进行优化，如果当前线程自旋成功（即这时候持锁线程已经退出了同步块，释放了锁），这时当前线程就可以避免阻塞。

(多核CPU下才有意义)



自旋重试成功的情况：

| 线程1（CPU1 上）        | 对象Mark               | 线程2（CPU2 上）        |
| ----------------------- | ---------------------- | ----------------------- |
| -                       | 10（重量锁）           | -                       |
| 访问同步块，获取monitor | 10（重量锁）重量锁指针 | -                       |
| 成功（加锁）            | 10（重量锁）重量锁指针 | -                       |
| 执行同步块              | 10（重量锁）重量锁指针 | -                       |
| 执行同步块              | 10（重量锁）重量锁指针 | 访问同步块，获取monitor |
| 执行同步块              | 10（重量锁）重量锁指针 | 自旋重试                |
| 执行完毕                | 10（重量锁）重量锁指针 | 自旋重试                |
| 成功（解锁）            | 01（无锁）             | 自旋重试                |
| -                       | 10（重量锁）重量锁指针 | 成功（加锁）            |
| -                       | 10（重量锁）重量锁指针 | 执行同步块              |
| -                       | ...                    | ...                     |



自旋重试失败的情况：

| 线程1（CPU1 上）        | 对象Mark               | 线程2（CPU2 上）        |
| ----------------------- | ---------------------- | ----------------------- |
| -                       | 10（重量锁）           | -                       |
| 访问同步块，获取monitor | 10（重量锁）重量锁指针 | -                       |
| 成功（加锁）            | 10（重量锁）重量锁指针 | -                       |
| 执行同步块              | 10（重量锁）重量锁指针 | -                       |
| 执行同步块              | 10（重量锁）重量锁指针 | 访问同步块，获取monitor |
| 执行同步块              | 10（重量锁）重量锁指针 | 自旋重试                |
| 执行同步块              | 10（重量锁）重量锁指针 | 自旋重试                |
| 执行同步块              | 10（重量锁）重量锁指针 | 自旋重试                |
| 执行同步块              | 10（重量锁）重量锁指针 | 阻塞                    |
| -                       | ...                    | ...                     |



结论：

- 在java6之后自旋锁是自适应的，比如对象刚刚的一次自旋操作成功过，那么认为这次自旋成功的可能性会高，就多自旋几次；反之，就少自旋甚至不自旋，总之，比较智能
- 自旋会占用CPU时间，单核CPU自旋就是浪费，多核CPU自旋才能发挥优势
- java7之后不能控制是否开启自旋功能



### 偏向锁

轻量级锁在没有竞争时（就自己这个线程），每次重入仍然需要执行 CAS 操作

Java6 中引入了偏向锁来作进一步优化：只有第一次使用 CAS 将线程ID设置到对象的 Mark Word头，之后发现朱哥线程ID是自己的就表示没有竞争，不用重新 CAS。以后只要不发生竞争，这个对象就归该线程所有

例如：

```java
static final Object obj = new Object();
public static void m1() {
    synchronized( obj ) {
        // 同步块A
        m2();
    }
}

public static void m2() {
    synchronized( obj ) {
        // 同步块B
        m3();
    }
}

public static void m3() {
    synchronized( obj ) {
        // 同步块C
    }
}
```

后两次的重复加锁，就是重入加锁



#### 偏向状态

基础的对象头格式：

```ruby
|--------------------------------------------------------------------|--------------------|
|												 Mark Word (64 bits) | 				State |
|--------------------------------------------------------------------|--------------------|
|  hashcode:25 | hashcode:31 | unused:1 | age:4 | biased_lock:0 | 01 | 			   Normal |
|--------------------------------------------------------------------|--------------------|
|    thread:54 | epoch:2     | unused:1 | age:4 | biased_lock:1 | 01 | 			   Biased |
|--------------------------------------------------------------------|--------------------|
| 										  ptr_to_lock_record:62 | 00 | Lightweight Locked |
|--------------------------------------------------------------------|--------------------|
| 								  ptr_to_heavyweight_monitor:62 | 10 | Heavyweight Locked |
|--------------------------------------------------------------------|--------------------|
| 																| 11 | 		Marked for GC |
|--------------------------------------------------------------------|--------------------|
        
 /*
  * biased_lock（是否启用偏向锁） ： 0-没有启用 1-启用偏向锁
  * 使用偏向锁后，存放的就是 threadID  
  */
```

一个对象创建时：

- 如果开启了偏向锁（默认开启），那么对象创建后，markword 置为 0x05 即最后3位位 101，这时它的 thread、epoch、age都为0
- 偏向锁是默认是延迟的，不会再程序启动时立即生效，如果想避免延迟，可以加 VM 参数 - XX：BiasedLockingStartupDelay=0 来禁用延迟
- 如果没有开启偏向锁，那么对象创建后，markword 值为 0x01 即最后3位为 001，这时它的 hashcode、age都为0，第一次用到 hashcode 时才会赋值

测试代码位置：`richard.test` 下的 `TestBiased`

```java
public static void main(String[] args) {
    Dog dog = new Dog();
    dog.hashCode(); // 会禁用这个对象的偏向锁：因为调用了该方法之后，会将hash码set到指定的markword中，那么就会变成 normal状态
    log.debug(getObjectHeader(dog));    // 001

    //main线程,偏向锁
    synchronized (dog){
        log.debug(getObjectHeader(dog));    // 00 轻量级锁
    }

    log.debug(getObjectHeader(dog));    // 001 释放完锁后，又变成正常不加锁的状态
}
```



#### 撤销 - 调用对象 hashCode

调用了对象的 hashCode，但偏向锁的对象 MarkWord 中存储的是线程id，如果调用 hashCode 会导致偏向锁被撤销

- 轻量级锁会在锁记录中记录 hashCode
- 重量级锁会在 Monitor 中记录 hashCode



#### 撤销 - 其他线程使用对象

当有其他线程使用偏向锁时，会将偏向锁升级为轻量级锁

```java
public static void main(String[] args) {
    Dog dog = new Dog();

    new Thread(() -> {
        log.debug(getObjectHeader(dog));

        synchronized (dog) {
            log.debug(getObjectHeader(dog));
        }
        log.debug(getObjectHeader(dog));

        synchronized (TestBiased.class) {
            TestBiased.class.notify();
        }
    }, "t1").start();

    new Thread(() -> {
        synchronized (TestBiased.class) {
            try {
                TestBiased.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.debug(getObjectHeader(dog));

        synchronized (dog) {
            log.debug(getObjectHeader(dog));
        }
        log.debug(getObjectHeader(dog));
    }, "t2").start();
}
```

注意：加了 wait/notify 是为了错峰加锁，如果有竞争那就直接变成重量级锁了

输入：

> 16:29:47.821 c.TestBiased [t1] - 00010101 10010011 11100001 00000000 00000000 00000000 00000000 00001101
> 16:29:47.822 c.TestBiased [t1] - 00010101 10010011 11100001 00000000 00010110 01010000 00011000 00001101
> 16:29:47.823 c.TestBiased [t1] - 00010101 10010011 11100001 00000000 00010110 01010000 00011000 00001101
>
> 16:29:47.823 c.TestBiased [t2] - 00010101 10010011 11100001 00000000 00010110 01010000 00011000 00001101
> 16:29:47.824 c.TestBiased [t2] - 00010101 10010011 11100001 00000000 00010110 10110000 11110110 10111000
> 16:29:47.824 c.TestBiased [t2] - 00010101 10010011 11100001 00000000 00000000 00000000 00000000 00001001

解析：

t1 一开始就为偏量级锁，后续加了锁以及释放后也是偏量级

t2 一开始也是偏量级锁 101，前面地址也没变，但是在加锁的时候发现已经有偏量级锁了，所以就会升级为轻量级锁 00，最后释放后，默认的偏量级锁也就变成了无锁状态的 001



#### 撤销 - 调用 wait/notify

只有重量级锁才有 wait/notify，所以也会将 偏量级锁/重量级锁 进行撤销



#### 批量重偏向

如果对象虽然被多个线程访问，但没有竞争，这时偏向了线程 T1 的对象仍有机会重新偏向 T2，重偏向会重置对象的 Thread ID

当撤销偏向锁阈值超过 20 次后，jvm 会认为是否是偏向错了，因此会在给这些对象加锁时重新偏向至加锁线程

```java
public static void main(String[] args) {
    Vector<Dog> list = new Vector<>();

    Thread t1 = new Thread(() -> {
        for (int i = 0; i < 30; i++) {
            Dog dog = new Dog();
            list.add(dog);
            synchronized (dog) {
                log.debug(getObjectHeader(dog));
            }
        }
        synchronized (list) {
            list.notify();
        }
    }, "t1");
    t1.start();

    Thread t2 = new Thread(() -> {
        synchronized (list) {
            try {
                list.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.debug("==============> ");
        for (int i = 0; i < 30; i++) {
            Dog dog = list.get(i);
            log.debug(getObjectHeader(dog));
            synchronized (dog) {
                log.debug(getObjectHeader(dog));
            }
            log.debug(getObjectHeader(dog));
        }
    }, "t2");
    t2.start();
}
```

输出结果（因为结果太长就不贴上来了）：

t1 运行的时候都是偏向锁，到t2 运行的时候，最开始的状态是：
偏向锁（偏向T1的）—>轻量级锁—>解锁。如此反复到第20位时，

变为：偏向锁（偏向T2），因为超过撤销阈值后，比对了两个的 hashCode可以得出，之前偏向线程T1的对象，现在变为偏向T2了



#### 批量撤销

当撤销偏向锁阈值超过 40 次后，jvm就会认为偏向有问题，根本就不应该偏向。于是整个类的所有对象都会变为不可偏向的，新建的对象也是不可偏向的

```java
static Thread t1,t2,t3;
private static void test4() throws InterruptedException {
    Vector<Dog> list = new Vector<>();

    int loopNumber = 39;
    t1 = new Thread(() -> {
        for (int i = 0; i < loopNumber; i++) {
            Dog dog = new Dog();
            list.add(dog);
            synchronized (dog) {
                log.debug(i + "\t" + getObjectHeader(dog));
            }
        }
        LockSupport.unpark(t2);
    }, "t1");
    t1.start();

    t2 = new Thread(() -> {
        LockSupport.park();
        log.debug("===============> ");
        for (int i = 0; i < loopNumber; i++) {
            Dog dog = list.get(i);
            log.debug(i + "\t" + getObjectHeader(dog));
            synchronized (dog) {
                log.debug(i + "\t" + getObjectHeader(dog));
            }
            log.debug(i + "\t" + getObjectHeader(dog));
        }
        LockSupport.unpark(t3);
    }, "t2");
    t2.start();

    t3 = new Thread(() -> {
        LockSupport.park();
        log.debug("===============> ");
        for (int i = 0; i < loopNumber; i++) {
            Dog dog = list.get(i);
            log.debug(i + "\t" + getObjectHeader(dog));
            synchronized (dog) {
                log.debug(i + "\t" + getObjectHeader(dog));
            }
            log.debug(i + "\t" + getObjectHeader(dog));
        }
    }, "t3");
    t3.start();

    t3.join();
    log.debug(getObjectHeader(new Dog()));
}
```

输出：最后主线程新new的对象，也不会再有偏向锁了



## wait notify

### wait/notify 原理

![](https://rsx.881credit.cn//uploads/images/projectImg/202204/30/8de37710641f2478d45003eb1ad237ca_1651302146_PgNhcA2c4v.png)

- Owner线程发现条件不满足，调用 wait 方法，即可进入 WaitSet 变为 WAITING 状态
- BLOCKED 和 WAITING 的线程都处于阻塞状态，不占用 CPU 时间片
- BLOCKED 线程会在 Owner 线程释放锁时唤醒
- WAITING 线程会在 Owner 线程调用 notify 或 notifyAll 时唤醒，但唤醒后并不意味着立刻获得锁，仍需进入 EntryList 重新竞争



### API 介绍

- obj.wait() 让进入 object 监视器的线程到 waitSet 等待
- obj.notify() 在 object 上正在 waitSet 等待的线程中挑一个唤醒
- obj.notifyAll() 让 object 上正在 waitSet 等待的线程全部唤醒

它们都是线程之间进行协作的手段，都属于 Object 对象的方法。必须获得此对象的锁，才能调用这几个方法

```java
public class TestWaitNotify {
    final static Object obj = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (obj) {
                log.debug("执行");
                try {
                    obj.wait();     // 让线程在 obj 上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                log.debug("执行其他代码");
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj) {
                log.debug("执行");
                try {
                    obj.wait();     // 让线程在 obj 上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                log.debug("执行其他代码");
            }
        }, "t2").start();

        // 主线程两秒后执行
        sleep(2);
        log.debug("唤醒 obj 上其它线程");
        synchronized (obj) {
            //obj.notify();   // 唤醒 obj 上一个线程
            obj.notifyAll();    // 唤醒 obj 上所有等待线程
        }
    }
}
```

输出：

① notify() 的情况下

21:43:00.015 c.TestWaitNotify [t1] - 执行
21:43:00.016 c.TestWaitNotify [t2] - 执行
21:43:02.025 c.TestWaitNotify [main] - 唤醒 obj 上其它线程
21:43:02.025 c.TestWaitNotify [t1] - 执行其他代码

② notifyAll() 的情况下

> 21:41:16.358 c.TestWaitNotify [t1] - 执行
> 21:41:16.359 c.TestWaitNotify [t2] - 执行
> 21:41:18.371 c.TestWaitNotify [main] - 唤醒 obj 上其它线程
> 21:41:18.371 c.TestWaitNotify [t2] - 执行其他代码
> 21:41:18.371 c.TestWaitNotify [t1] - 执行其他代码

**<font color=red>注意</font>**：

wait() 里不加参数，就是无线等待，如果加了参数，那就是到点醒了就会继续往下走；如果加了参数，其他线程提前notify，那么wait的线程也不会一直wait满时间



## wait notify的正确使用

### sleep(long n) 和 wait(long n) 的区别

（1）sleep 是 Thread 方法，而 wait 是 Object 方法

（2）sleep 不需要强制和 synchronized 配合使用，但 wait 需要和 synchronized 一起用

（3）sleep 在睡眠的同事，不会释放对象锁，但 wait 在等待的时候会释放对象锁

共同点：

它们的状态都是 **TIMED_WAITING**

```java
static final Object lock = new Object();

public static void main(String[] args) {
    new Thread(() -> {
        synchronized (lock) {
            log.debug("获得锁");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }, "t1").start();

    Sleeper.sleep(1);
    synchronized (lock) {
        log.debug("获得锁");
    }
}
```

输出：

> 15:23:20.997 c.Test19 [t1] - 获得锁

t1 线程获得锁，sleep后就不释放锁了，所以主线程再也拿不到锁；如果t1线程中的sleep改为 `lock.wait()`，t1线程一进入休息室，那主线程就可以拿到锁了



### 实际例子

代码位置：rickard.demo5下

step 1（TestCorrectPostureStep1类）：

```java
public class TestCorrectPostureStep1 {
    static final Object room = new Object();
    static boolean hasCigarette = false; // 有没有烟
    static boolean hasTakeout = false;

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    sleep(2);
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                }
            }
        }, "小南").start();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                synchronized (room) {
                    log.debug("可以开始干活了");
                }
            }, "其它人").start();
        }

        sleep(1);
        new Thread(() -> {
            // 这里能不能加 synchronized (room)？
            hasCigarette = true;
            log.debug("烟到了噢！");
        }, "送烟的").start();
    }
}
```

分析：

- 因为线程 `小南` 关门盖被睡觉，同时 `sleep(2)` 不会释放锁，导致其他线程都会被阻塞
- 因为 `小南` 线程的阻塞，所以 `送烟` 就算提前送到，也无法立刻醒来
- 该例子的 main 没有加 synchronized 就好像主线程是翻窗户进来的

因此，可以使用 wait-notify 进行优化



step2（TestCorrectPostureStep2类）：

```java
public class TestCorrectPostureStep2 {
    static final Object room = new Object();
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                }
            }
        }, "小南").start();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                synchronized (room) {
                    log.debug("可以开始干活了");
                }
            }, "其它人").start();
        }

        sleep(1);
        new Thread(() -> {
            synchronized (room) {
                hasCigarette = true;
                log.debug("烟到了噢！");
                room.notify();
            }
        }, "送烟的").start();
    }
}
```

输出结果正确，`小南` 线程在等待的时候会释放锁，其他人可以正常加锁使用



step3（TestCorrectPostureStep3）：

```java
public class TestCorrectPostureStep3 {
    static final Object room = new Object();
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;

    // 虚假唤醒
    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小南").start();

        new Thread(() -> {
            synchronized (room) {
                Thread thread = Thread.currentThread();
                log.debug("外卖送到没？[{}]", hasTakeout);
                if (!hasTakeout) {
                    log.debug("没外卖，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("外卖送到没？[{}]", hasTakeout);
                if (hasTakeout) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小女").start();

        sleep(1);
        new Thread(() -> {
            synchronized (room) {
                hasTakeout = true;
                log.debug("外卖到了噢！");
                room.notify();
            }
        }, "送外卖的").start();
    }
}
```

输出：

使用notify，就是虚假唤醒，并不能正确唤醒到正确的等待线程，这时候只需要将 `notify()` 改为 `notifyAll()`



step4（TestCorrectPostureStep4）：

```java
public class TestCorrectPostureStep4 {
    static final Object room = new Object();
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;

    public static void main(String[] args) {

        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                while (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小南").start();

        new Thread(() -> {
            synchronized (room) {
                Thread thread = Thread.currentThread();
                log.debug("外卖送到没？[{}]", hasTakeout);
                if (!hasTakeout) {
                    log.debug("没外卖，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("外卖送到没？[{}]", hasTakeout);
                if (hasTakeout) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小女").start();

        sleep(1);
        new Thread(() -> {
            synchronized (room) {
                hasTakeout = true;
                log.debug("外卖到了噢！");
                room.notifyAll();
            }
        }, "送外卖的").start();
    }
}
```

将 `小南` 线程的if 判断改为 while 判断，用于解决虚假唤醒



### 同步模式之保护性暂停（Guarded Suspension）

```java
synchronized (lock) {
    while(条件不成立) {
    	// 继续等待
        lock.wait();
	}
    // 成立了就继续执行后续逻辑
}

// 另一个线程
synchronized (lock) {
	lock.notifyAll();
}
```



同步模式之保护性暂停：即 Guarded Suspension，用在一个线程等待另一个线程的执行结果

**<font color=red>要点</font>**：

- 有一个结果需要从一个线程传递到另一个线程，让他们关联同一个 GuardedObject
- 如果有结果不断从一个线程到另一个线程，那么可以使用消息队列
- JDK 中，join的实现、Future的实现，采用的就是此模式
- 因为要等待另一方的结果，因此也是同步模式

![](https://rsx.881credit.cn//uploads/images/projectImg/202205/02/15eb551afc90ffc01a367c3526fab0fc_1651481512_1jOmsHDJka.png)

```java
public class Test20 {
    // 线程1 等待 线程2的下载结果
    public static void main(String[] args) {
        GuardedObject guardedObject = new GuardedObject();
        new Thread(() -> {
            // 等待结果
            log.debug("begin");
            Object object = guardedObject.get(2000);
            log.debug("结果是：{}",object);
        }, "t1").start();

        new Thread(() -> {
            log.debug("begin");
            Sleeper.sleep(1);
            guardedObject.complete(new Object());
        }, "t2").start();

    }
}

// 增加超市效果
class GuardedObject {
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
```



#### 扩展（解耦—重要例子）

如果需要多在多个类之间使用 GuardObject 对象，作为参数传递不是很方便，因此设计一个用来解耦的中间类，这样不仅能够解耦 【结果等待者】和【结果生产者】，还能够同时支持多个任务的管理

![](https://rsx.881credit.cn//uploads/images/projectImg/202205/03/5ad2d7b15c25d90608730b67f96ad752_1651559157_uYAFNEcORZ.png)

```java
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
```

输出：

> 14:57:35.066 c.People [Thread-1] - 开始收信 id:3
> 14:57:35.066 c.People [Thread-0] - 开始收信 id:1
> 14:57:35.066 c.People [Thread-2] - 开始收信 id:2
> 14:57:36.080 c.PostMan [Thread-3] - 送信 id:3,内容:内容3
> 14:57:36.080 c.People [Thread-1] - 收到信 id:3,内容:内容3
> 14:57:36.080 c.PostMan [Thread-4] - 送信 id:2,内容:内容2
> 14:57:36.080 c.People [Thread-2] - 收到信 id:2,内容:内容2
> 14:57:36.080 c.PostMan [Thread-5] - 送信 id:1,内容:内容1
> 14:57:36.080 c.People [Thread-0] - 收到信 id:1,内容:内容1

解耦结果产生者和消费者



### 异步模式之生产者/消费者

#### 定义

要点：

- 与前面的保护性暂停中的 GuardObject 不同，**不需要产生结果和消费结果的线程一一对应**
- 消费队列可以采用平衡生产和消费的线程资源
- 生产者仅负责产生结果数据，不关心数据如何处理，而消费者专心处理结果数据
- 消息队列是有容量限制的，**满时不会再加入数据，空时不会再消耗数据**
- JDK 中各种阻塞队列，采用的就是这种模式

![](https://rsx.881credit.cn//uploads/images/projectImg/202205/03/aea21b696ff866c5380babea6a01c1ba_1651564205_C6TDuALj1d.png)

（**注**：消息队列中的字母，仅代表每个线程产生的消息代号）



#### 实现

```java
public class Test21 {
    public static void main(String[] args) {
        MessageQueue queue = new MessageQueue(2);

        for (int i = 0; i < 3; i++) {
            int id = i;
            new Thread(() -> {
                queue.put(new Message(id, "值" + id));
            }, "生产者" + i).start();
        }

        new Thread(() -> {
            while (true) {
                sleep(1);
                Message message = queue.take();
            }
        }, "消费者").start();
    }
}

// 消息队列类（用于Java线程之间的通信，MQ是进程之间的通信）
@Slf4j(topic = "c.MessageQueue")
class MessageQueue {
    // 消息的队列集合
    private LinkedList<Message> list = new LinkedList<>();
    // 队列容量
    private int capcity;

    public MessageQueue(int capcity) {
        this.capcity = capcity;
    }

    // 获取消息
    public Message take() {
        // 检查对象是否为空
        synchronized (list) {
            while (list.isEmpty()) {
                try {
                    log.debug("队列为空，消费者线程等待");
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 从队列头部获取消息并返回
            Message message = list.removeFirst();
            log.debug("已消费消息{}",message);
            list.notifyAll();
            return message;
        }
    }

    // 存入消息
    public void put(Message message) {
        synchronized (list) {
            // 检查对象是否已经满了
            while (list.size() == capcity) {
                try {
                    log.debug("队列已满，生产者线程等待");
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 将消息胶乳队列尾部
            list.addLast(message);
            log.debug("已生产信息{}",message);
            list.notifyAll();
        }
    }
}

final class Message {
    private int id;
    private Object value;

    public int getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public Message(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}
```



## Park & Unpark

### 基本使用

它们是 LockSupport 类中的方法

```java
// 暂停当前线程
LockSupport.park();

// 恢复某个线程的运行
LockSupport.unpark(暂停线程对象)
```

先park，再 unpark

```java
public static void main(String[] args) {
    Thread t1 = new Thread(() -> {
        log.debug("start ...");
        sleep(1);
        log.debug("park ...");
        LockSupport.park();
        log.debug("resume");
    }, "t1");
    t1.start();

    sleep(2);
    log.debug("unpark ...");
    LockSupport.unpark(t1);
}
```

输出：

> 17:35:21.341 c.TestMultiLock [t1] - start ...
> 17:35:22.357 c.TestMultiLock [t1] - park ...
> 17:35:23.354 c.TestMultiLock [main] - unpark ...
> 17:35:23.354 c.TestMultiLock [t1] - resume

**<font color=red>注意</font>**：如果将sleep的时间改变一些，使得执行顺序为先 `unpark` ，再执行 `park`，那么 `unpark`  既可以在 `park` 之前调用，也可以在 `park` 之后调用，都可以恢复指定线程的运行



### 特点

与 Object 的 wait & notify 相比

- wait、notify和notifyAll 必须配合 Object Monitor 一起使用，而 park & unpark 不必
- park & unpark 是以线程为单位来【阻塞】和【唤醒】线程，而 notify 只能随机唤醒一个等待线程，notifyAll 是唤醒所有等待线程，就不那么【精确】
- park & unpark 可以先 unpark，而 wait & notify 不能先  notify



### park & unpark 的原理

每个线程都有自己的一个 Parker 对象，由三部分组成 ` _counter`（信号量）、`_cond`（条件变量） 和  `_mutex`（互斥量）

举例子：

- 线程就像一个旅人，Parker 就像他随身携带的背包，**条件变量**就好比背包中的**帐篷**，_counter 就好比背包中的备用干粮（0为耗尽，1为充足）
- 调用 park 就是要看需不需要停下来歇息
  - 如果备用干粮耗尽，那么钻进 **帐篷** 歇息
  - 如果备用干粮充足，那么不需要停留，继续前进
- 调用 unpark，就好比令干粮充足
  - 如果这时线程还在 **帐篷**，就唤醒让他继续前进
  - 如果这时线程还在运行，那么下次他调用 park 时，仅是消耗备用干粮，不需停留继续前进
    - 因为背包空间有限，多次调用 unpark 仅会补充一次备份干粮



图解1-先调用park方法：

![](https://rsx.881credit.cn//uploads/images/projectImg/202205/03/de8881d0ce0f88d34237d22803edbb9f_1651573087_yeieCvGtAK.png)

1、当前线程调用 Unsafe.park() 方法

2、检查 _counter，当情况为0，这时获得 _mutex 互斥锁

3、线程进入 _cond 条件变量阻塞

4、设置 _counter=0



图解2-先调用unpark方法：

![](https://rsx.881credit.cn//uploads/images/projectImg/202205/03/8a9d2e95b008ff8300b4ba7f3193915e_1651573484_QjrrK7xri5.png)

1、调用 Unsafe.unpark(Thread_0)方法，设置 _counter 为1

2、唤醒 _cond 条件变量中的 Thread_0

3、Thread_0 恢复运行

4、设置 _counter 为0



图解3-先调用 unpark，在调用 park：

![](https://rsx.881credit.cn//uploads/images/projectImg/202205/03/39777c2b2e78f40d393bd40b9f2c0385_1651573801_1w2lBr9lc9.png)

1、调用 Unsafe.unpark(Thread_0) 方法，设置 _counter为1

2、当前线程调用 Unsafe.park() 方法

3、检查 _counter，本情况为1，这时线程无需阻塞，继续运行

4、设置 _counter 为0



