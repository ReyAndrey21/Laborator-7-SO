import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.Queue;

public class SharedResource {
    private final Lock lock = new ReentrantLock();
    private final Condition whiteCondition = lock.newCondition();
    private final Condition blackCondition = lock.newCondition();
    private int whiteCount = 0; 
    private int blackCount = 0; 
    private boolean whiteTurn = true; 
    private final Queue<String> queue = new LinkedList<>(); 

    
    public void useResource(String color,int id) {
        System.out.println(color + " thread " + id + " is using the resource.");
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(color + " thread " + id + " finished using the resource.");
    }

    
    public void accessResource(String color,int id) {
        lock.lock();
        try {
            
            queue.add(color);

            if ("White".equals(color)) {
                while (blackCount > 0 || !whiteTurn || !Objects.equals(queue.peek(), "White")) {
                    whiteCondition.await();
                }
                whiteCount++;
            } else if ("Black".equals(color)) {
                while (whiteCount > 0 || whiteTurn || !Objects.equals(queue.peek(), "Black")) {
                    blackCondition.await();
                }
                blackCount++;
            }

            queue.poll();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }

        useResource(color,id);

        lock.lock();
        try {
            
            if ("White".equals(color)) {
                whiteCount--;
                if (whiteCount == 0) {
                    whiteTurn = false; 
                    blackCondition.signalAll();
                }
            } else if ("Black".equals(color)) {
                blackCount--;
                if (blackCount == 0) {
                    whiteTurn = true; 
                    whiteCondition.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}

