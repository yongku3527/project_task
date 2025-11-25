package com.quanhai.dingdingdemo.jucDemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 死锁演示类
 * 
 * 本演示包含：
 * 1. 经典死锁演示 - 两个线程互相等待对方持有的资源
 * 2. 死锁检测和诊断
 * 3. 死锁预防策略
 * 4. 使用tryLock避免死锁
 * 5. 资源分配图分析
 * 6. 死锁恢复机制
 */
public class DeadlockDemo {

    /**
     * 资源类 - 模拟有限的共享资源
     */
    static class Resource {
        private final String name;
        private final Lock lock = new ReentrantLock();

        public Resource(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Lock getLock() {
            return lock;
        }

        @Override
        public String toString() {
            return "Resource[" + name + "]";
        }
    }

    /**
     * 账户类 - 演示银行转账死锁
     */
    static class Account {
        private final String name;
        private final Lock lock = new ReentrantLock();
        private double balance;

        public Account(String name, double balance) {
            this.name = name;
            this.balance = balance;
        }

        public String getName() {
            return name;
        }

        public Lock getLock() {
            return lock;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        @Override
        public String toString() {
            return name + "(余额:" + balance + ")";
        }
    }

    /**
     * 经典死锁演示 - 两个线程互相持有对方需要的资源
     */
    static class ClassicDeadlockDemo {
        private final Resource resourceA = new Resource("资源A");
        private final Resource resourceB = new Resource("资源B");

