package com.quanhai.dingdingdemo.jucDemo;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.RejectedExecutionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 线程池演示类
 * 
 * 本演示包含：
 * 1. ThreadPoolExecutor - 基础线程池实现
 * 2. Executors工厂方法 - 各种预定义线程池
 * 3. FixedThreadPool - 固定大小线程池
 * 4. CachedThreadPool - 缓存线程池
 * 5. ScheduledThreadPool - 定时任务线程池
 * 6. SingleThreadExecutor - 单线程线程池
 * 7. 自定义线程工厂和拒绝策略
 * 8. 线程池监控和统计
 * 9. 线程池参数调优
 */
public class ThreadPoolDemo {

    /**
     * 任务执行统计
     */
    static class TaskStats {
        private final AtomicInteger completedTasks = new AtomicInteger(0);
        private final AtomicInteger failedTasks = new AtomicInteger(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final List<Long> executionTimes = new CopyOnWriteArrayList<>();

        public void recordSuccess(long executionTime) {
            completedTasks.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            executionTimes.add(executionTime);
        }

        public void recordFailure() {
            failedTasks.incrementAndGet();
        }

        public void printStats(String poolName) {
            System.out.println("=== " + poolName + " 统计信息 ===");
            System.out.println("完成任务数: " + completedTasks.get());
            System.out.println("失败任务数: " + failedTasks.get());
            
            if (completedTasks.get() > 0) {
                long avgTime = totalExecutionTime.get() / completedTasks.get();
                System.out.println("平均执行时间: " + avgTime + "ms");
                
                long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
                long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
                System.out.println("最小执行时间: " + minTime + "ms");
                System.out.println("最大执行时间: " + maxTime + "ms");
            }
            System.out.println("===========================\n");
        }
    }

    /**
     * 演示任务类
     */
    static class DemoTask implements Runnable {
        private final String taskName;
        private final int executionTime; // 执行时间（毫秒）
        private final TaskStats stats;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        public DemoTask(String taskName, int executionTime, TaskStats stats) {
            this.taskName = taskName;
            this.executionTime = executionTime;
            this.stats = stats;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            String startTimeStr = dateFormat.format(new Date(startTime));
            
            System.out.println("[" + startTimeStr + "] " + taskName + " 开始执行，预计耗时 " + executionTime + "ms");
            
            try {
                // 模拟任务执行
                Thread.sleep(executionTime);
                
                long endTime = System.currentTimeMillis();
                long actualExecutionTime = endTime - startTime;
                String endTimeStr = dateFormat.format(new Date(endTime));
                
                System.out.println("[" + endTimeStr + "] " + taskName + " 执行完成，实际耗时 " + actualExecutionTime + "ms");
                
                stats.recordSuccess(actualExecutionTime);
                
            } catch (InterruptedException e) {
                System.out.println(taskName + " 被中断");
                stats.recordFailure();
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String toString() {
            return taskName + "(预计" + executionTime + "ms)";
        }
    }

    /**
     * 自定义线程工厂
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public CustomThreadFactory(String poolName) {
            this.namePrefix = poolName + "-线程-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            
            // 设置线程属性
            thread.setDaemon(false); // 非守护线程
            thread.setPriority(Thread.NORM_PRIORITY); // 正常优先级
            
            System.out.println("创建新线程: " + thread.getName());
            return thread;
        }
    }

    /**
     * 自定义拒绝策略
     */
    static class CustomRejectedHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("任务被拒绝: " + r.toString() + 
                             ", 当前线程池状态: " + 
                             "活动线程=" + executor.getActiveCount() + 
                             ", 队列大小=" + executor.getQueue().size());
            
            // 可以在这里实现自定义的拒绝逻辑，比如：
            // 1. 记录日志
            // 2. 保存到持久化存储
            // 3. 尝试重新执行
            // 4. 发送告警
        }
    }

    /**
     * 演示FixedThreadPool - 固定大小线程池
     */
    public static void demonstrateFixedThreadPool() {
        System.out.println("\n========== 演示FixedThreadPool ==========");
        
        // 创建固定大小线程池（3个线程）
        ExecutorService executor = Executors.newFixedThreadPool(3, 
            new CustomThreadFactory("FixedPool"));
        
        TaskStats stats = new TaskStats();
        
        // 提交10个任务，但只有3个线程执行
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            DemoTask task = new DemoTask("固定池任务-" + i, 1000 + i * 200, stats);
            Future<?> future = executor.submit(task);
            futures.add(future);
        }
        
        executor.shutdown();
        
