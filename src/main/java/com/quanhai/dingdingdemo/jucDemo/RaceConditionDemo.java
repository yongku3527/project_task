package com.quanhai.dingdingdemo.jucDemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 竞态条件演示类
 * 
 * 本演示包含：
 * 1. 什么是竞态条件以及常见场景
 * 2. 计数器竞态条件演示
 * 3. 银行账户竞态条件演示
 * 4. 商品库存竞态条件演示
 * 5. 竞态条件检测和诊断
 * 6. 各种解决方案：synchronized、Lock、Atomic类、volatile、ThreadLocal
 * 7. 性能对比分析
 * 8. 最佳实践指南
 */
public class RaceConditionDemo {

    /**
     * 任务执行结果统计
     */
    static class ExecutionResult {
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final List<Long> executionTimes = new CopyOnWriteArrayList<>();

        public void recordSuccess(long executionTime) {
            successCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            executionTimes.add(executionTime);
        }

        public void recordFailure() {
            failureCount.incrementAndGet();
        }

        public void printStats(String title) {
            System.out.println("\n=== " + title + " 统计结果 ===");
            System.out.println("成功操作: " + successCount.get());
            System.out.println("失败操作: " + failureCount.get());
            
            if (successCount.get() > 0) {
                long avgTime = totalExecutionTime.get() / successCount.get();
                System.out.println("平均执行时间: " + avgTime + "ms");
            }
            System.out.println("=========================\n");
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailureCount() {
            return failureCount.get();
        }

        public void reset() {
            successCount.set(0);
            failureCount.set(0);
            totalExecutionTime.set(0);
            executionTimes.clear();
        }
    }

    /**
     * 计数器类 - 演示不同同步方式的竞态条件
     */
    static class Counter {
        // 1. 无同步的计数器（存在竞态条件）
        private int unsafeCounter = 0;

        // 2. 使用volatile的计数器（只能保证可见性，不能保证原子性）
        private volatile int volatileCounter = 0;

        // 3. 使用synchronized的计数器
        private int synchronizedCounter = 0;

        // 4. 使用AtomicInteger的计数器
        private AtomicInteger atomicCounter = new AtomicInteger(0);

        // 5. 使用Lock的计数器
        private int lockCounter = 0;
        private final Lock lock = new ReentrantLock();

        // 6. ThreadLocal计数器（每个线程独立）
        private static final ThreadLocal<Integer> threadLocalCounter = 
            ThreadLocal.withInitial(() -> 0);

        /**
         * 线程不安全的计数器操作
         */
        public void incrementUnsafe() {
            unsafeCounter++; // 典型的竞态条件：读-修改-写操作
        }

        public int getUnsafeCounter() {
            return unsafeCounter;
        }

        /**
         * 使用volatile的计数器（只保证可见性，不保证原子性）
         */
        public void incrementVolatile() {
            volatileCounter++; // 仍然存在竞态条件
        }

        public int getVolatileCounter() {
            return volatileCounter;
        }

        /**
         * 使用synchronized的计数器（线程安全）
         */
        public synchronized void incrementSynchronized() {
            synchronizedCounter++;
        }

        public synchronized int getSynchronizedCounter() {
            return synchronizedCounter;
        }

        /**
         * 使用AtomicInteger的计数器（线程安全且高效）
         */
        public void incrementAtomic() {
            atomicCounter.incrementAndGet();
        }

        public int getAtomicCounter() {
            return atomicCounter.get();
        }

        /**
         * 使用Lock的计数器（线程安全）
         */
        public void incrementLock() {
            lock.lock();
            try {
                lockCounter++;
            } finally {
                lock.unlock();
            }
        }

        public int getLockCounter() {
            lock.lock();
            try {
                return lockCounter;
            } finally {
                lock.unlock();
            }
        }

        /**
         * ThreadLocal计数器（每个线程独立计数）
         */
        public void incrementThreadLocal() {
            int current = threadLocalCounter.get();
            threadLocalCounter.set(current + 1);
        }

        public int getThreadLocalCounter() {
            return threadLocalCounter.get();
        }

        public static int getCurrentThreadLocalCounter() {
            return threadLocalCounter.get();
        }
    }

