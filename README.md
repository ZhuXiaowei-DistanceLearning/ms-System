# 基于SpirngCloud的秒杀系统

@Author:zxw

@School:吉首大学

# 1.单一应用环境下

1. 环境：
   1. 库存10000
   2. 并发请求5000

## 1.在无锁的情况下

1. 最终结果28秒，高并发不可用

## 2.在synchronized条件下

1. 通过在方法名加上synchronized关键字，最终结果33秒，结果超出1，会有细微精度的丢失,当购买数量刚好相等时，精度无丢失
2. 通过使用synchronized语句块对this加锁，最终结果21秒，高并发不可用

## 3.使用乐观锁CAS算法

1. 使用无限循环判断在100ms内一直重试直到更改成功，最终结果39秒,结果准确
2. 使用循环次数重试机制，次数为3，最终结果33秒，结果准确

# 2. 分布式应用环境下

1. 使用Redis+RabbitMQ实现异步抢购，减少后台压力

# 3.项目遇到的问题

## 3.1 Zipkin

1. ```java
   java.lang.IllegalArgumentException: Prometheus requires that all meters with the same name have the same set of tag keys. There is already an existing meter containing tag keys [exception, method, status, uri]. The meter you are attempting to register has keys [method, status, uri].
   ```

   ```yml
   server:
     port: 9411
   spring:
     application:
       name: zipkin-server
   
   management:
     metrics:
       web:
         server:
           auto-time-requests: false
     endpoints:
       web:
         exposure:
           include: "*"
   ```

   