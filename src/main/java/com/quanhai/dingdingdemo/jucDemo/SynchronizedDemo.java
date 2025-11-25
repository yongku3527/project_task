package com.quanhai.dingdingdemo.jucDemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * synchronized关键字演示类
 * 
 * 本演示包含：
 * 1. 实例方法同步 - 使用synchronized修饰实例方法
 * 2. 静态方法同步 - 使用synchronized修饰静态方法  
 * 3. 代码块同步 - 使用synchronized修饰代码块
 * 4. 演示未同步时的竞态条件问题
 * 5. 演示同步后的正确性保证
 */
public class SynchronizedDemo {

    /**
     * 银行账户类 - 演示实例方法同步
     */
    static class BankAccount {
        private String accountName;
        private double balance;

        public BankAccount(String accountName, double balance) {
            this.accountName = accountName;
            this.balance = balance;
        }

        /**
         * 同步的存款方法
         * 使用synchronized关键字确保线程安全
         * 当一个线程执行此方法时，其他线程无法同时执行此方法或此对象的其他同步方法
         */
        public synchronized void deposit(double amount, String threadName) {
            System.out.println(threadName + " 正在存款: " + amount);
            
            // 模拟处理时间
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            double newBalance = balance + amount;
            System.out.println(threadName + " 存款完成，当前余额: " + balance + " -> " + newBalance);
            this.balance = newBalance;
        }

        /**
         * 同步的取款方法
         * 同样使用synchronized确保线程安全
         */
        public synchronized void withdraw(double amount, String threadName) {
            if (balance >= amount) {
                System.out.println(threadName + " 正在取款: " + amount);
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                double newBalance = balance - amount;
                System.out.println(threadName + " 取款完成，当前余额: " + balance + " -> " + newBalance);
                this.balance = newBalance;
            } else {
                System.out.println(threadName + " 取款失败，余额不足: " + balance + " < " + amount);
            }
        }

        /**
         * 非同步方法 - 演示竞态条件
         * 这个方法不是同步的，多个线程可能同时修改balance
         */
        public void unsafeUpdateBalance(double amount, String threadName) {
            System.out.println(threadName + " 非安全更新余额: " + amount);
            
            // 这里存在竞态条件：多个线程可能同时读取和写入balance
            double currentBalance = balance;
            System.out.println(threadName + " 读取到余额: " + currentBalance);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            balance = currentBalance + amount;
            System.out.println(threadName + " 非安全更新完成，当前余额: " + balance);
        }

        /**
         * 安全的余额查询方法
         * 由于balance是double类型，读操作通常不需要同步，
         * 但为了完整性，这里也使用synchronized
         */
        public synchronized double getBalance() {
            return balance;
        }

        @Override
        public String toString() {
            return accountName + " - 余额: " + balance;
        }
    }

    /**
     * 银行账户类 - 演示静态方法同步和代码块同步
     */
    static class Bank {
        private static double totalBalance = 10000; // 静态变量，所有实例共享
        private String name;

        public Bank(String name) {
            this.name = name;
        }

        /**
         * 静态方法同步
         * 锁住的是Bank类的Class对象，所有实例共享同一个锁
         */
        public static synchronized void staticDeposit(double amount, String threadName) {
            System.out.println(threadName + " 静态同步存款: " + amount);
            
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            totalBalance += amount;
            System.out.println(threadName + " 静态同步完成，总余额: " + totalBalance);
        }

        /**
         * 静态方法同步 - 取款
         */
        public static synchronized void staticWithdraw(double amount, String threadName) {
            if (totalBalance >= amount) {
                System.out.println(threadName + " 静态同步取款: " + amount);
                
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                totalBalance -= amount;
                System.out.println(threadName + " 静态同步取款完成，总余额: " + totalBalance);
            } else {
                System.out.println(threadName + " 静态同步取款失败，余额不足");
            }
        }

        /**
         * 代码块同步示例
         * 锁定当前对象实例
         */
        public void codeBlockDeposit(double amount, String threadName) {
            System.out.println(threadName + " 进入代码块同步存款方法");
            
            // 同步代码块，锁住当前对象
            synchronized (this) {
                System.out.println(threadName + " 获得对象锁，准备存款");
                
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                totalBalance += amount;
                System.out.println(threadName + " 代码块同步完成，总余额: " + totalBalance);
            }
        }

