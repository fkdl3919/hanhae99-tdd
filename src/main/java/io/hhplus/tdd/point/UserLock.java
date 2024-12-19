package io.hhplus.tdd.point;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class UserLock {

    private ReentrantLock lock;

    // 참조 카운트 관리
    private int counter;

    public UserLock(ReentrantLock lock) {
        this.lock = lock;
        this.counter = 0;
    }

    public int getCounter() {
        return this.counter;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public synchronized void incrementCounter() {
        counter++;
    }

    public synchronized void decrementCounter() {
        counter--;
    }

    public synchronized boolean isUnused() {
        return counter == 0;
    }

}
