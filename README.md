# 高并发编程

# 1.概念

1. 同步和异步
   1. 同步：调用者必须等到方法调用返回后，才能继续后继行为
   2. 异步：方法调用立即返回
2. 并发和并行
3. 临界区
   1. 公共资源或者共享数据，每一次只能有一个线程使用它
4. 阻塞和非阻塞
5. 死锁、饥饿和活锁
   1. 死锁
   2. 饥饿：无法获得所需要的资源，导致一直无法执行
   3. 活锁：主动释放资源给他人用。两个线程之间资源来回跳动

## 1.1 并发级别

1. 阻塞：在其他线程释放资源前，当前线程无法继续执行
2. 无饥饿:线程之间的优先级，资源分配不公平
3. 无锁:无穷循环中，线程会不断尝试修改共享变量，如果没有冲突，修改成功。否则继续修改CAS算法
4. 无等待:要求所有线程有限步内必须完成，RCU（Read Copy Update）对数据读不加以控制，但在写数据的时候，先取得原始数据的副本，接着修改副本数据，修改完成后，在合适的实际写回数据
5. 无障碍:乐观锁

## 1.2 两个并行定律

1. Amdahl定律
2. Gustafson

# 2. Java并行程序

1. 原子性：一个操作不可中断，即使多个线程一起执行的时候，一个操作一旦开始，就不会被其他线程干扰

2. 可见性：当一个线程修改了某一个共享变量的值时，其他线程是否能够立即指导修改

3. 有序性：程序执行时，指令重排

4. ```java
   public enum State{
   	NEW, // 线程新建，等到start方法调用时线程才开始执行
       RUNNABLE, // 所需资源准备完毕
       BLOCKED, // synchronized同步块，进入blocked阻塞状态
       WAITING, // 等待请求的锁，等待时间无限制,等到一些特殊时间，比如wait(),notify(),join()
       TIMED_WAITING, // 等待时间有限制
       TERMINATED // 线程结束
   }
   ```

## 2.1 线程基本操作

1. 新建：不要用run()方法开启新线程，它只会在当前线程中串行执行run()方法中的代码,默认的Tread.run()方法直接调用内部的Runnable接口

2. 终止:stop(),直接终止线程，并立即释放这个线程所持有的锁

3. 中断:

   ```java
   interrupt() // 中断线程
   isInterrupted() // 判断线程是否被中断
   Thread.interrupted() // 判断是否被中断，并清除当前状态
   ```

4. 等待和通知:

   ```java
   public final void wait() throws InterruptedException // 进入对象的等待队列
   public final native void notify() // 随机唤醒
   Object.wait() // 需要包含在synchronized语句中，在wait()方法执行前首先获得目标对象的一个监视器，在wait()执行后，会释放这个监视器
   Object.notify() // 首先获得监视器
   ```

5. 挂起和继续执行

   ```java
   suspend() // 不释放锁资源,从线程状态上看还是Runnable
   resume() // 唤起线程
   ```

6. 等待线程结束和谦让

   ```java
   public void final join() // 无限等待，一只阻塞当前线程
   public final synchronized void join(long mills) // 有限时间等待
   public static native void yield() // 当前线程让出CPU。让出后还会进行CPU资源争夺
   ```

7. volatile与java内存模型

8. 线程组

   ```java
   ThreadGroup tg = new ThreadGroup("print");
   Thread t1 = new Thread(tg,"","");
   ```

9. 守护线程

   ```java
   Thread t1 = new Thread();
   t.setDeamon(true); // 必须在start()之前设置，如果用户线程全部结束，守护线程要守护的对象已经不存在了，那么整个应用程序就应该结束。
   ```

10. 线程优先级

    ```java
    public final static int MIN_PRIORITY = 1;
    public final static int NORM_PRIORITY = 5;
    public final static int MAX_PRIORITY = 10;
    Thread t1 = new Thread();
    t1.setPriority(Thread.MIN_PRIORITY); // 高优先级在大部分情况下，都会首先完成任务
    ```