    /**
     * 银行账户类 - 演示金额操作的竞态条件
     */
    static class BankAccount {
        private final String accountNumber;
        private volatile double balance; // 使用volatile保证可见性

        // 原子操作：使用AtomicReference保证引用操作的原子性
        private final AtomicReference<Double> atomicBalance;

        public BankAccount(String accountNumber, double initialBalance) {
            this.accountNumber = accountNumber;
            this.balance = initialBalance;
            this.atomicBalance = new AtomicReference<>(initialBalance);
        }

        /**
         * 不安全的存款操作
         */
        public void unsafeDeposit(double amount) {
            double current = balance;
            try {
                Thread.sleep(1); // 模拟处理时间，增加竞态条件发生概率
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            balance = current + amount; // 竞态条件：读取-修改-写入
        }

        /**
         * 使用synchronized的安全存款
         */
        public synchronized void safeDepositSynced(double amount) {
            double current = balance;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            balance = current + amount;
        }

        /**
         * 使用Lock的安全存款
         */
        private final Lock balanceLock = new ReentrantLock();

        public void safeDepositLock(double amount) {
            balanceLock.lock();
            try {
                double current = balance;
                Thread.sleep(1);
                balance = current + amount;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                balanceLock.unlock();
            }
        }

        /**
         * 使用CAS操作的安全存款
         */
        public void safeDepositCAS(double amount) {
            while (true) {
                double current = balance;
                double newBalance = current + amount;
                
                // 原子操作：如果当前值没有变化，则设置为新值
                if (compareAndSet(current, newBalance)) {
                    break;
                }
                // 如果值发生变化，重试
            }
        }

        /**
         * CAS操作实现
         */
        private boolean compareAndSet(double expected, double newValue) {
            // 这里简化实现，实际应该使用AtomicReference
            synchronized (this) {
                if (balance == expected) {
                    balance = newValue;
                    return true;
                }
                return false;
            }
        }

        /**
         * 原子性存款操作
         */
        public void atomicDeposit(double amount) {
            while (true) {
                Double current = atomicBalance.get();
                Double newBalance = current + amount;
                
                if (atomicBalance.compareAndSet(current, newBalance)) {
                    break;
                }
            }
        }

        public double getBalance() {
            return balance;
        }

        public double getAtomicBalance() {
            return atomicBalance.get();
        }

        public String getAccountNumber() {
            return accountNumber;
        }
    }

    /**
     * 商品库存类 - 演示库存操作的竞态条件
     */
    static class ProductInventory {
        private final String productId;
        private volatile int stock;

        // 使用AtomicInteger保证库存的原子性操作
        private final AtomicInteger atomicStock;

        public ProductInventory(String productId, int initialStock) {
            this.productId = productId;
            this.stock = initialStock;
            this.atomicStock = new AtomicInteger(initialStock);
        }

        /**
         * 不安全的库存扣减操作
         */
        public boolean unsafeDeductStock(int quantity) {
            int current = stock;
            try {
                Thread.sleep(2); // 模拟处理时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (current >= quantity) {
                stock = current - quantity; // 竞态条件
                return true;
            }
            return false;
        }

        /**
         * 使用synchronized的安全库存扣减
         */
        public synchronized boolean safeDeductStockSynced(int quantity) {
            int current = stock;
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (current >= quantity) {
                stock = current - quantity;
                return true;
            }
            return false;
        }

        /**
         * 使用AtomicInteger的安全库存扣减
         */
        public boolean safeDeductStockAtomic(int quantity) {
            while (true) {
                int current = atomicStock.get();
                
                if (current < quantity) {
                    return false; // 库存不足
                }
                
                int newStock = current - quantity;
                
                // 原子更新：如果当前值没有变化，则更新为新值
                if (atomicStock.compareAndSet(current, newStock)) {
                    return true; // 成功扣减
                }
                // 如果值发生变化，重试
            }
        }

        /**
         * 使用LongAdder的库存统计（高并发场景优化）
         */
        private final LongAdder totalSold = new LongAdder();

        public void recordSale(int quantity) {
            totalSold.add(quantity);
        }

        public long getTotalSold() {
            return totalSold.sum();
        }

