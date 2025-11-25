package com.quanhai.dingdingdemo.jucDemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

/**
 * Lock接口演示类
 * 
 * 本演示包含：
 * 1. ReentrantLock - 可重入锁的使用
 * 2. ReentrantReadWriteLock - 读写锁的使用
 * 3. tryLock() - 非阻塞式获取锁
 * 4. tryLock(timeout) - 超时获取锁
 * 5. Condition - 条件变量的使用
 * 6. Lock与synchronized的区别和优势
 */
public class LockDemo {

    /**
     * 可重入锁演示类
     */
    static class ReentrantLockDemo {
        private final ReentrantLock lock = new ReentrantLock();
        private int counter = 0;
        private int writeCounter = 0;

        /**
         * 使用ReentrantLock的普通锁方法
         */
        public void incrementCounter(String threadName) {
            lock.lock(); // 获取锁
            try {
                System.out.println(threadName + " 获取到锁，当前重入次数: " + lock.getHoldCount());
                
                // 模拟操作
                counter++;
                Thread.sleep(200);
                
                // 递归调用演示可重入性
                innerIncrement(threadName);
                
                System.out.println(threadName + " 完成操作，计数器值: " + counter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock(); // 释放锁
                System.out.println(threadName + " 释放锁");
            }
        }

        /**
         * 内部方法，演示锁的可重入性
         * 同一个线程可以多次获得同一把锁
         */
        private void innerIncrement(String threadName) {
            lock.lock(); // 再次获取同一个锁
            try {
                System.out.println(threadName + " 嵌套锁获取成功，当前重入次数: " + lock.getHoldCount());
                counter++;
            } finally {
                lock.unlock(); // 释放锁
                System.out.println(threadName + " 嵌套锁释放，当前重入次数: " + lock.getHoldCount());
            }
        }

        /**
         * 使用tryLock()非阻塞获取锁
         */
        public void tryLockDemo(String threadName) {
            if (lock.tryLock()) {
                try {
                    System.out.println(threadName + " 成功获取锁，开始操作");
                    counter++;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println(threadName + " 锁操作完成");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println(threadName + " 无法获取锁，执行其他逻辑");
            }
        }

        /**
         * 使用tryLock(timeout)超时获取锁
         */
        public void tryLockWithTimeout(String threadName, long timeout) {
            try {
                if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println(threadName + " 在" + timeout + "ms内获取到锁");
                        writeCounter++;
                        Thread.sleep(500); // 模拟长时间操作
                        System.out.println(threadName + " 写操作完成");
                    } finally {
                        lock.unlock();
                        System.out.println(threadName + " 释放锁");
                    }
                } else {
                    System.out.println(threadName + " 在" + timeout + "ms内未能获取到锁");
                }
            } catch (InterruptedException e) {
                System.out.println(threadName + " 获取锁时被中断");
                Thread.currentThread().interrupt();
            }
        }

        public int getCounter() {
            lock.lock();
            try {
                return counter;
            } finally {
                lock.unlock();
            }
        }