        /**
         * 代码块同步示例 - 锁定其他对象
         */
        private final Object lockObject = new Object();
        
        public void differentLockDeposit(double amount, String threadName) {
            System.out.println(threadName + " 进入不同锁同步方法");
            
            // 锁定特定的对象，而不是this
            synchronized (lockObject) {
                System.out.println(threadName + " 获得专用锁，准备存款");
                
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                totalBalance += amount;
                System.out.println(threadName + " 不同锁同步完成，总余额: " + totalBalance);
            }
        }

        public static double getTotalBalance() {
            return totalBalance;
        }

        @Override
        public String toString() {
            return name + " - 总余额: " + totalBalance;
        }
    }

    /**
     * 演示实例方法同步
     */
    public static void demonstrateInstanceMethodSync() {
        System.out.println("\n========== 演示实例方法同步 ==========");
        
        BankAccount account = new BankAccount("测试账户", 1000);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 启动多个线程同时进行存款操作
        for (int i = 1; i <= 4; i++) {
            final String threadName = "线程-" + i;
            final double amount = 200 * i;
            
            executor.submit(() -> {
                account.deposit(amount, threadName);
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("最终余额: " + account.getBalance());
    }

    /**
     * 演示非同步方法的竞态条件
     */
    public static void demonstrateRaceCondition() {
        System.out.println("\n========== 演示非同步方法的竞态条件 ==========");
        
        BankAccount account = new BankAccount("竞态条件测试账户", 1000);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // 多个线程同时调用非同步方法
        for (int i = 1; i <= 3; i++) {
            final String threadName = "竞态线程-" + i;
            final double amount = 100;
            
            executor.submit(() -> {
                for (int j = 0; j < 3; j++) {
                    account.unsafeUpdateBalance(amount, threadName);
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("期望余额: 1900, 实际余额: " + account.getBalance());
    }

    /**
     * 演示静态方法同步
     */
    public static void demonstrateStaticMethodSync() {
        System.out.println("\n========== 演示静态方法同步 ==========");
        
        Bank bank1 = new Bank("银行1");
        Bank bank2 = new Bank("银行2");
        ExecutorService executor = Executors.newFixedThreadPool(6);
        
        // 多个线程同时操作静态方法
        for (int i = 1; i <= 6; i++) {
            final String threadName = "静态线程-" + i;
            final double amount = 500 * i;
            
            if (i % 2 == 0) {
                executor.submit(() -> Bank.staticDeposit(amount, threadName));
            } else {
                executor.submit(() -> Bank.staticWithdraw(amount, threadName));
            }
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("静态同步后总余额: " + Bank.getTotalBalance());
    }

    /**
     * 演示代码块同步
     */
    public static void demonstrateCodeBlockSync() {
        System.out.println("\n========== 演示代码块同步 ==========");
        
        Bank bank = new Bank("代码块同步银行");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 测试对象锁
        for (int i = 1; i <= 2; i++) {
            final String threadName = "对象锁线程-" + i;
            final double amount = 300 * i;
            
            executor.submit(() -> bank.codeBlockDeposit(amount, threadName));
        }
        
        // 测试专用锁
        for (int i = 3; i <= 4; i++) {
            final String threadName = "专用锁线程-" + i;
            final double amount = 400 * (i - 2);
            
            executor.submit(() -> bank.differentLockDeposit(amount, threadName));
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("代码块同步后总余额: " + Bank.getTotalBalance());
    }

    /**
     * 运行所有synchronized演示
     */
    public static void runAllDemos() {
        System.out.println("========================================");
        System.out.println("         synchronized关键字演示         ");
        System.out.println("========================================");
        
        // 1. 演示实例方法同步
        demonstrateInstanceMethodSync();
        
        // 2. 演示竞态条件
        demonstrateRaceCondition();
        
        // 3. 演示静态方法同步
        demonstrateStaticMethodSync();
        
        // 4. 演示代码块同步
        demonstrateCodeBlockSync();
        
        System.out.println("\n========================================");
        System.out.println("         synchronized演示完成         ");
        System.out.println("========================================");
    }
}