        /**
         * 线程1：先获取资源A，再获取资源B
         */
        public void threadOneOperation() {
            System.out.println("线程1 开始执行，尝试获取资源顺序: A -> B");
            
            synchronized (resourceA) {
                System.out.println("线程1 获得资源A: " + resourceA);
                
                try {
                    Thread.sleep(100); // 模拟处理时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("线程1 持有资源A，尝试获取资源B...");
                
                synchronized (resourceB) {
                    System.out.println("线程1 获得资源B: " + resourceB);
                    // 执行任务
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                System.out.println("线程1 完成操作，释放所有资源");
            }
        }

        /**
         * 线程2：先获取资源B，再获取资源A（相反顺序）
         */
        public void threadTwoOperation() {
            System.out.println("线程2 开始执行，尝试获取资源顺序: B -> A");
            
            synchronized (resourceB) {
                System.out.println("线程2 获得资源B: " + resourceB);
                
                try {
                    Thread.sleep(150); // 模拟处理时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("线程2 持有资源B，尝试获取资源A...");
                
                synchronized (resourceA) {
                    System.out.println("线程2 获得资源A: " + resourceA);
                    // 执行任务
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                System.out.println("线程2 完成操作，释放所有资源");
            }
        }

        /**
         * 运行经典死锁演示
         */
        public void runDeadlockDemo() {
            System.out.println("=== 经典死锁演示 ===");
            
            Thread thread1 = new Thread(() -> threadOneOperation(), "死锁线程-1");
            Thread thread2 = new Thread(() -> threadTwoOperation(), "死锁线程-2");
            
            thread1.start();
            thread2.start();
            
            // 等待一段时间看是否发生死锁
            try {
                thread1.join(5000); // 等待最多5秒
                thread2.join(5000);
                
                if (thread1.isAlive() || thread2.isAlive()) {
                    System.out.println("检测到死锁！线程相互等待，无法继续执行。");
                    
                    // 检测死锁
                    detectDeadlock();
                    
                    // 强制中断线程
                    thread1.interrupt();
                    thread2.interrupt();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * 简单死锁检测
         */
        private void detectDeadlock() {
            System.out.println("执行死锁检测...");
            
            // 获取所有线程
            Thread[] threads = new Thread[Thread.activeCount() * 2];
            int threadCount = Thread.enumerate(threads);
            
            System.out.println("当前活跃线程:");
            for (int i = 0; i < threadCount; i++) {
                if (threads[i] != null) {
                    System.out.println("  - " + threads[i].getName() + " (状态: " + threads[i].getState() + ")");
                    
                    // 打印线程的锁信息
                    Thread.State state = threads[i].getState();
                    if (state == Thread.State.BLOCKED) {
                        StackTraceElement[] stackTrace = threads[i].getStackTrace();
                        for (StackTraceElement element : stackTrace) {
                            if (element.getMethodName().contains("synchronized")) {
                                System.out.println("    在方法 " + element.getMethodName() + 
                                                 " 中等待锁: " + element.getClassName());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 银行转账死锁演示
     */
    static class BankTransferDeadlockDemo {
        private final Account accountA = new Account("账户A", 1000);
        private final Account accountB = new Account("账户B", 1000);

        /**
         * 错误的双账户转账 - 可能导致死锁
         */
        public void unsafeTransfer(Account from, Account to, double amount, String threadName) {
            System.out.println(threadName + " 开始转账: " + from + " -> " + to + ", 金额: " + amount);
            
            // 获取两个账户的锁
            synchronized (from) {
                System.out.println(threadName + " 获得 " + from + " 的锁");
                
                try {
                    Thread.sleep(200); // 模拟验证时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                synchronized (to) {
                    System.out.println(threadName + " 获得 " + to + " 的锁");
                    
                    try {
                        Thread.sleep(300); // 模拟转账时间
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    if (from.getBalance() >= amount) {
                        from.setBalance(from.getBalance() - amount);
                        to.setBalance(to.getBalance() + amount);
                        System.out.println(threadName + " 转账完成: " + from + " -> " + to);
                    } else {
                        System.out.println(threadName + " 转账失败，余额不足");
                    }
                }
            }
        }

        /**
         * 安全的转账 - 按照固定顺序获取锁
         */
        public void safeTransfer(Account from, Account to, double amount, String threadName) {
            System.out.println(threadName + " 开始安全转账: " + from + " -> " + to + ", 金额: " + amount);
            
            // 按照账户名的字母顺序获取锁，确保所有线程按相同顺序获取锁
            Account firstLock = from.getName().compareTo(to.getName()) < 0 ? from : to;
            Account secondLock = from.getName().compareTo(to.getName()) < 0 ? to : from;
            
            synchronized (firstLock) {
                System.out.println(threadName + " 获得第一个锁: " + firstLock);
                
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                synchronized (secondLock) {
                    System.out.println(threadName + " 获得第二个锁: " + secondLock);
                    
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // 实际转账操作
                    if (from.getBalance() >= amount) {
                        from.setBalance(from.getBalance() - amount);
                        to.setBalance(to.getBalance() + amount);
                        System.out.println(threadName + " 安全转账完成: " + from + " -> " + to);
                    } else {
                        System.out.println(threadName + " 安全转账失败，余额不足");
                    }
                }
            }
        }

        /**
         * 使用tryLock避免死锁
         */
        public void transferWithTryLock(Account from, Account to, double amount, String threadName) {
            System.out.println(threadName + " 开始tryLock转账: " + from + " -> " + to + ", 金额: " + amount);
            
            // 尝试按照固定顺序获取锁
            while (true) {
                boolean gotFromLock = from.getLock().tryLock();
                boolean gotToLock = to.getLock().tryLock();
                
                if (gotFromLock && gotToLock) {
                    try {
                        System.out.println(threadName + " 成功获取两个锁");
                        
                        // 执行转账
                        if (from.getBalance() >= amount) {
                            from.setBalance(from.getBalance() - amount);
                            to.setBalance(to.getBalance() + amount);
                            System.out.println(threadName + " tryLock转账完成: " + from + " -> " + to);
                        } else {
                            System.out.println(threadName + " tryLock转账失败，余额不足");
                        }
                        break;
                        
                    } finally {
                        from.getLock().unlock();
                        to.getLock().unlock();
                        System.out.println(threadName + " 释放两个锁");
                    }
                } else {
                    // 释放已获取的锁
                    if (gotFromLock) {
                        from.getLock().unlock();
                    }
                    if (gotToLock) {
                        to.getLock().unlock();
                    }
                    
                    System.out.println(threadName + " 无法获取所有锁，等待重试");
                    
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        /**
         * 运行银行转账死锁演示
         */
        public void runBankTransferDemo() {
            System.out.println("\n=== 银行转账死锁演示 ===");
            
            // 重置账户余额
            accountA.setBalance(1000);
            accountB.setBalance(1000);
            
            System.out.println("初始状态: " + accountA + ", " + accountB);
            
            ExecutorService executor = Executors.newFixedThreadPool(4);
            
            // 启动相反方向的转账操作，容易死锁
            executor.submit(() -> {
                // A -> B
                unsafeTransfer(accountA, accountB, 200, "转账线程-A->B");
            });
            
            executor.submit(() -> {
                // B -> A (相反方向，可能导致死锁)
                unsafeTransfer(accountB, accountA, 150, "转账线程-B->A");
            });
            
            executor.shutdown();
            
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("死锁演示后状态: " + accountA + ", " + accountB);
        }

        /**
         * 运行安全的转账演示
         */
        public void runSafeTransferDemo() {
            System.out.println("\n=== 安全转账演示 ===");
            
            // 重置账户余额
            accountA.setBalance(1000);
            accountB.setBalance(1000);
            
            System.out.println("初始状态: " + accountA + ", " + accountB);
            
            ExecutorService executor = Executors.newFixedThreadPool(4);
            
            // 启动多个相反方向的转账操作
            executor.submit(() -> safeTransfer(accountA, accountB, 200, "安全线程-A->B"));
            executor.submit(() -> safeTransfer(accountB, accountA, 150, "安全线程-B->A"));
            executor.submit(() -> safeTransfer(accountA, accountB, 100, "安全线程-A->B-2"));
            executor.submit(() -> safeTransfer(accountB, accountA, 300, "安全线程-B->A-2"));
            
            executor.shutdown();
            
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("安全转账后状态: " + accountA + ", " + accountB);
        }

        /**
         * 运行tryLock转账演示
         */
        public void runTryLockTransferDemo() {
            System.out.println("\n=== tryLock转账演示 ===");
            
            // 重置账户余额
            accountA.setBalance(1000);
            accountB.setBalance(1000);
            
            System.out.println("初始状态: " + accountA + ", " + accountB);
            
            ExecutorService executor = Executors.newFixedThreadPool(4);
            
            // 启动多个相反方向的转账操作
            executor.submit(() -> transferWithTryLock(accountA, accountB, 200, "TryLock线程-A->B"));
            executor.submit(() -> transferWithTryLock(accountB, accountA, 150, "TryLock线程-B->A"));
            executor.submit(() -> transferWithTryLock(accountA, accountB, 100, "TryLock线程-A->B-2"));
            executor.submit(() -> transferWithTryLock(accountB, accountA, 300, "TryLock线程-B->A-2"));
            
            executor.shutdown();
            
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("TryLock转账后状态: " + accountA + ", " + accountB);
        }
    }

    /**
     * 死锁预防策略演示
     */
    static class DeadlockPreventionDemo {
        private final Resource[] resources;

        public DeadlockPreventionDemo() {
            this.resources = new Resource[]{
                new Resource("资源1"),
                new Resource("资源2"),
                new Resource("资源3")
            };
        }

        /**
         * 策略1：统一锁获取顺序
         * 所有线程按照相同的顺序获取多个锁
         */
        public void unifiedLockOrder(String threadName, int... resourceIndices) {
            System.out.println(threadName + " 开始按照固定顺序获取资源");
            
            // 按照资源索引排序，确保所有线程按相同顺序获取
            int[] sortedIndices = resourceIndices.clone();
            java.util.Arrays.sort(sortedIndices);
            
            synchronized (resources[sortedIndices[0]]) {
                System.out.println(threadName + " 获得资源: " + resources[sortedIndices[0]]);
                
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                synchronized (resources[sortedIndices[1]]) {
                    System.out.println(threadName + " 获得资源: " + resources[sortedIndices[1]]);
                    
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    System.out.println(threadName + " 完成所有资源操作");
                }
            }
        }

        /**
         * 策略2：限时获取锁
         * 使用tryLock(timeout)避免无限等待
         */
        public void timeLimitedLock(String threadName, int... resourceIndices) {
            System.out.println(threadName + " 开始限时获取资源");
            
            boolean[] acquiredLocks = new boolean[resourceIndices.length];
            int acquiredCount = 0;
            
            try {
                for (int i = 0; i < resourceIndices.length; i++) {
                    if (resources[resourceIndices[i]].getLock().tryLock(1000, TimeUnit.MILLISECONDS)) {
                        acquiredLocks[i] = true;
                        acquiredCount++;
                        System.out.println(threadName + " 获得资源: " + resources[resourceIndices[i]]);
                    } else {
                        System.out.println(threadName + " 获取资源 " + resources[resourceIndices[i]] + " 超时");
                        break;
                    }
                }
                
                if (acquiredCount == resourceIndices.length) {
                    // 所有资源获取成功，执行任务
                    System.out.println(threadName + " 获得所有资源，执行任务");
                    Thread.sleep(500);
                }
                
            } catch (InterruptedException e) {
                System.out.println(threadName + " 被中断");
            } finally {
                // 释放所有已获得的锁
                for (int i = resourceIndices.length - 1; i >= 0; i--) {
                    if (acquiredLocks[i]) {
                        resources[resourceIndices[i]].getLock().unlock();
                        System.out.println(threadName + " 释放资源: " + resources[resourceIndices[i]]);
                    }
                }
            }
        }

        /**
         * 策略3：使用超时和重试机制
         */
        public void timeoutAndRetry(String threadName, int maxRetries, int... resourceIndices) {
            System.out.println(threadName + " 开始使用超时重试机制");
            
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                boolean[] acquiredLocks = new boolean[resourceIndices.length];
                boolean allLocksAcquired = true;
                
                try {
                    // 按照固定顺序尝试获取所有锁
                    for (int i = 0; i < resourceIndices.length; i++) {
                        if (resources[resourceIndices[i]].getLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                            acquiredLocks[i] = true;
                        } else {
                            allLocksAcquired = false;
                            break;
                        }
                    }
                    
                    if (allLocksAcquired) {
                        System.out.println(threadName + " 第" + (attempt + 1) + "次重试成功获得所有资源");
                        Thread.sleep(300);
                        break;
                    } else {
                        System.out.println(threadName + " 第" + (attempt + 1) + "次重试失败，释放已获得锁");
                        // 释放已获得的锁
                        for (int i = 0; i < resourceIndices.length; i++) {
                            if (acquiredLocks[i]) {
                                resources[resourceIndices[i]].getLock().unlock();
                            }
                        }
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(200); // 等待一段时间后重试
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        /**
         * 运行死锁预防演示
         */
        public void runPreventionDemo() {
            System.out.println("\n=== 死锁预防策略演示 ===");
            
            ExecutorService executor = Executors.newFixedThreadPool(6);
            
            // 演示统一锁顺序
            executor.submit(() -> unifiedLockOrder("统一锁线程-1", 0, 1, 2));
            executor.submit(() -> unifiedLockOrder("统一锁线程-2", 2, 0, 1));
            executor.submit(() -> unifiedLockOrder("统一锁线程-3", 1, 2, 0));
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 演示限时获取锁
            executor.submit(() -> timeLimitedLock("限时锁线程-1", 0, 1));
            executor.submit(() -> timeLimitedLock("限时锁线程-2", 1, 0));
            
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 演示超时重试
            executor.submit(() -> timeoutAndRetry("重试线程-1", 3, 0, 2));
            executor.submit(() -> timeoutAndRetry("重试线程-2", 3, 2, 0));
            
            executor.shutdown();
            
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 死锁恢复演示
     */
    static class DeadlockRecoveryDemo {
        private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();

        /**
         * 检测死锁并恢复
         */
        public void deadlockDetectionAndRecovery() {
            System.out.println("\n=== 死锁检测与恢复演示 ===");
            
            // 模拟一个可能死锁的场景
            ClassicDeadlockDemo deadlockDemo = new ClassicDeadlockDemo();
            
            System.out.println("启动可能发生死锁的操作...");
            
            Thread deadlockThread1 = new Thread(() -> deadlockDemo.threadOneOperation(), "检测线程-1");
            Thread deadlockThread2 = new Thread(() -> deadlockDemo.threadTwoOperation(), "检测线程-2");
            
            deadlockThread1.start();
            deadlockThread2.start();
            
            // 启动死锁检测器
            startDeadlockDetector();
            
            try {
                Thread.sleep(8000); // 等待8秒
                
                // 检查是否发生死锁
                if (isDeadlockDetected()) {
                    System.out.println("检测到死锁，执行恢复操作...");
                    performDeadlockRecovery();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private volatile boolean deadlockDetected = false;
        private final Object detectorLock = new Object();

        /**
         * 启动死锁检测器
         */
        private void startDeadlockDetector() {
            Thread detector = new Thread(() -> {
                try {
                    Thread.sleep(3000); // 等待3秒让死锁发生
                    
                    synchronized (detectorLock) {
                        deadlockDetected = true;
                        System.out.println("死锁检测器发现死锁情况");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "死锁检测器");
            
            detector.start();
        }

        /**
         * 检查是否检测到死锁
         */
        private boolean isDeadlockDetected() {
            synchronized (detectorLock) {
                return deadlockDetected;
            }
        }

        /**
         * 执行死锁恢复操作
         */
        private void performDeadlockRecovery() {
            System.out.println("执行死锁恢复策略：");
            System.out.println("1. 强制中断部分线程");
            System.out.println("2. 回滚未完成的事务");
            System.out.println("3. 重新启动中断的线程");
            System.out.println("4. 应用安全重试机制");
            
            // 模拟恢复操作
            processedTransactions.clear();
            System.out.println("死锁恢复完成，事务已回滚");
        }
    }

    /**
     * 运行所有死锁演示
     */
    public static void runAllDemos() {
        System.out.println("========================================");
        System.out.println("            死锁演示                   ");
        System.out.println("========================================");
        
        // 1. 经典死锁演示
        ClassicDeadlockDemo classicDemo = new ClassicDeadlockDemo();
        classicDemo.runDeadlockDemo();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 2. 银行转账死锁演示
        BankTransferDeadlockDemo bankDemo = new BankTransferDeadlockDemo();
        bankDemo.runBankTransferDemo();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 3. 安全转账演示
        bankDemo.runSafeTransferDemo();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 4. TryLock转账演示
        bankDemo.runTryLockTransferDemo();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 5. 死锁预防策略演示
        DeadlockPreventionDemo preventionDemo = new DeadlockPreventionDemo();
        preventionDemo.runPreventionDemo();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 6. 死锁恢复演示
        DeadlockRecoveryDemo recoveryDemo = new DeadlockRecoveryDemo();
        recoveryDemo.deadlockDetectionAndRecovery();
        
        System.out.println("\n========================================");
        System.out.println("           死锁演示完成                 ");
        System.out.println("========================================");
        
        System.out.println("\n死锁最佳实践总结：");
        System.out.println("1. 避免嵌套锁 - 减少锁的数量");
        System.out.println("2. 固定锁顺序 - 统一资源访问顺序");
        System.out.println("3. 使用超时机制 - tryLock(timeout)避免无限等待");
        System.out.println("4. 检测和恢复 - 实现死锁检测算法");
        System.out.println("5. 设计重构 - 重新设计架构避免死锁");
    }
}