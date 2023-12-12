package ait.mediation;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private final Queue<T> queue;
    private final int maxSize;
    private final Lock locker = new ReentrantLock();
    private final Condition notFull = locker.newCondition();
    private final Condition notEmpty = locker.newCondition();
    public BlkQueueImpl(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedList<>();

    }

    @Override
    public void push(T message) {
        locker.lock();
        try {
            while (queue.size() == maxSize) {
                // Queue is full, wait for space
                notFull.await();
            }
            queue.offer(message);
            // Signal consumers that the queue is not empty
            notEmpty.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            locker.unlock();
        }
    }

    @Override
    public T pop() {
        locker.lock();
        try {
            while (queue.isEmpty()) {
                // Queue is empty, wait for messages
                notEmpty.await();
            }
            T message = queue.poll();
            // Signal producers that the queue is not full
            notFull.signal();
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            locker.unlock();
        }
    }
}
