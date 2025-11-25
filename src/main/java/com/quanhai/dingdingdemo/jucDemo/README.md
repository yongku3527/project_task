# JUC多线程编程演示项目

## 项目概述

本项目是一个全面的Java并发编程（JUC - java.util.concurrent）学习演示程序，通过实际的代码示例和交互式演示，帮助开发者深入理解Java多线程编程的核心概念和最佳实践。

## 项目特色

- **交互式演示**: 友好的命令行界面，可选择运行不同模块
- **全面覆盖**: 涵盖synchronized、Lock、线程池、死锁、竞态条件等核心概念
- **实用代码**: 所有代码示例都可直接运行和修改
- **详细注释**: 每段代码都包含详细的中文注释和原理解释
- **最佳实践**: 提供实际开发中的最佳实践建议
- **性能对比**: 包含不同同步机制的性能测试和对比

## 系统要求

- **Java版本**: Java 8 或更高版本
- **内存要求**: 建议至少 512MB 可用内存
- **操作系统**: 支持所有主流操作系统 (Windows, macOS, Linux)
- **依赖包**: 仅使用 JDK 自带的 java.util.concurrent 包，无外部依赖

## 项目结构

```
src/main/java/com/quanhai/dingdingdemo/jucDemo/
├── JucDemoMain.java           # 主程序入口和交互界面
├── SynchronizedDemo.java      # synchronized关键字演示
├── LockDemo.java             # Lock接口使用演示
├── ThreadPoolDemo.java       # 线程池使用演示
├── DeadlockDemo.java         # 死锁问题演示与解决
├── RaceConditionDemo.java    # 竞态条件演示与解决
└── README.md                 # 项目说明文档
```

## 功能模块详细说明

### 1. SynchronizedDemo.java - 线程同步机制

**主要功能**:
- 实例方法同步演示
- 静态方法同步演示  
- 同步代码块演示
- 竞态条件问题演示

**核心类**:
- `BankAccount`: 银行账户类，演示线程安全的资金操作
- `UnsafeBankAccount`: 非线程安全的账户类，作为对比
- `SynchronizedBank`: 线程安全的账户类，使用synchronized

**运行效果**:
```
========== synchronized关键字演示 ==========
--- 测试线程安全银行账户 ---
[Thread-0] 开始转账操作...
[Thread-1] 开始转账操作...
[Thread-0] 存款: $500, 账户余额: $2000
[Thread-1] 存款: $500, 账户余额: $2500
[Thread-0] 取款: $300, 账户余额: $2200
[Thread-1] 取款: $300, 账户余额: $1900
最终账户余额: $1900
余额正确性: 100%

--- 测试非线程安全银行账户 ---
[Thread-0] 开始转账操作...
[Thread-1] 开始转账操作...
[Thread-0] 存款: $500, 账户余额: $2000
[Thread-1] 存款: $500, 账户余额: $2500
[Thread-0] 取款: $300, 账户余额: $2200
[Thread-1] 取款: $300, 账户余额: $1900
最终账户余额: $1850  (存在数据丢失)
余额正确性: 错误
```

### 2. LockDemo.java - Lock接口使用

**主要功能**:
- ReentrantLock基本使用
- ReentrantReadWriteLock读写锁
- Condition条件等待机制
- Lock与synchronized的区别对比

**核心类**:
- `ReentrantLockDemo`: 可重入锁演示
- `ReadWriteLockDemo`: 读写锁演示
- `ConditionDemo`: 条件等待演示
- `LockComparison`: 锁对比分析

**运行效果**:
```
========== Lock接口演示 ==========
--- ReentrantLock演示 ---
[Thread-0] 尝试获取锁...
[Thread-0] 获取到锁，执行任务...
[Thread-1] 尝试获取锁...
[Thread-1] 等待锁释放...
[Thread-0] 释放锁
[Thread-1] 获取到锁，执行任务...
--- ReentrantReadWriteLock演示 ---
读线程 ReadThread-0 开始读取数据...
读线程 ReadThread-1 开始读取数据...
读线程 ReadThread-2 开始读取数据...
数据读取完成: DataReader{value='Hello World'}
写线程 WriteThread 开始写入数据...
数据写入完成: DataReader{value='Updated Data'}
```

### 3. ThreadPoolDemo.java - 线程池管理

