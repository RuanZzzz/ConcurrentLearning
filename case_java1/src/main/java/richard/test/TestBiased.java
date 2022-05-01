package richard.test;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;
import org.testng.annotations.Test;

import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 查看markword
@Slf4j(topic = "c.TestBiased")
public class TestBiased {
    static class Obj{
        int i=0;
//        byte[] array=new byte[100];
    }

    /**
     * 获得二进制数据
     * @param o
     * @return
     */
    public static String getObjectHeader(Object o){
        ByteOrder order=ByteOrder.nativeOrder();//字节序
        String table=ClassLayout.parseInstance(o).toPrintable();
        final Pattern p=Pattern.compile("(0|1){8}");
        Matcher matcher=p.matcher(table);
        List<String> header=new ArrayList<>();
        while(matcher.find()){
            header.add(matcher.group());
        }
        //小端机器,需要反过来遍历
        StringBuilder sb=new StringBuilder();
        if(order.equals(ByteOrder.LITTLE_ENDIAN)){
            Collections.reverse(header);
        }
        for(String s:header){
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * 针对64bit jvm的解析对象头函数
     * 在64bit jvm中,对象头有两个部分: Mark Word和Class Pointer, Mark Word占8字节,Class Pointer則看具体情况4字节或8字节
     *                              Class Pointer占4字节(64bit JVM开启压缩指针选项(默认是开启的),不开启的话就是8个字节)
     * @param s 对象头的二进制形式字符串(每8位,使用一个空格分开)
     */
    public static void parseObjectHeader(String s){
        String[] tmp=s.split(" ");
        System.out.print("Class Pointer: ");
        for(int i=0;i<4;++i){
            System.out.print(tmp[i]+" ");
        }
        System.out.println("\nMark Word:");
        if(tmp[11].charAt(5)=='0'&&tmp[11].substring(6).equals("01")){//0 01无锁状态,不考虑GC标记的情况
            //notice: 无锁情况下mark word的结构: unused(25bit) + hashcode(31bit) + unused(1bit) + age(4bit) + biased_lock_flag(1bit) + lock_type(2bit)
            //      hashcode只需要31bit的原因是: hashcode只能大于等于0,省去了负数范围,所以使用31bit就可以存储
            System.out.print("\thashcode (31bit): ");
            System.out.print(tmp[7].substring(1)+" ");
            for(int i=8;i<11;++i) System.out.print(tmp[i]+" ");
            System.out.println();
        }else if(tmp[11].charAt(5)=='1'&&tmp[11].substring(6).equals("01")){//1 01,即偏向锁的情况
            //notice: 对象处于偏向锁的情况,其结构为: ThreadID(54bit) + epoch(2bit) + unused(1bit) + age(4bit) + biased_lock_flag(1bit) + lock_type(2bit)
            //      这里的ThreadID是持有偏向锁的线程ID, epoch: 一个偏向锁的时间戳,用于偏向锁的优化
            System.out.print("\tThreadID(54bit): ");
            for(int i=4;i<10;++i) System.out.print(tmp[i]+" ");
            System.out.println(tmp[10].substring(0,6));
            System.out.println("\tepoch: "+tmp[10].substring(6));
        }else{//轻量级锁或重量级锁的情况,不考虑GC标记的情况
            //notice: JavaThread*(62bit,include zero padding) + lock_type(2bit)
            //      此时JavaThread*指向的是 栈中锁记录/重量级锁的monitor
            System.out.print("\tjavaThread*(62bit,include zero padding): ");
            for(int i=4;i<11;++i) System.out.print(tmp[i]+" ");
            System.out.println(tmp[11].substring(0,6));
            System.out.println("\tLockFlag (2bit): "+tmp[11].substring(6));
            System.out.println();
            return;
        }
        System.out.println("\tage (4bit): "+tmp[11].substring(1,5));
        System.out.println("\tbiasedLockFlag (1bit): "+tmp[11].charAt(5));
        System.out.println("\tLockFlag (2bit): "+tmp[11].substring(6));

        System.out.println();
    }


    @Test
    public void testBiasedLock(){
        //需要禁止偏向锁延迟: -XX:BiasedLockingStartupDelay=0
        Obj o=new Obj();
        parseObjectHeader(getObjectHeader(o));
        synchronized (o){
            parseObjectHeader(getObjectHeader(o));
        }
    }

    @Test
    public void testLightLock(){
        //需要禁止偏向锁延迟: -XX:BiasedLockingStartupDelay=0
        Obj o=new Obj();
        parseObjectHeader(getObjectHeader(o));
        synchronized (o){
            parseObjectHeader(getObjectHeader(o));
        }
        //升级为轻量级锁,因为下面的线程又占用了o,注意是上面先执行完后,才开启下面这个线程,因此不会升级为重量级锁
        new Thread(()->{
            synchronized (o){
                parseObjectHeader(getObjectHeader(o));
            }
        }).start();
    }

    @Test
    public void testHeavyLock(){
        //这里没有禁止偏向锁延迟,所以最初是无锁状态
        Obj o=new Obj();
        parseObjectHeader(getObjectHeader(o));
        synchronized (o){
            parseObjectHeader(getObjectHeader(o));
        }
        //重量级锁
        for(int i=0;i<2;++i)//线程数大于1时(交错执行),会升级成重量级锁
            new Thread(()->{
                synchronized (o){
                    parseObjectHeader(getObjectHeader(o));
                }
            }).start();
    }

    /**
     * log.debug(getObjectHeader(dog));
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        test4();
    }

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

}



class Dog {

}