        try {
            // 等待所有任务完成
            executor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stats.printStats("FixedThreadPool");
    }

    /**
     * 演示CachedThreadPool - 缓存线程池
     */
    public static void demonstrateCachedThreadPool() {
        System.out.println("\n========== 演示CachedThreadPool ==========");
        
        // 创建缓存线程池（可自动扩展）
        ExecutorService executor = Executors.newCachedThreadPool(
            new CustomThreadFactory("CachedPool"));
        
        TaskStats stats = new TaskStats();
        
        // 提交5个任务，线程池会根据需要创建新线程
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DemoTask task = new DemoTask("缓存池任务-" + i, 500 + i * 300, stats);
            Future<?> future = executor.submit(task);
            futures.add(future);
        }
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stats.printStats("CachedThreadPool");
    }

    /**
     * 演示ScheduledThreadPool - 定时任务线程池
     */
    public static void demonstrateScheduledThreadPool() {
        System.out.println("\n========== 演示ScheduledThreadPool ==========");
        
        // 创建定时任务线程池（2个线程）
        ScheduledExecutorService scheduledExecutor = 
            Executors.newScheduledThreadPool(2, new CustomThreadFactory("ScheduledPool"));
        
        TaskStats stats = new TaskStats();
        
        System.out.println("启动定时任务演示...");
        
        // 1. 延迟执行任务
        ScheduledFuture<?> delayTask = scheduledExecutor.schedule(
            new DemoTask("延迟任务", 800, stats), 
            2, TimeUnit.SECONDS);
        
        // 2. 固定频率执行任务（上一个任务开始后2秒执行下一个）
        ScheduledFuture<?> rateTask = scheduledExecutor.scheduleAtFixedRate(
            new DemoTask("固定频率任务", 1000, stats),
            1, // 初始延迟
            3, // 周期
            TimeUnit.SECONDS);
        
        // 3. 固定延迟执行任务（上一个任务完成后延迟2秒执行下一个）
        ScheduledFuture<?> delayAfterTask = scheduledExecutor.scheduleWithFixedDelay(
            new DemoTask("固定延迟任务", 800, stats),
            1, // 初始延迟
            2, // 延迟
            TimeUnit.SECONDS);
        
        try {
            // 等待一段时间后取消定时任务
            Thread.sleep(15 * 1000);
            
            delayTask.cancel(true);
            rateTask.cancel(true);
            delayAfterTask.cancel(true);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scheduledExecutor.shutdown();
            try {
                scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 演示SingleThreadExecutor - 单线程线程池
     */
    public static void demonstrateSingleThreadExecutor() {
        System.out.println("\n========== 演示SingleThreadExecutor ==========");
        
        // 创建单线程线程池
        ExecutorService executor = Executors.newSingleThreadExecutor(
            new CustomThreadFactory("SinglePool"));
        
        TaskStats stats = new TaskStats();
        
        // 提交6个任务，按顺序执行
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            DemoTask task = new DemoTask("单线程任务-" + i, 800 + i * 100, stats);
            Future<?> future = executor.submit(task);
            futures.add(future);
        }
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stats.printStats("SingleThreadExecutor");
    }

    /**
     * 演示ThreadPoolExecutor自定义配置
     */
    public static void demonstrateCustomThreadPool() {
        System.out.println("\n========== 演示自定义ThreadPoolExecutor ==========");
        
        // 自定义线程池配置
        int corePoolSize = 2;        // 核心线程数
        int maximumPoolSize = 4;     // 最大线程数
        long keepAliveTime = 60;     // 非核心线程存活时间
        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(3); // 有界队列
        
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            timeUnit,
            workQueue,
            new CustomThreadFactory("CustomPool"),
            new CustomRejectedHandler()
        );
        
        TaskStats stats = new TaskStats();
        
        // 设置线程池属性（Java 8中可能不可用，使用替代方案）
        // customExecutor.allowCoreThreadTimeout(true); // 允许核心线程超时
        // 在Java 8中，需要使用反射或创建自定义线程池
        System.out.println("注意：allowCoreThreadTimeout 在某些Java版本中不可用");
        System.out.println("建议在需要时使用自定义线程池实现此功能");
        
        System.out.println("提交7个任务到自定义线程池（核心线程:2, 最大线程:4, 队列容量:3）");
        
        // 提交7个任务，会触发队列满和线程池扩展
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            DemoTask task = new DemoTask("自定义池任务-" + i, 1000 + i * 200, stats);
            try {
                Future<?> future = customExecutor.submit(task);
                futures.add(future);
            } catch (RejectedExecutionException e) {
                System.out.println("任务 " + task.toString() + " 被拒绝");
                stats.recordFailure();
            }
        }
        
        customExecutor.shutdown();
        
        try {
            customExecutor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stats.printStats("自定义ThreadPoolExecutor");
        
        // 打印线程池统计信息
        System.out.println("=== 线程池监控信息 ===");
        System.out.println("核心线程数: " + customExecutor.getCorePoolSize());
        System.out.println("最大线程数: " + customExecutor.getMaximumPoolSize());
        System.out.println("活跃线程数: " + customExecutor.getActiveCount());
        System.out.println("完成任务数: " + customExecutor.getCompletedTaskCount());
        System.out.println("队列大小: " + customExecutor.getQueue().size());
        System.out.println("========================\n");
    }

    /**
     * 演示线程池参数调优
     */
    public static void demonstrateThreadPoolTuning() {
        System.out.println("\n========== 线程池参数调优演示 ==========");
        
        System.out.println("线程池参数调优指南：");
        System.out.println("1. CPU密集型任务：线程数 = CPU核心数");
        System.out.println("2. IO密集型任务：线程数 = CPU核心数 * 2");
        System.out.println("3. 混合型任务：根据性能测试调整");
        System.out.println("4. 队列选择：有界队列防止OOM，无界队列要谨慎");
        System.out.println("5. 拒绝策略：保护系统和业务逻辑");
        
        // 模拟不同场景的线程池配置
        int cpuCores = Runtime.getRuntime().availableProcessors();
        System.out.println("\n当前CPU核心数: " + cpuCores);
        
        // CPU密集型任务线程池
        ExecutorService cpuExecutor = Executors.newFixedThreadPool(cpuCores);
        
        // IO密集型任务线程池
        ExecutorService ioExecutor = Executors.newFixedThreadPool(cpuCores * 2);
        
        // 自定义混合型任务线程池
        ThreadPoolExecutor mixedExecutor = new ThreadPoolExecutor(
            cpuCores,                    // 核心线程数
            cpuCores * 2,               // 最大线程数
            60L, TimeUnit.SECONDS,      // 空闲线程存活时间
            new LinkedBlockingQueue<>(100), // 队列容量
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "混合池-线程-" + counter.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行
        );
        
        System.out.println("创建了三个不同配置的线程池用于测试");
        
        // 关闭线程池
        cpuExecutor.shutdown();
        ioExecutor.shutdown();
        mixedExecutor.shutdown();
    }

    /**
     * 演示Future和Promise模式
     */
    public static void demonstrateFutureOperations() {
        System.out.println("\n========== 演示Future操作 ==========");
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // 1. 提交有返回值的任务
            Callable<String> callableTask = () -> {
                Thread.sleep(1000);
                return "任务执行结果";
            };
            
            Future<String> future = executor.submit(callableTask);
            System.out.println("任务已提交，等待结果...");
            
            // 2. 阻塞等待结果
            String result = future.get(2, TimeUnit.SECONDS);
            System.out.println("任务结果: " + result);
            
            // 3. 检查任务是否完成
            System.out.println("任务是否完成: " + future.isDone());
            
            // 4. 取消任务
            Future<?> anotherFuture = executor.submit(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("任务执行被中断");
                }
            });
            
            Thread.sleep(1000); // 让任务开始执行
            boolean cancelled = anotherFuture.cancel(true);
            System.out.println("任务取消结果: " + cancelled);
            System.out.println("任务是否已取消: " + anotherFuture.isCancelled());
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 运行所有线程池演示
     */
    public static void runAllDemos() {
        System.out.println("========================================");
        System.out.println("            线程池演示                  ");
        System.out.println("========================================");
        
        // 1. 演示FixedThreadPool
        demonstrateFixedThreadPool();
        
        // 2. 演示CachedThreadPool
        demonstrateCachedThreadPool();
        
        // 3. 演示ScheduledThreadPool
        demonstrateScheduledThreadPool();
        
        // 4. 演示SingleThreadExecutor
        demonstrateSingleThreadExecutor();
        
        // 5. 演示自定义ThreadPoolExecutor
        demonstrateCustomThreadPool();
        
        // 6. 演示线程池调优
        demonstrateThreadPoolTuning();
        
        // 7. 演示Future操作
        demonstrateFutureOperations();
        
        System.out.println("\n========================================");
        System.out.println("           线程池演示完成               ");
        System.out.println("========================================");
    }
}