        public int getStock() {
            return stock;
        }

        public int getAtomicStock() {
            return atomicStock.get();
        }

        public String getProductId() {
            return productId;
        }
    }

    /**
     * 演示计数器竞态条件
     */
    public static void demonstrateCounterRaceCondition() {
        System.out.println("\n========== 计数器竞态条件演示 ==========");
        
        Counter counter = new Counter();
        ExecutionResult result = new ExecutionResult();
        int threadCount = 10;
        int operationsPerThread = 1000;
        int expectedValue = threadCount * operationsPerThread;

        System.out.println("测试配置: " + threadCount + " 个线程，每个线程执行 " + 
                         operationsPerThread + " 次递增操作");
        System.out.println("期望结果: " + expectedValue);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 测试线程不安全计数器
        System.out.println("\n--- 测试线程不安全计数器 ---");
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.incrementUnsafe();
                    }
                    result.recordSuccess(System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    result.recordFailure();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("不安全计数器结果: " + counter.getUnsafeCounter());
        System.out.println("数据丢失: " + (expectedValue - counter.getUnsafeCounter()));
        result.printStats("不安全计数器");

        // 重置计数器和结果
        result.reset();

        // 测试volatile计数器
        System.out.println("\n--- 测试volatile计数器 ---");
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.incrementVolatile();
                    }
                    result.recordSuccess(System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    result.recordFailure();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("volatile计数器结果: " + counter.getVolatileCounter());
        System.out.println("数据丢失: " + (expectedValue - counter.getVolatileCounter()));
        result.printStats("volatile计数器");

        // 测试synchronized计数器
        System.out.println("\n--- 测试synchronized计数器 ---");
        result.reset();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.incrementSynchronized();
                    }
                    result.recordSuccess(System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    result.recordFailure();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("synchronized计数器结果: " + counter.getSynchronizedCounter());
        System.out.println("数据准确性: " + (counter.getSynchronizedCounter() == expectedValue ? "100%" : "错误"));
        result.printStats("synchronized计数器");

        // 测试AtomicInteger计数器
        System.out.println("\n--- 测试AtomicInteger计数器 ---");
        result.reset();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.incrementAtomic();
                    }
                    result.recordSuccess(System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    result.recordFailure();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("AtomicInteger计数器结果: " + counter.getAtomicCounter());
        System.out.println("数据准确性: " + (counter.getAtomicCounter() == expectedValue ? "100%" : "错误"));
        result.printStats("AtomicInteger计数器");
    }

    /**
     * 演示银行账户竞态条件
     */
    public static void demonstrateBankAccountRaceCondition() {
        System.out.println("\n========== 银行账户竞态条件演示 ==========");
        
        BankAccount account = new BankAccount("ACC001", 10000);
        int threadCount = 5;
        double depositAmount = 1000;
        int operationsPerThread = 100;
        double expectedFinalBalance = 10000 + threadCount * operationsPerThread * depositAmount;

        System.out.println("测试配置: " + threadCount + " 个线程，每个线程执行 " + 
                         operationsPerThread + " 次存款操作，每次存款 " + depositAmount);
        System.out.println("期望最终余额: " + expectedFinalBalance);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 测试不安全存款
        System.out.println("\n--- 测试不安全存款 ---");
        BankAccount account1 = new BankAccount("ACC001", 10000); // 重置账户
        final double finalDepositAmount1 = depositAmount; // 创建final变量
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    account1.unsafeDeposit(finalDepositAmount1);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("不安全存款结果: " + account1.getBalance());
        System.out.println("资金损失: " + (expectedFinalBalance - account1.getBalance()));

        // 测试原子性存款
        System.out.println("\n--- 测试原子性存款 ---");
        BankAccount account2 = new BankAccount("ACC001", 10000); // 重置账户
        final double finalDepositAmount2 = depositAmount; // 创建final变量
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    account2.atomicDeposit(finalDepositAmount2);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("原子性存款结果: " + account2.getAtomicBalance());
        System.out.println("数据准确性: " + (Math.abs(account2.getAtomicBalance() - expectedFinalBalance) < 0.01 ? "100%" : "错误"));
    }