11. synchronized
    1. 对于普通同步方法，锁是当前实例对象。
    2. 对于静态同步方法，锁是当前类的Class对象。
    3. 对于同步方法块，锁是Synchonized括号里配置的对象。
       当

12. 错误的额加锁

# 3. JDK并发包

## 3.1 同步控制

### 3.1.1 重入锁(ReentrantLock)

1. 中断响应

   ```
   lock.lockInterruptibly()
   ```

2. 锁申请等待限时

   ```java
   // 如果不指定参数，则不会等待直接返回false
   try{
   if(lock.tryLock(5, TimeUnit.SECONDS)){
       Thread.sleep(6000)；
   }
   }finally{
       lock.unlock();
   }
   ```

1. 公平锁

   ```java
   public ReentrantLock(boolean fair) // 公平锁,需要维护一个有序队列，实现成本比较高，性能比较低
   ```

2. API

   1. lock()：获得锁，如果已经被占用，则等待
   2. lockInterruptibly()：获得锁，但优先响应中断
   3. tryLock()：尝试获得锁，如果成功，则返回true，失败返回false。
   4. tryLock(long time,TimeUnit unit)：给定时间内尝试获得锁
   5. unlock()：释放锁

3. 三要素

   1. 原子状态：原子状态使用CAS操作
   2. 等待队列：所有没有请求到锁的线程，会进入等待队列进行等待
   3. 阻塞原于park和unpark()：用来挂起和恢复线程。没有得到锁的线程将会被挂起

### 3.1.2 重入锁的好搭档：Condition

1. 与重入锁相关联，通过lock接口的Condition new Condition()方法可以生成一个与当前重入锁绑定的Condition实例,用重入锁的对象来获得此对象
2. ![1567393746883](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567393746883.png)

### 3.1.3  信号量(Semaphore)

1. ```java
   public Semaphore(int permits) // 指定同时能申请多少个许可
   public Semaphore(int permits, boolean fair) // 第二个参数可以指定是否公平
   ```

2. ```java
   public void acquire() // 获取准入许可，如果无法获得，则线程等待
   public void acquireUninterruptibly() // 不响应中断
   public boolean tryAcquire() // 尝试获取许可
   public boolean tryAcquire(long timeout, TimeUnit unit)
   public void release() // 释放许可
   ```

3. 信号量是对锁的扩展，可以指定多个线程，同时访问某一个资源

### 3.1.4 ReadWriteLock 读写锁

1. ![1567394933710](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567394933710.png)

2. ```java
   ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock()
   Lock readlock = readWriteLock.readLock();
   Lock writelock = readWriteLock.writeLock();
   ```

### 3.1.5 倒计数器CountDownLatch

1. 控制线程等待，让某一个线程等待直到倒计数结束，再开始执行

   ```java
   public CountDownLatch(int count) // 需要count个线程完成任务后等待在CountDownLatch上的线程才能继续执行
   CountDownLatch end = new CountDownLatch(10);
   end.countDown(); // 通知CountDownLatch一个线程已经完成任务,倒计数减1
   ```

### 3.1.6 循环栅栏CyclicBarrier

1. 阻止线程继续执行，要求线程在栅栏外等待，这个计数器可以反复使用,当碰到await()方法每个线程都会等待，直到所有线程执行完毕。

2. ```java
   public CyclicBarrier(int parties, Runnable barrierAction)
   ```

### 3.1.7 线程阻塞工具类LockSupport

1. 线程阻塞工具，可以让线程内任意位置让线程阻塞，弥补了resume()方法导致线程无法执行的情况，与wait()方法相比不需要先获得某个对象的锁

### 3.1.8 Guava和RateLimiter限流

1. ```java
   package com.guava;
   
   import com.google.common.util.concurrent.RateLimiter;
   
   /**
    * @author zxw
    * @date 2019/9/3 9:22
    */
   public class RateLimiterDemo {
       static RateLimiter limiter = RateLimiter.create(2);
       static int i = 0;
       public static class Task implements Runnable{
   
           @Override
           public void run() {
               System.out.println(System.currentTimeMillis());
               System.out.println(i);
               i++;
           }
       }
   
       public static void main(String[] args) {
           for (int i = 0; i < 50; i++) {
               // 限流
   //            limiter.acquire();
               // 令牌
               if(!limiter.tryAcquire()){
                   continue;
               }
               Thread thread = new Thread(new Thread(new Task()));
           }
       }
   }
   ```

   


