# 消息队列4-整合SpringBoot

## 1. 基本思路

- 搭建环境
- 基础设定：交换机名称、队列名称、绑定关系
- 发送消息：使用RabbitTemplate
- 接收消息：使用@RabbitListener注解

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240601103618777.png)

## 2. 消费者工程

### 2.1 配置POM.xml

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.5</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### 2.2 YAML

增加日志打印的配置：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: 123456
    virtual-host: /
logging:
  level:
    com.nateshao.consumer.mp.listener.MyMessageListener: info
```

### 2.3 主启动类

仿照生产者工程的主启动类，改一下类名即可

```java
package com.nateshao.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/30 16:00
 * @Version 1.0
 */
@SpringBootApplication
public class RabbitMQConsumerMainType {
    public static void main(String[] args) {
        SpringApplication.run(RabbitMQConsumerMainType.class, args);
    }
}
```

### 2.4 监听器（重要）

```java
package com.nateshao.consumer.mp.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class MyMessageListener {

    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order"; // 字符串数组
    public static final String QUEUE_NAME = "queue.order";

//    写法一：监听 + 在 RabbitMQ 服务器上创建交换机、队列
    @RabbitListener(bindings = @QueueBinding(
            // value = @Queue：给value属性赋值,QUEUE_NAME：队列名称,durable ="true" ：持久化
            value = @Queue(value = QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = EXCHANGE_DIRECT), // exchange指定交换机exchange；@Exchange给exchange属性赋值；EXCHANGE_DIRECT交换机名称
            key = {ROUTING_KEY} // 指定路由键信息
        )
    )

//    写法二：监听
//    @RabbitListener(queues = {QUEUE_NAME})
    public void processMessage(String dataString, Message message, Channel channel) {
        log.info("消费端接收到了消息：" + dataString);
    }
}
```



## 3.@RabbitListener注解属性对比

### 3.1 bindings属性

- 表面作用：
  - 指定交换机和队列之间的绑定关系
  - 指定当前方法要监听的队列
- 隐藏效果：如果RabbitMQ服务器上没有这里指定的交换机和队列，那么框架底层的代码会创建它们

### 3.2 queues属性

```java
@RabbitListener(queues = {QUEUE_ATGUIGU})
```

- 作用：指定当前方法要监听的队列
- <span style="color:blue;font-weight:bolder;">注意</span>：此时框架不会创建相关交换机和队列，必须提前创建好

## 四.生产者工程

### 4.1 配置POM

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.5</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
</dependencies>
```

### 4.2 YAML

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: 123456
    virtual-host: /
```

### 4.3 主启动类

```java
package com.nateshao.producer.mp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
@SpringBootApplication
public class RabbitMQProducerMainType {
    public static void main(String[] args) {
        SpringApplication.run(RabbitMQProducerMainType.class, args);
    }
}
```



### 4.4 测试程序

```java
package com.nateshao.producer;

import com.nateshao.producer.mp.RabbitMQProducerMainType;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RabbitMQProducerMainType.class)
class RabbitMQTest {

    @Test
    void contextLoads() {
    }

    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void test01SendMessage() {
        rabbitTemplate.convertAndSend(EXCHANGE_DIRECT, ROUTING_KEY, "Hello Rabbit!我是千羽!");
    }

}
```

## 4.5 运行结果

先运行消费者，再运行生产者

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240601104325147.png)



消费端控制台打印：

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240601104427301.png)

















