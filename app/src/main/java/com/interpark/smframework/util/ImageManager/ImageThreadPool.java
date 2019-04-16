package com.interpark.smframework.util.ImageManager;

import com.interpark.smframework.base.types.PERFORM_SEL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ImageThreadPool {
    public ImageThreadPool(int threadCount) {
        _running = true;

        for (int i=0; i<threadCount; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        threadFunc();
                    } catch (InterruptedException e) {

                    }
                }
            });
            t.start();
            _workers.add(t);
        }
    }

    public void interrupt() {
        _running = false;
//            _mutex.lock();
        synchronized (_cond) {
            _cond.signalAll();
        }
//            _mutex.unlock();
    }

    public void addTask(final PERFORM_SEL task) {

//            Log.i("ImageDownloader", "[[[[[ thread pool addTask!!!!!!");

        synchronized (_queue) {
            _queue.add(task);
        }

//            _mutex.lock();
//            _queue.add(task);
        synchronized (_cond) {
            _cond.notify();
        }
//            _mutex.unlock();
    }

    private void threadFunc() throws InterruptedException {
        while (true) {
            PERFORM_SEL task = null;

            if (!_running) {
                break;
            }

            boolean isEmpty = false;
            synchronized (_queue) {
                isEmpty = _queue.isEmpty();
            }

            if (!isEmpty) {
                synchronized (_queue) {
                    task = _queue.poll();
                }
            } else {
                synchronized (_cond) {
                    _cond.wait();
                }
                if (!_running) {
                    break;
                }
                continue;
            }

            if (task!=null) {
                task.performSelector();
            }
        }
    }

    private final Lock _mutex = new ReentrantLock(true);
    private final Condition _cond = _mutex.newCondition();
    private ArrayList<Thread> _workers = new ArrayList<>();

    private Queue<PERFORM_SEL> _queue = new LinkedList<>();
    private boolean _running;;
}