## 3.2 线程池

1. 如果当前运行的线程少于corePoolSize，则创建新线程来执行任务（注意，执行这一步骤需要获取全局锁）。

2. 如果运行的线程等于或多于corePoolSize，则将任务加入BlockingQueue。

3. 如果无法将任务加入BlockingQueue(队列已满)，则创建新的线程来处理任务(注意，执行这一步骤需要获取全局锁)。

4. 如果创建新线程将使当前运行的线程超出maximumPoolSize，任务将被拒绝，并调xRejectedExecutionHandler.rejectedExecution()方法。

5. ThreadPoolExecutor采取上述步骤的总体设计思路，是为了在执行execute()方法时，尽可能地避免获取全局锁(那将会是一个严重的可伸缩瓶颈)。在ThreadPoolExecutor完成预热之后(当前运行的线程数大于等于corePoolSize)

6. ![1567475072047](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567475072047.png)

7. ![1567475097776](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567475097776.png)

8. ```java
   public ThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue) {
           this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), defaultHandler);
       }
   
    public ThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,  
                             	BlockingQueue<Runnable> workQueue,// 任务队列
   ThreadFactory threadFactory, // 线程工厂
   RejectedExecutionHandler handler)// 拒绝策略
   ```

### 3.2.1 任务队列

1. 直接提交队列
   1. SynchronousQueue对象提供，没有容量，每个插入操作都要等待一个相应的删除操作，反之，每一个删除操作都要等待对应的插入操作
   2. 提交的任务不会被真实地保存，而总是将新任务提交给线程执行，如果没有空闲的进程，则尝试创建新的进程，如果进程数量达到最大值，则执行拒绝策略
2. 有界的任务队列
   1. ArrayBlockingQueue,如果大于corePoolSize,则将新任务加入等待队列。若等待队列已满，无法加入，则在总线程数不大于maximumPoolSize的前提下，创建新的线程执行任务，若大于maximumPoolSize，则执行拒绝策略。除非系统非常繁忙，否则要确保线程数维持在corePoolSize
3. 无界的任务队列
   1. LinkedBlockingQueue，除非系统资源耗尽，否则无界的任务队列不存在任务入队失败情况。当系统的线程数达到corePoolSize时，线程池会生成新的线程执行任务，但当系统的线程数达到corePoolSize后，就不会继续增加了，后序线程放入等待队列。
4. 优先任务队列
   1. 可以控制任务的执行先后顺序，是一个特殊的无界队列

### 3.2.2 拒绝策略

1. ![1567477666355](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567477666355.png)
2. ![1567477680348](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567477680348.png)
3. 可以通过扩展RejectedExecutionHandler接口实现拒绝策略

### 3.2.3 自定义线程创建ThreadFactory

1. ```java
   Thread newThread(Runnable r);
   new ThreadPoolExecutor(...,...,...,...,...,new ThreadFactory(){
       public ThreadFactory(){
           
       }
   })
   ```

2. 扩展线程池

   ```java
   new ThreadPoolExecutor(5,5,0L,TimeUnit.SECONDS,new LinkedBlockingDeque<>()){
               @Override
               protected void beforeExecute(Thread t, Runnable r) {
                   super.beforeExecute(t, r);
               }
   
               @Override
               protected void afterExecute(Runnable r, Throwable t) {
                   super.afterExecute(r, t);
               }
   
               @Override
               protected void terminated() {
                   super.terminated();
               }
           };
   ```

