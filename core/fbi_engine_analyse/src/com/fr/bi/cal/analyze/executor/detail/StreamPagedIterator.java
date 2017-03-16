package com.fr.bi.cal.analyze.executor.detail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by daniel on 2016/7/13.
 */
public class StreamPagedIterator<T> implements Iterator<T> {

    private int maxCount = 1 << 14;
    //通知开始生产的数量，队列里面小于一定数量就通知加的线程开始增加，不要等到空了wait住
    private int produceCount = maxCount >> 1;
    //通知开始消费的数量，队列里面大于一定的数量就开始唤醒waitfor方法里面等待消费的线程
    private int consumeCount = maxCount - 1;
    //是不是正在消费
    private volatile boolean isWorking = true;
    private volatile Queue<T> queue = new LinkedList<T>();
    private volatile boolean isEnd = false;

    public StreamPagedIterator() {
    }

    public StreamPagedIterator(int maxCount, int produceCount, int consumeCount) {
        this.maxCount = maxCount;
        this.produceCount = produceCount;
        this.consumeCount = consumeCount;
    }


    private void waitFor() {
        if (queue.size() == produceCount) {
            synchronized (this) {
                this.notify();
            }
        }
        if (queue.isEmpty()) {
            synchronized (this) {
                while (isRealEmpty() && (!isEnd)) {
                    try {
                        isWorking = false;
                        this.wait();
                        isWorking = true;
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private boolean isRealEmpty() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }


    @Override
    public boolean hasNext() {
        waitFor();
        return (!isEnd) || (!isRealEmpty());
    }

    @Override
    public T next() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    public void finish() {
        isEnd = true;
        synchronized (this) {
            this.notify();
        }
    }

    public void wakeUp() {
        synchronized (this) {
            this.notify();
        }
    }

    public void addCell(T cellElement) {
        if (queue.size() > maxCount) {
            synchronized (this) {
                if (queue.size() > maxCount) {
                    try {
                        this.wait();
                    } catch (Exception e) {
                    }
                }
            }
        }
        synchronized (queue) {
            queue.add(cellElement);
        }
        //如果消费线程wait住了，并且超过了消费阈值就唤醒消费线程
        if (queue.size() > consumeCount) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