**主要功能**:
- FixedThreadPool固定大小线程池
- CachedThreadPool缓存线程池
- ScheduledThreadPool调度线程池
- 自定义线程工厂
- 自定义拒绝策略
- 线程池监控统计

**核心类**:
- `ThreadPoolManager`: 线程池管理器
- `CustomThreadFactory`: 自定义线程工厂
- `CustomRejectedHandler`: 自定义拒绝策略
- `MonitoredExecutor`: 带监控的执行器

**运行效果**:
```
========== 线程池演示 ==========
--- 测试固定大小线程池 ---
线程池基本信息:
核心线程数: 4
最大线程数: 4
队列容量: 100
活跃线程数: 2
总任务数: 10
完成任务数: 10
队列当前大小: 0

执行时间统计:
任务执行时间范围: 100-500ms
平均执行时间: 250ms
执行效率: 高效

--- 测试缓存线程池 ---
线程池基本信息:
核心线程数: 0
最大线程数: 2147483647
队列容量: 0
活跃线程数: 5
总任务数: 5
完成任务数: 5
```

### 4. DeadlockDemo.java - 死锁演示与解决

**主要功能**:
- 经典死锁演示
- 银行转账死锁演示
- 死锁检测机制
- 死锁预防策略
- 死锁恢复机制

**核心类**:
- `ClassicDeadlockDemo`: 经典死锁演示
- `BankTransferDeadlock`: 银行转账死锁
- `DeadlockDetector`: 死锁检测器
- `DeadlockPrevention`: 死锁预防

**运行效果**:
```
========== 死锁演示 ==========
--- 经典死锁演示 ---
[Thread-0] 尝试获取资源A...
[Thread-1] 尝试获取资源B...
[Thread-0] 获取资源A成功，尝试获取资源B...
[Thread-1] 获取资源B成功，尝试获取资源A...
[⚠️ 警告] 检测到死锁！
检测到的死锁:
  Thread-0 等待 Thread-1 持有的资源B
  Thread-1 等待 Thread-0 持有的资源A

--- 死锁预防演示 ---
[Thread-0] 按照统一顺序获取锁: resourceA -> resourceB
[Thread-1] 按照统一顺序获取锁: resourceA -> resourceB
[Thread-0] 获取resourceA成功
[Thread-1] 等待resourceA释放...
[Thread-0] 获取resourceB成功
[Thread-1] 获取resourceA成功
[Thread-0] 释放所有锁
[Thread-1] 获取resourceB成功
✅ 没有发生死锁！
```

### 5. RaceConditionDemo.java - 竞态条件演示

**主要功能**:
- 计数器竞态条件
- 银行账户竞态条件
- 商品库存竞态条件
- 竞态条件检测
- 性能对比分析
- 最佳实践指南

**核心类**:
- `Counter`: 计数器类，对比不同同步方式
- `BankAccount`: 银行账户类
- `ProductInventory`: 商品库存类
- `RaceConditionDetector`: 竞态条件检测器

**运行效果**:
```
========== 竞态条件演示 ==========
测试配置: 10 个线程，每个线程执行 1000 次递增操作
期望结果: 10000

--- 测试线程不安全计数器 ---
不安全计数器结果: 9847
数据丢失: 153
⚠️ 检测到竞态条件！

--- 测试AtomicInteger计数器 ---
AtomicInteger计数器结果: 10000
数据准确性: 100%
✅ 完美同步

--- 性能对比分析 ---
AtomicInteger时间: 245ms
synchronized时间: 412ms
AtomicInteger相对性能: 59.47%
synchronized相对性能: 基准 (100%)
✅ 建议在高并发场景使用Atomic类
```

## 运行指南

### 1. 编译项目

```bash
# 在项目根目录执行编译命令
javac -d build src/main/java/com/quanhai/dingdingdemo/jucDemo/*.java

# 或者使用Maven编译（如果项目包含pom.xml）
mvn compile
```

### 2. 运行程序

```bash
# 运行主程序
java -cp build com.quanhai.dingdingdemo.jucDemo.JucDemoMain
```

### 3. 交互操作

程序启动后会显示主菜单：

```
==================================================
           JUC多线程编程演示程序                 
==================================================
请选择要运行的演示：
==================================================
1. 全部演示 (推荐)
2. synchronized关键字演示
3. Lock接口演示
4. 线程池演示
5. 死锁问题演示
6. 竞态条件演示
7. 查看项目说明
0. 退出程序
==================================================
请输入选择 (0-7): 
```