3. 优化线程池数量

   1. ![1567478156316](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567478156316.png)

   2. 自定义扩展线程池

      ```java
      package com.pool;
      
      import java.util.concurrent.*;
      
      /**
       * @author zxw
       * @date 2019/9/3 10:37
       */
      public class TraceThreadPoolExecutor extends ThreadPoolExecutor {
          public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
              super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
          }
      
          @Override
          public void execute(Runnable command) {
              super.execute(command);
          }
      
          @Override
          public Future<?> submit(Runnable task) {
              return super.submit(task);
          }
      
          public Exception clientTrace() {
              return new Exception("Client stack trace");
          }
      
          private Runnable wrap(final Runnable task, final Exception clientStack, String clientThreadName) {
              return new Runnable() {
                  @Override
                  public void run() {
                      try {
                          task.run();
                      } catch (Exception e) {
                          clientStack.printStackTrace();
                          e.printStackTrace();
                      }
                  }
              };
          }
      }
      ```

### 3.2.4 Fork/Join框架

1. ![1567478706596](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567478706596.png)

2. ```java
   package com.pool;
   
   import java.util.concurrent.RecursiveTask;
   
   /**
    * @author zxw
    * @date 2019/9/3 10:45
    */
   public class CountTask extends RecursiveTask<Long> {
       private static final int THRESHOLD = 10000;
       private long start;
       private long end;
   
       public CountTask(long start, long end) {
           this.start = start;
           this.end = end;
       }
   
       @Override
       protected Long compute() {
           long sum = 0;
           boolean canCompute = (end - start) < THRESHOLD;
           if (canCompute) {
               for (long i = start; i <= end; i++) {
                   sum += i;
               }
           }
           return null;
       }
   }
   ```

### 3.2.5 Guava线程池

1. DirectExecutor线程池

   ```java
   public static void main(String[] args) {
           Executor executor = MoreExecutors.directExecutor();
           executor.execute(() -> System.out.println("I am running in " + Thread.currentThread().getName()));
       }
   ```

2. Deamon线程池

   1. ```
      MoreExecutors.getExitingExecutorService(executor);
      ```

3. 对Future模式扩展

## 3.3 JDK的并发容器

1. ConcurrentHashMap:线程安全的HashMap
2. CopyOnWriteArrayLsit:读写List，远远优于Vector
3. ConcurrentLinkedQueue:高效的并发队列，使用链表实现。线程安全的LinkedList
4. BlockingQueue:通过链表、数组等方式实现了这个接口。表示阻塞队列，非常适合作为数据共享的通道
   1. ![1567480826627](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567480826627.png)
5. ConcurrentSkipListMap:跳表的实现。这是一个Map，使用跳表的数据结构进行快速查找。

### 3.3.1 CopyOnWriteArrayLsit和ConcurrentLinkedQueue

1. 都是高并发队列
2. ConcurrentLinkedQueue通过CAS操作和锁分离来提高系统性能
3. CopyOnWriteArrayLsit通过写复制来提升并发能力

### 3.3.2 BlockingQueue

1. ArrayBlockingQueue：一个由数组结构组成的有界阻塞队列。
2. LinkedBlockingQueue：一个由链表结构组成的有界阻塞队列。
3. PriorityBlockingQueue：一个支持优先级排序的无界阻塞队列。
4. DelayQueue：一个使用优先级队列实现的无界阻塞队列
5. SynchronousQueue：一个不存储元素的阻塞队列。
6. LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
7. LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。

## 3.4 JMH性能测试

1. 模式
   1. Throughput:整体吞吐量，表示1秒内可以执行多少次调用
   2. AverageTime:调用的平均时间，指每一次调用所需要的时间
   3. SampleTime:随机取样，最后输出取样结果的分布
   4. SingleShotTime:只运行一次，用于测试冷启动时的性能
2. 迭代
   1. 一次迭代表示1秒，在这一秒内会不间断调用被测方法，并采样计算吞吐量、平均时间等
3. 预热(Wramup)
   1. 同一个方法在JIT编译前后的时间将会不同
4. 状态(State)
   1. 线程范文，也就是一个对象只会被一个线程访问，在多线程池测试时，会为每一个线程生成一个对象。另一种是基准测试范围，即多个线程共享一个实例。
5. 配置类(Options/OptionsBuilder)
   1. 测试开始前，首先要对测试进行配置。通常需要指定一些参数。比如测试类(include)、使用的进程个数(fork)、预热迭代次数(warmupIterations)

