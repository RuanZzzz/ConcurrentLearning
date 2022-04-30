package richard.demo3.exercise;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

@Slf4j(topic = "c.ExerciseSell")
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