**操作说明**:
- 输入 `1`: 运行所有演示模块（推荐新手）
- 输入 `2-6`: 运行特定演示模块
- 输入 `7`: 查看项目详细信息
- 输入 `0`: 退出程序
- 按 `Enter` 键继续下一个演示

## 学习建议

### 新手入门路径

1. **基础概念学习**
   - 先运行"全部演示"了解整体结构
   - 阅读每个演示的输出结果
   - 理解基本的并发编程概念

2. **深入理解机制**
   - 单独运行每个模块
   - 修改代码参数观察变化
   - 对比不同解决方案的效果

3. **实践应用**
   - 将示例代码应用到实际项目
   - 参考最佳实践指南
   - 进行性能优化练习

### 进阶学习建议

1. **代码审查**
   - 仔细阅读每个类的实现
   - 分析同步机制的选择原因
   - 理解性能优化的思路

2. **扩展实验**
   - 修改线程数量和操作次数
   - 添加新的测试场景
   - 实现自定义的并发工具

3. **深入研究**
   - 阅读JDK源码中的并发实现
   - 学习并发编程的理论知识
   - 关注Java并发包的新特性

## 最佳实践总结

### 同步机制选择

| 场景 | 推荐方案 | 原因 |
|------|----------|------|
| 简单计数器 | AtomicInteger | 高性能，无锁实现 |
| 复杂业务逻辑 | synchronized | 简单可靠，JVM优化 |
| 读写分离 | ReentrantReadWriteLock | 提升读性能 |
| 限时等待 | tryLock(timeout) | 避免无限等待 |
| 条件等待 | Condition | 精确的等待/通知 |

### 线程池配置

| 场景 | 核心线程数 | 最大线程数 | 队列类型 |
|------|------------|------------|----------|
| CPU密集型 | CPU核心数 | CPU核心数 | LinkedBlockingQueue |
| IO密集型 | 2×CPU核心数 | 4×CPU核心数 | LinkedBlockingQueue |
| 混合型 | CPU核心数+1 | 2×CPU核心数 | LinkedBlockingQueue |
| 短任务 | 0 | 很多 | SynchronousQueue |

### 并发问题预防

1. **死锁预防**
   - 统一锁的获取顺序
   - 使用tryLock避免无限等待
   - 设置锁超时时间
   - 避免嵌套锁

2. **竞态条件解决**
   - 使用原子类（AtomicXxx）
   - 适当使用synchronized
   - 设计无锁数据结构
   - 最小化共享状态

3. **性能优化**
   - 减少锁的持有时间
   - 使用并发集合类
   - 选择合适的数据结构
   - 进行性能测试

## 常见问题解答

### Q1: 运行时出现内存不足怎么办？
A: 增加JVM堆内存，使用命令：
```bash
java -Xmx1024m -cp build com.quanhai.dingdingdemo.jucDemo.JucDemoMain
```

### Q2: 程序运行时间过长怎么办？
A: 可以只运行特定模块（输入2-6），或者修改各演示类中的线程数和操作次数。

### Q3: 如何修改演示参数？
A: 直接编辑各个Java文件中的常量，如线程数、操作次数等。

### Q4: 死锁演示为什么检测不到死锁？
A: 死锁的发生具有一定的随机性，可以通过增加线程数或操作次数来提高死锁发生概率。

### Q5: Atomic类和synchronized哪个性能更好？
A: Atomic类在简单操作上性能更好，但synchronized在复杂业务逻辑上更灵活。具体使用需要根据场景选择。

## 扩展阅读

1. **推荐书籍**
   - 《Java并发编程实战》
   - 《Java并发编程的艺术》
   - 《深入理解Java虚拟机》

2. **在线资源**
   - Oracle官方Java并发教程
   - JDK源码中的并发实现
   - 开源并发库（如Disruptor）

3. **实践项目**
   - 实现一个简单的线程池
   - 开发无锁数据结构
   - 构建高并发系统

## 版本历史

- **v1.0** (当前版本)
  - 实现5个核心演示模块
  - 添加交互式菜单界面
  - 完善文档和注释
  - 性能测试和对比

## 许可证

本项目仅用于学习和演示目的，请勿用于商业用途。

## 联系方式

如有疑问或建议，请通过以下方式联系：
- 创建Issue进行讨论
- 提交Pull Request贡献代码
- 完善项目文档

---

**祝您学习愉快！并发编程世界欢迎您！** 🚀