## 3.5 锁的优化

1.  性能提升建议
   1. 减少锁的持有时间:只在必要时进行同步，例如对整个方法中的某条语句加锁，这样能减少锁的持有时间
2. 减小锁粒度
   1. 减小锁定对象的范围，从而降低锁冲突的可能性，进而提高系统的并发能力
3. 用读写分离锁来替换独占锁
4. 锁分离
   1. 使用两把锁对不同的方法进行加锁
5. 锁粗化
   1. 根据运行时的真实情况对各个资源点进行权衡折中的过程。锁粗化的思想和减少锁持有时间是相反的。

### 3.5.1 Java虚拟机对锁的优化

1. 锁偏向
   1. 如果一个线程获得了锁，那么锁就进入了偏向模式。当这个线程再次请求锁的时候，无需做任何同步操作。使用Java虚拟机参数 -XX:+UseBiasedLocking 开启偏向锁
2. 轻量级锁
   1. 如果偏向锁失败，那么虚拟机不会立即挂起线程，它还会使用轻量级锁优化，它只是简单地将对象头部作为指针指向持有锁的线程堆栈的内部，来判断一个线程是否持有对象锁。如果线程获得轻量级锁成功，则可以顺利进入临界区。如果轻量级锁加锁失败，则表示其他线程抢先争夺到了锁，那么当前线程的锁清秋就会膨胀为重量级锁。
3. 自旋锁
   1. 当前线程无法获得锁，而且什么时候可以获得锁是一个未知数，也许在几个CPU时钟周期后就可以获得到锁。虚拟机会让当前线程做几个空循环，在经过若干次循环后，如果可以得到锁，那么久顺利进入临界区。如果还不能获得锁，才会真的将线程在操作系统层面挂起。
4. 锁消除
   1. 对运行上下文扫描，去除不可能存在共享资源竞争的锁。

## 3.6 ThreadLocal

1. 如果共享对象对于竞争的处理容易引起性能损失，典型案例：随机数

## 3.7 无锁

1.  CAS(V,E,B)
   1. V,E,N：V表示要更新的变量，E表示预期值，N表示新值。仅当V值等于E值时，才会将V的值设为N，如果V值和E值不同，说明已经有其他线程做了更新，则当前线程什么都不做。
2. 好处
   1. 比有锁拥有可好的性能
   2. 避免死锁

### 3.7.1 AtomicInteger

1. ![1567564684691](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567564684691.png)
2. ![1567564766971](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567564766971.png)

### 3.7.2 AtomicReference

1. 对对象进行原子操作

### 3.7.2 AtmoicStampedReference

1. ![1567565485213](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567565485213.png)

### 3.7.3 AtomicIntegerArray

1. AtomicIntegerArray

2. AtomicLongArray

3. AtomicReferenceArray

4. ![1567565667934](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567565667934.png)

5. ```java
   static AtomicIntegerArray arr = new AtomicIntegerArray(10);
   ```

### 3.7.4 AtomicIntegerFieldUpdater

1. 在不改动原有代码的基础上，让普通的变量也享受CAS操作带来的线程安全性

2. AtomicIntegerFieldUpdater

   ```java
   public final static AtomicIntegerFieldUpdater<Candidate> scoreUpdater = AtomicIntegerFieldUpdater.newUpdater(Candidate.class, "score");
   ```

3. AtomicLongFieldUpdater

4. AtomicReferenceFieldUpdater

### 3.7.5 无锁的Vector

1. 详情200页

### 3.7.6 SynchronousQueue

# 4. 并行模式与算法

## 4.1 单例模式

1.  任何对Singleton方法或字段的应用，都会导致类初始化,并创建instance实例![1567644960498](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567644960498.png)
2. 延迟加载
   1. ![1567645049204](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567645049204.png)
   2. ![1567645056586](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567645056586.png)
3. ![1567645185050](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567645185050.png)

## 4.2 不变模式