        public int getWriteCounter() {
            lock.lock();
            try {
                return writeCounter;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 读写锁演示类
     * 多个线程可以同时读，但写操作是排他的
     */
    static class ReadWriteLockDemo {
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private final Lock readLock = readWriteLock.readLock();
        private final Lock writeLock = readWriteLock.writeLock();
        
        private String data = "初始数据";
        private int readCount = 0;
        private int writeCount = 0;

        /**
         * 读取数据 - 多个线程可以同时读取
         */
        public void readData(String threadName) {
            readLock.lock(); // 获取读锁
            try {
                System.out.println(threadName + " 获取读锁，开始读取数据");
                readCount++;
                
                // 模拟读取耗时
                Thread.sleep(300);
                
                System.out.println(threadName + " 读取到数据: " + data + ", 当前读操作次数: " + readCount);
                
                // 再次嵌套读取演示可重入性
                nestedRead(threadName);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                readLock.unlock();
                System.out.println(threadName + " 释放读锁");
            }
        }

        /**
         * 嵌套读取演示
         */
        private void nestedRead(String threadName) {
            readLock.lock(); // 再次获取读锁
            try {
                readCount++;
                System.out.println(threadName + " 嵌套读取，数据: " + data);
            } finally {
                readLock.unlock();
            }
        }

        /**
         * 写入数据 - 排他性操作
         */
        public void writeData(String newData, String threadName) {
            writeLock.lock(); // 获取写锁
            try {
                System.out.println(threadName + " 获取写锁，开始写入数据");
                writeCount++;
                
                // 模拟写入耗时
                Thread.sleep(500);
                
                String oldData = data;
                data = newData;
                
                System.out.println(threadName + " 写入完成: " + oldData + " -> " + data + ", 写操作次数: " + writeCount);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                writeLock.unlock();
                System.out.println(threadName + " 释放写锁");
            }
        }

        /**
         * 读写锁升级演示
         * 注意：ReentrantReadWriteLock不支持升级
         */
        public void demonstrateUpgrade() {
            readLock.lock();
            try {
                System.out.println("首先获取读锁，尝试升级到写锁...");
                
                // ReentrantReadWriteLock不支持锁升级，会导致死锁
                // writeLock.lock(); // 这会导致死锁！
                
                System.out.println("ReentrantReadWriteLock不支持锁升级，需要先释放读锁再获取写锁");
            } finally {
                readLock.unlock();
            }
        }

        public String getData() {
            readLock.lock();
            try {
                return data;
            } finally {
                readLock.unlock();
            }
        }

        public int getReadCount() {
            return readCount;
        }

        public int getWriteCount() {
            return writeCount;
        }
    }

    /**
     * 条件变量演示类
     * 使用Condition实现线程间的协调
     */
    static class ConditionDemo {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition(); // 队列非空条件
        private final Condition notFull = lock.newCondition();  // 队列未满条件
        
        private final String[] buffer = new String[5];
        private int count = 0;
        private int head = 0;
        private int tail = 0;

        /**
         * 生产者方法 - 向缓冲区添加数据
         */
        public void produce(String item, String threadName) throws InterruptedException {
            lock.lock();
            try {
                // 当缓冲区满时，等待
                while (count == buffer.length) {
                    System.out.println(threadName + " 缓冲区满，等待消费者...");
                    notFull.await(); // 等待缓冲区不满
                }
                
                // 添加数据
                buffer[tail] = item;
                tail = (tail + 1) % buffer.length;
                count++;
                
                System.out.println(threadName + " 生产了: " + item + ", 缓冲区状态: " + count + "/" + buffer.length);
                
                // 通知消费者
                notEmpty.signal(); // 唤醒等待非空条件的线程
                
            } finally {
                lock.unlock();
            }
        }

        /**
         * 消费者方法 - 从缓冲区取数据
         */
        public String consume(String threadName) throws InterruptedException {
            lock.lock();
            try {
                // 当缓冲区空时，等待
                while (count == 0) {
                    System.out.println(threadName + " 缓冲区空，等待生产者...");
                    notEmpty.await(); // 等待缓冲区非空
                }
                
                // 取数据
                String item = buffer[head];
                head = (head + 1) % buffer.length;
                count--;
                
                System.out.println(threadName + " 消费了: " + item + ", 缓冲区状态: " + count + "/" + buffer.length);
                
                // 通知生产者
                notFull.signal(); // 唤醒等待未满条件的线程
                
                return item;
                
            } finally {
                lock.unlock();
            }
        }

        /**
         * 演示条件变量等待超时
         */
        public void produceWithTimeout(String item, String threadName) throws InterruptedException {
            lock.lock();
            try {
                if (count == buffer.length) {
                    System.out.println(threadName + " 缓冲区满，等待1秒...");
                    notFull.await(1, TimeUnit.SECONDS);
                }
                
                if (count < buffer.length) {
                    buffer[tail] = item;
                    tail = (tail + 1) % buffer.length;
                    count++;
                    System.out.println(threadName + " 超时等待后成功生产: " + item);
                    notEmpty.signal();
                } else {
                    System.out.println(threadName + " 等待超时，生产失败");
                }
                
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 演示ReentrantLock基本用法
     */
    public static void demonstrateReentrantLock() {
        System.out.println("\n========== 演示ReentrantLock ==========");
        
        ReentrantLockDemo demo = new ReentrantLockDemo();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 演示可重入锁
        System.out.println("--- 演示可重入性 ---");
        for (int i = 1; i <= 3; i++) {
            final String threadName = "可重入线程-" + i;
            executor.submit(() -> demo.incrementCounter(threadName));
        }
        
        // 等待完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("最终计数器值: " + demo.getCounter());
        
        // 演示tryLock()
        System.out.println("\n--- 演示tryLock() ---");
        for (int i = 1; i <= 3; i++) {
            final String threadName = "尝试锁线程-" + i;
            executor.submit(() -> demo.tryLockDemo(threadName));
        }
        
        // 等待完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 演示tryLock(timeout)
        System.out.println("\n--- 演示tryLock(timeout) ---");
        for (int i = 1; i <= 3; i++) {
            final String threadName = "超时锁线程-" + i;
            final long timeout = i * 500; // 500ms, 1000ms, 1500ms
            executor.submit(() -> demo.tryLockWithTimeout(threadName, timeout));
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("写操作计数器值: " + demo.getWriteCounter());
    }

    /**
     * 演示读写锁
     */
    public static void demonstrateReadWriteLock() {
        System.out.println("\n========== 演示ReentrantReadWriteLock ==========");
        
        ReadWriteLockDemo demo = new ReadWriteLockDemo();
        ExecutorService executor = Executors.newFixedThreadPool(6);
        
        // 启动多个读线程
        for (int i = 1; i <= 4; i++) {
            final String threadName = "读线程-" + i;
            executor.submit(() -> {
                for (int j = 0; j < 2; j++) {
                    demo.readData(threadName);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 启动写线程
        for (int i = 1; i <= 2; i++) {
            final String threadName = "写线程-" + i;
            final String data = "数据-" + i;
            executor.submit(() -> {
                try {
                    Thread.sleep(200); // 让读线程先开始
                    demo.writeData(data, threadName);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(8, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n最终数据: " + demo.getData());
        System.out.println("总读操作次数: " + demo.getReadCount());
        System.out.println("总写操作次数: " + demo.getWriteCount());
        
        // 演示锁升级
        demo.demonstrateUpgrade();
    }

    /**
     * 演示条件变量
     */
    public static void demonstrateCondition() {
        System.out.println("\n========== 演示Condition ==========");
        
        ConditionDemo demo = new ConditionDemo();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        
        // 启动生产者线程
        for (int i = 1; i <= 3; i++) {
            final String threadName = "生产者-" + i;
            final String item = "商品-" + i;
            executor.submit(() -> {
                for (int j = 0; j < 3; j++) {
                    try {
                        demo.produce(item + "-" + j, threadName);
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 启动消费者线程
        for (int i = 1; i <= 2; i++) {
            final String threadName = "消费者-" + i;
            executor.submit(() -> {
                for (int j = 0; j < 4; j++) {
                    try {
                        String item = demo.consume(threadName);
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 演示Lock与synchronized的区别
     */
    public static void demonstrateLockVsSynchronized() {
        System.out.println("\n========== Lock vs synchronized 区别演示 ==========");
        
        System.out.println("Lock接口优势：");
        System.out.println("1. 可以尝试非阻塞地获取锁 (tryLock())");
        System.out.println("2. 可以设置超时时间获取锁 (tryLock(timeout))");
        System.out.println("3. 可以响应中断 (lockInterruptibly())");
        System.out.println("4. 可以实现多个条件变量 (Condition)");
        System.out.println("5. 性能更高（在低竞争情况下）");
        
        System.out.println("\nsynchronized关键字优势：");
        System.out.println("1. 语法更简洁");
        System.out.println("2. 自动释放锁（即使出现异常）");
        System.out.println("3. JVM优化更成熟");
        System.out.println("4. 支持锁升级和降级");
        System.out.println("5. 内存语义更清晰");
    }

    /**
     * 运行所有Lock演示
     */
    public static void runAllDemos() {
        System.out.println("========================================");
        System.out.println("            Lock接口演示               ");
        System.out.println("========================================");
        
        // 1. 演示ReentrantLock
        demonstrateReentrantLock();
        
        // 2. 演示读写锁
        demonstrateReadWriteLock();
        
        // 3. 演示条件变量
        demonstrateCondition();
        
        // 4. 演示Lock与synchronized的区别
        demonstrateLockVsSynchronized();
        
        System.out.println("\n========================================");
        System.out.println("           Lock接口演示完成             ");
        System.out.println("========================================");
    }
}