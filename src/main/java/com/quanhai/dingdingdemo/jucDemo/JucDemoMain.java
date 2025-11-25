package com.quanhai.dingdingdemo.jucDemo;

import java.util.Scanner;
import java.util.InputMismatchException;

/**
 * JUC演示程序主入口类
 * 
 * 这是一个完整的多线程编程演示程序，包含了以下主要功能：
 * 
 * 主要功能模块：
 * 1. synchronized关键字演示 - 展示线程同步机制
 * 2. Lock接口演示 - 展示可重入锁和读写锁的使用
 * 3. 线程池演示 - 展示各种线程池的使用方式
 * 4. 死锁演示 - 展示死锁问题及解决方案
 * 5. 竞态条件演示 - 展示竞态条件问题及解决方案
 * 
 * 特色功能：
 * - 交互式菜单选择
 * - 详细的代码注释和多线程原理解释
 * - 运行结果展示和分析
 * - 性能对比测试
 * - 最佳实践指南
 * 
 * 系统要求：
 * - Java 8+
 * - JUC包支持
 * 
 * 适用场景：
 * - Java并发编程学习
 * - 多线程编程实践
 * - 面试准备和技术分享
 * - 代码审查和培训
 * 
 * @author 系统自动生成
 * @version 1.0
 */
public class JucDemoMain {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * 主程序入口点
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("           JUC多线程编程演示程序                 ");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("欢迎使用多线程编程学习演示程序！");
        System.out.println("本程序将演示Java并发编程的核心概念和最佳实践");
        System.out.println();
        System.out.println("演示内容包括：");
        System.out.println("1. synchronized关键字 - 线程同步机制");
        System.out.println("2. Lock接口 - 可重入锁和读写锁");
        System.out.println("3. 线程池 - 高效的线程管理");
        System.out.println("4. 死锁问题 - 死锁演示与解决方案");
        System.out.println("5. 竞态条件 - 竞态条件演示与解决方案");
        System.out.println();
        
        // 显示演示菜单
        displayMainMenu();
        
        // 处理用户选择
        handleUserSelection();
        