    /**
     * 演示商品库存竞态条件
     */
    public static void demonstrateInventoryRaceCondition() {
        System.out.println("\n========== 商品库存竞态条件演示 ==========");
        
        ProductInventory product = new ProductInventory("PROD001", 1000);
        int threadCount = 10;
        int deductionQuantity = 10;
        int operationsPerThread = 100;
        int expectedFinalStock = 1000 - threadCount * operationsPerThread * deductionQuantity;

        System.out.println("测试配置: " + threadCount + " 个线程，每个线程执行 " + 
                         operationsPerThread + " 次扣减操作，每次扣减 " + deductionQuantity);
        System.out.println("期望最终库存: " + expectedFinalStock);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 测试不安全库存扣减
        System.out.println("\n--- 测试不安全库存扣减 ---");
        ProductInventory product1 = new ProductInventory("PROD001", 1000); // 重置库存
        final AtomicInteger unsafeSuccessCount = new AtomicInteger(0);
        final int finalDeductionQuantity1 = deductionQuantity;

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    if (product1.unsafeDeductStock(finalDeductionQuantity1)) {
                        unsafeSuccessCount.incrementAndGet();
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

        System.out.println("不安全扣减成功次数: " + unsafeSuccessCount.get());
        System.out.println("不安全库存结果: " + product1.getStock());
        System.out.println("预期成功次数: " + (threadCount * operationsPerThread));
        System.out.println("超卖问题: " + (unsafeSuccessCount.get() > threadCount * operationsPerThread ? "存在" : "无"));

        // 测试原子性库存扣减
        System.out.println("\n--- 测试原子性库存扣减 ---");
        ProductInventory product2 = new ProductInventory("PROD001", 1000); // 重置库存
        final AtomicInteger atomicSuccessCount = new AtomicInteger(0);
        final int finalDeductionQuantity2 = deductionQuantity;

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    if (product2.safeDeductStockAtomic(finalDeductionQuantity2)) {
                        atomicSuccessCount.incrementAndGet();
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

        System.out.println("原子性扣减成功次数: " + atomicSuccessCount.get());
        System.out.println("原子性库存结果: " + product2.getAtomicStock());
        System.out.println("数据准确性: " + (atomicSuccessCount.get() == threadCount * operationsPerThread && 
                                               product2.getAtomicStock() == expectedFinalStock ? "100%" : "错误"));
    }

    /**
     * 竞态条件检测和诊断工具
     */
    static class RaceConditionDetector {
        private static final ConcurrentMap<String, AtomicInteger> raceConditionDetections = 
            new ConcurrentHashMap<>();

        /**
         * 检测可能的竞态条件
         */
        public static void detectRaceCondition(String location, int threadId) {
            AtomicInteger counter = raceConditionDetections.computeIfAbsent(location, k -> new AtomicInteger(0));
            int current = counter.incrementAndGet();
            
            if (current > 1) {
                System.out.println("⚠️  检测到竞态条件在位置: " + location + 
                                 ", 当前线程: " + threadId + 
                                 ", 检测次数: " + current);
            }
        }

        /**
         * 竞态条件严重程度评估
         */
        public static void assessRaceConditionSeverity() {
            System.out.println("\n=== 竞态条件严重程度评估 ===");
            
            raceConditionDetections.forEach((location, count) -> {
                int detectionCount = count.get();
                String severity;
                
                if (detectionCount < 5) {
                    severity = "低风险";
                } else if (detectionCount < 20) {
                    severity = "中风险";
                } else {
                    severity = "高风险";
                }
                
                System.out.println("位置: " + location + ", 检测次数: " + detectionCount + ", 风险等级: " + severity);
            });
            
            raceConditionDetections.clear(); // 清理统计
        }

        /**
         * 线程安全分析报告
         */
        public static void generateSafetyReport() {
            System.out.println("\n=== 线程安全分析报告 ===");
            System.out.println("检测时间: " + java.time.LocalDateTime.now());
            System.out.println("总检测位置数: " + raceConditionDetections.size());
            
            int totalDetections = raceConditionDetections.values().stream()
                .mapToInt(AtomicInteger::get).sum();
            System.out.println("总检测次数: " + totalDetections);
            
            if (totalDetections == 0) {
                System.out.println("状态: ✅ 未检测到竞态条件");
            } else {
                System.out.println("状态: ⚠️  检测到竞态条件");
            }
        }
    }

    /**
     * 性能对比分析
     */
    public static void performanceComparison() {
        System.out.println("\n========== 性能对比分析 ==========");
        
        Counter counter = new Counter();
        int threadCount = 8;
        int operationsPerThread = 10000;
        
        // 性能测试配置
        System.out.println("性能测试配置: " + threadCount + " 线程 × " + operationsPerThread + " 操作");
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 测试AtomicInteger性能
        long atomicStart = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    counter.incrementAtomic();
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long atomicEnd = System.nanoTime();
        long atomicTime = atomicEnd - atomicStart;
        
        System.out.println("AtomicInteger时间: " + (atomicTime / 1_000_000) + "ms");
        
        // 重置线程池
        executor = Executors.newFixedThreadPool(threadCount);
        
        // 测试synchronized性能
        long syncStart = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    counter.incrementSynchronized();
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long syncEnd = System.nanoTime();
        long syncTime = syncEnd - syncStart;
        
        System.out.println("synchronized时间: " + (syncTime / 1_000_000) + "ms");
        
        // 性能对比结果
        System.out.println("\n性能对比结果:");
        System.out.println("AtomicInteger相对性能: " + 
                         String.format("%.2f", (double) atomicTime / syncTime * 100) + "%");
        System.out.println("synchronized相对性能: 基准 (100%)");
    }

    /**
     * 竞态条件最佳实践
     */
    public static void bestPractices() {
        System.out.println("\n========== 竞态条件最佳实践 ==========");
        
        System.out.println("1. 识别竞态条件的场景:");
        System.out.println("   - 读-修改-写操作 (counter++)");
        System.out.println("   - 检查-操作模式 (if(check) { action() })");
        System.out.println("   - 多个线程访问共享可变状态");
        
        System.out.println("\n2. 选择合适的同步机制:");
        System.out.println("   - 简单计数器: 使用AtomicInteger");
        System.out.println("   - 复杂操作: 使用synchronized或Lock");
        System.out.println("   - 只读操作: 使用volatile");
        System.out.println("   - 线程隔离: 使用ThreadLocal");
        
        System.out.println("\n3. 避免竞态条件的策略:");
        System.out.println("   - 不可变对象: 状态不可改变");
        System.out.println("   - 线程封闭: 每个线程拥有自己的数据");
        System.out.println("   - 原子操作: 使用CAS等原子指令");
        System.out.println("   - 最小化共享状态: 减少需要同步的数据");
        
        System.out.println("\n4. 性能优化建议:");
        System.out.println("   - 无锁数据结构: ConcurrentLinkedQueue");
        System.out.println("   - 分段锁: ConcurrentHashMap的分段");
        System.out.println("   - LongAdder: 高并发计数器");
        System.out.println("   - 避免过度同步: 减少锁的持有时间");
        
        System.out.println("\n5. 测试和调试:");
        System.out.println("   - 使用Thread.sleep增加竞态条件发生概率");
        System.out.println("   - 压力测试和并发测试");
        System.out.println("   - 死锁检测工具");
        System.out.println("   - 代码审查和静态分析");
    }

    /**
     * 运行所有竞态条件演示
     */
    public static void runAllDemos() {
        System.out.println("========================================");
        System.out.println("          竞态条件演示                ");
        System.out.println("========================================");
        
        // 1. 计数器竞态条件演示
        demonstrateCounterRaceCondition();
        
        // 2. 银行账户竞态条件演示
        demonstrateBankAccountRaceCondition();
        
        // 3. 商品库存竞态条件演示
        demonstrateInventoryRaceCondition();
        
        // 4. 性能对比分析
        performanceComparison();
        
        // 5. 竞态条件检测
        RaceConditionDetector.assessRaceConditionSeverity();
        RaceConditionDetector.generateSafetyReport();
        
        // 6. 最佳实践指南
        bestPractices();
        
        System.out.println("\n========================================");
        System.out.println("         竞态条件演示完成              ");
        System.out.println("========================================");
    }
}