1. 一个对象一旦被创建，它的内部状态将永远不会发生改变。没有一个线程可以修改其内部状态和数据，同时其内部状态也绝不会自行发生改变。
2. 条件
   1. 当对象创建后，其内部状态和数据不再发生任何变化
   2. 对象需要被共享，被多线程频繁访问
3. 注意点
   1. 去除setter方法以及所有修改自身属性的方法
   2. 将所有属性设置为私有，并用final标记，确保其不可修改
   3. 确保没有子类可以重载修改它的行为
   4. 有一个可以创建完成对象的构造函数
   5. ![1567645832914](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567645832914.png)

## 4.3 生产者-消费者模式

## 4.4 高性能的生产者-消费者模式-无锁实现

## 4.5 Future模式

1. ![1567646681938](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567646681938.png)

2. 异步调用

3. Callable接口

   ```java
    @Override
       public String call() throws Exception {
           StringBuffer sb = new StringBuffer();
           for (int i = 0; i < 10; i++) {
               sb.append(result);
               try {
                   Thread.sleep(100);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           return sb.toString();
       }
   ```

4. ```java
    public static void main(String[] args) throws ExecutionException, InterruptedException {
           FutureTask<String> future = new FutureTask<>(new RealDatajdk("a"));
           ExecutorService pool = Executors.newFixedThreadPool(1);
           pool.submit(future);
           System.out.println("请求完毕");
           try {
               Thread.sleep(2000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           System.out.println("数据=" + future.get());
       }
   ```

   ![1567648012028](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567648012028.png)

5. Guava对Future模式支持

   1. ```java
      public static void main(String[] args) throws InterruptedException {
              ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
              ListenableFuture<String> task = service.submit(new RealDatajdk("x"));
              task.addListener(()->{
                  System.out.println("异步处理成功");
                  try {
                      System.out.println(task.get());
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  } catch (ExecutionException e) {
                      e.printStackTrace();
                  }
              },MoreExecutors.directExecutor());
              System.out.println("main task done ......");
              Thread.sleep(3000);
          }
      ```

## 4.6 并行流水线

1. 通过队列将操作拆分，一个线程处理一部分

## 4.7 并行搜索

## 4.8 并行排序

1. 奇偶交换排序

## 4.9 NIO

# 5.JAVA8与并发

## 5.1 函数式编程

1. FunctionalInterface注释

2. 接口默认方法

3. lambda表达式

4. 方法引用

5. Arrays

   1. ![1567653121631](C:\Users\zxw\Desktop\个人项目笔记\高并发编程.assets\1567653121631.png)

6. Stream

   1. 创建Stream
   2. 中间操作
   3. 终止操作

7. ```java
   IntConsumer intConsumer = System.out::println;
   IntConsumer errComsumer = System.out::println;
      Arrays.stream(arr).forEach(intConsumer.andThen(errComsumer));
   ```

## 5.2 并行流与并行排序

1. 使用并行流过滤数据
2. 从集合得到并行流
3. 并行排序

## 5.3 增强的Future

1. CompletableFuture实现了Futrue和CompletionStage

2. ```java
    public static Integer cacl(Integer para) {
           try {
               Thread.sleep(1000);
               System.out.println(para * para);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           return para * para;
       }
      
       public static void main(String[] args) throws  CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> cacl(50))
           // 异常处理
           .exceptionally(ex->{
               System.out.println(ex.toString());
               return 0;
           })
           // 组合多个CompletableFuture
           .thenCompose((i)->CompletableFuture.supplyAsync(()->cacl(i)))
           // 流失调用
                   .thenApply((i)->Integer.toString(i))
                   .thenAccept(System.out::println);
           System.out.println("开始");
           future.get();
           System.out.println("结束");
       }
   ```

3. ![1567732899483](高并发编程.assets/1567732899483.png)

## 5.4 StampedLock 读写锁改进

1. 使用CAS算法

## 5.5 原子类增强

### 5.5.1 LongAddr

### 5.5.2 ConcurrentHashMap

1. foreach操作
2. reduce操作
3. search操作
4. mappingCount():返回Map中的条目总数，返回long
5. newKeySize()：返回一个线程安全的Set