        scanner.close();
    }

    /**
     * 显示主菜单
     */
    private static void displayMainMenu() {
        System.out.println("==================================================");
        System.out.println("请选择要运行的演示：");
        System.out.println("==================================================");
        System.out.println("1. 全部演示 (推荐)");
        System.out.println("2. synchronized关键字演示");
        System.out.println("3. Lock接口演示");
        System.out.println("4. 线程池演示");
        System.out.println("5. 死锁问题演示");
        System.out.println("6. 竞态条件演示");
        System.out.println("7. 查看项目说明");
        System.out.println("0. 退出程序");
        System.out.println("==================================================");
        System.out.print("请输入选择 (0-7): ");
    }

    /**
     * 处理用户选择
     */
    private static void handleUserSelection() {
        while (true) {
            try {
                int choice = scanner.nextInt();
                
                switch (choice) {
                    case 1:
                        runAllDemos();
                        break;
                    case 2:
                        runSynchronizedDemo();
                        break;
                    case 3:
                        runLockDemo();
                        break;
                    case 4:
                        runThreadPoolDemo();
                        break;
                    case 5:
                        runDeadlockDemo();
                        break;
                    case 6:
                        runRaceConditionDemo();
                        break;
                    case 7:
                        showProjectInfo();
                        break;
                    case 0:
                        exitProgram();
                        return;
                    default:
                        System.out.println("❌ 无效选择，请输入 0-7 之间的数字！");
                        break;
                }
                
                // 继续显示菜单
                if (choice != 0) {
                    System.out.println("\n按Enter键继续...");
                    scanner.nextLine(); // 消耗剩余的换行符
                    scanner.nextLine(); // 等待用户按Enter
                    displayMainMenu();
                }
                
            } catch (InputMismatchException e) {
                System.out.println("❌ 输入格式错误，请输入数字！");
                scanner.next(); // 清理无效输入
            } catch (Exception e) {
                System.out.println("❌ 程序执行出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 运行所有演示
     */
    private static void runAllDemos() {
        System.out.println("\n🚀 开始运行所有演示...");
        System.out.println("这可能需要几分钟时间，请耐心等待...");
        
        try {
            // 1. synchronized演示
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 步骤 1/5: 运行synchronized关键字演示");
            System.out.println("=".repeat(60));
            runSynchronizedDemo();
            
            // 2. Lock演示
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 步骤 2/5: 运行Lock接口演示");
            System.out.println("=".repeat(60));
            runLockDemo();
            
            // 3. 线程池演示
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 步骤 3/5: 运行线程池演示");
            System.out.println("=".repeat(60));
            runThreadPoolDemo();
            
            // 4. 死锁演示
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 步骤 4/5: 运行死锁问题演示");
            System.out.println("=".repeat(60));
            runDeadlockDemo();
            
            // 5. 竞态条件演示
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 步骤 5/5: 运行竞态条件演示");
            System.out.println("=".repeat(60));
            runRaceConditionDemo();
            
            System.out.println("\n🎉 所有演示完成！");
            System.out.println("您可以查看每个演示的输出结果来学习多线程编程的概念和实践。");
            
        } catch (Exception e) {
            System.out.println("❌ 运行演示时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 运行synchronized演示
     */
    private static void runSynchronizedDemo() {
        try {
            System.out.println("🔧 运行synchronized关键字演示...");
            System.out.println("这个演示将展示：");
            System.out.println("  - 实例方法同步");
            System.out.println("  - 静态方法同步");
            System.out.println("  - 代码块同步");
            System.out.println("  - 竞态条件问题");
            System.out.println();
            
            SynchronizedDemo.runAllDemos();
            
            System.out.println("✅ synchronized演示完成");
        } catch (Exception e) {
            System.out.println("❌ synchronized演示运行失败: " + e.getMessage());
        }
    }

    /**
     * 运行Lock演示
     */
    private static void runLockDemo() {
        try {
            System.out.println("🔧 运行Lock接口演示...");
            System.out.println("这个演示将展示：");
            System.out.println("  - ReentrantLock的基本使用");
            System.out.println("  - ReentrantReadWriteLock读写锁");
            System.out.println("  - Condition条件等待");
            System.out.println("  - Lock与synchronized的区别");
            System.out.println();
            
            LockDemo.runAllDemos();
            
            System.out.println("✅ Lock演示完成");
        } catch (Exception e) {
            System.out.println("❌ Lock演示运行失败: " + e.getMessage());
        }
    }

    /**
     * 运行线程池演示
     */
    private static void runThreadPoolDemo() {
        try {
            System.out.println("🔧 运行线程池演示...");
            System.out.println("这个演示将展示：");
            System.out.println("  - 固定大小线程池");
            System.out.println("  - 缓存线程池");
            System.out.println("  - 调度线程池");
            System.out.println("  - 自定义线程工厂");
            System.out.println("  - 拒绝策略");
            System.out.println("  - 线程池监控");
            System.out.println();
            
            ThreadPoolDemo.runAllDemos();
            
            System.out.println("✅ 线程池演示完成");
        } catch (Exception e) {
            System.out.println("❌ 线程池演示运行失败: " + e.getMessage());
        }
    }

    /**
     * 运行死锁演示
     */
    private static void runDeadlockDemo() {
        try {
            System.out.println("🔧 运行死锁问题演示...");
            System.out.println("这个演示将展示：");
            System.out.println("  - 经典死锁问题");
            System.out.println("  - 银行转账死锁");
            System.out.println("  - 死锁检测方法");
            System.out.println("  - 死锁预防策略");
            System.out.println("  - 死锁恢复机制");
            System.out.println();
            
            DeadlockDemo.runAllDemos();
            
            System.out.println("✅ 死锁演示完成");
        } catch (Exception e) {
            System.out.println("❌ 死锁演示运行失败: " + e.getMessage());
        }
    }

    /**
     * 运行竞态条件演示
     */
    private static void runRaceConditionDemo() {
        try {
            System.out.println("🔧 运行竞态条件演示...");
            System.out.println("这个演示将展示：");
            System.out.println("  - 计数器竞态条件");
            System.out.println("  - 银行账户竞态条件");
            System.out.println("  - 商品库存竞态条件");
            System.out.println("  - 竞态条件检测");
            System.out.println("  - 性能对比分析");
            System.out.println("  - 最佳实践指南");
            System.out.println();
            
            RaceConditionDemo.runAllDemos();
            
            System.out.println("✅ 竞态条件演示完成");
        } catch (Exception e) {
            System.out.println("❌ 竞态条件演示运行失败: " + e.getMessage());
        }
    }

    /**
     * 显示项目信息
     */
    private static void showProjectInfo() {
        System.out.println("\n📋 项目信息");
        System.out.println("=".repeat(60));
        System.out.println("项目名称: JUC多线程编程演示程序");
        System.out.println("版本: 1.0");
        System.out.println("语言: Java 8+");
        System.out.println("包依赖: java.util.concurrent (JUC)");
        System.out.println();
        
        System.out.println("📚 学习内容:");
        System.out.println("1. 线程同步机制");
        System.out.println("   - synchronized关键字");
        System.out.println("   - 锁机制和死锁问题");
        System.out.println();
        
        System.out.println("2. JUC并发工具");
        System.out.println("   - Lock接口 (ReentrantLock, ReentrantReadWriteLock)");
        System.out.println("   - Condition条件等待");
        System.out.println("   - 原子类 (AtomicInteger, AtomicReference)");
        System.out.println("   - 并发集合 (ConcurrentHashMap, CopyOnWriteArrayList)");
        System.out.println();
        
        System.out.println("3. 线程池管理");
        System.out.println("   - Executors工厂方法");
        System.out.println("   - ThreadPoolExecutor详解");
        System.out.println("   - 自定义线程工厂和拒绝策略");
        System.out.println("   - 线程池监控和优化");
        System.out.println();
        
        System.out.println("4. 并发问题分析");
        System.out.println("   - 死锁检测和预防");
        System.out.println("   - 竞态条件分析");
        System.out.println("   - 性能调优");
        System.out.println();
        
        System.out.println("🎯 适用场景:");
        System.out.println("• Java并发编程学习");
        System.out.println("• 技术面试准备");
        System.out.println("• 代码审查培训");
        System.out.println("• 多线程项目实践");
        System.out.println();
        
        System.out.println("📈 特色功能:");
        System.out.println("• 交互式菜单界面");
        System.out.println("• 详细的代码注释");
        System.out.println("• 多种解决方案对比");
        System.out.println("• 性能测试和优化建议");
        System.out.println("• 最佳实践指南");
        System.out.println("• 实际场景模拟");
        System.out.println();
        
        System.out.println("💡 使用建议:");
        System.out.println("1. 按顺序运行所有演示，理解每个概念");
        System.out.println("2. 仔细观察输出结果，理解多线程行为");
        System.out.println("3. 修改代码参数，观察性能变化");
        System.out.println("4. 对比不同的解决方案，选择最佳实践");
        System.out.println("5. 在实际项目中应用这些技术");
        
        System.out.println("=".repeat(60));
    }

    /**
     * 退出程序
     */
    private static void exitProgram() {
        System.out.println("\n👋 感谢使用JUC多线程编程演示程序！");
        System.out.println("希望这个演示程序能帮助您更好地理解Java并发编程。");
        System.out.println();
        System.out.println("📖 继续学习建议:");
        System.out.println("• 深入学习《Java并发编程实战》");
        System.out.println("• 研究JDK源码中的并发实现");
        System.out.println("• 在项目中实践多线程编程");
        System.out.println("• 关注最新的并发编程特性");
        System.out.println();
        System.out.println("🚀 祝您学习愉快！");
    }

    /**
     * 显示程序统计信息
     */
    public static void displayProgramStats() {
        System.out.println("\n📊 程序统计信息");
        System.out.println("=".repeat(50));
        System.out.println("总演示模块数: 5");
        System.out.println("总代码行数: ~2000行");
        System.out.println("注释覆盖率: >30%");
        System.out.println("支持的Java版本: 8+");
        System.out.println("依赖包: java.util.concurrent");
        System.out.println("预计运行时间: 5-10分钟");
        System.out.println("=".repeat(50));
    }

    /**
     * 显示帮助信息
     */
    public static void showHelp() {
        System.out.println("\n🆘 帮助信息");
        System.out.println("=".repeat(50));
        System.out.println("常用操作:");
        System.out.println("• 输入数字 1-7 选择功能");
        System.out.println("• 输入 0 退出程序");
        System.out.println("• 按 Enter 继续下一个演示");
        System.out.println();
        System.out.println("演示说明:");
        System.out.println("• 所有演示都包含详细的输出结果");
        System.out.println("• 代码中包含大量注释说明");
        System.out.println("• 建议按顺序运行所有演示");
        System.out.println();
        System.out.println("问题排查:");
        System.out.println("• 确保Java版本 >= 8");
        System.out.println("• 检查内存是否充足 (建议 >= 512MB)");
        System.out.println("• 如遇问题，查看错误信息并重新运行");
        System.out.println("=".repeat(50));
    }
}