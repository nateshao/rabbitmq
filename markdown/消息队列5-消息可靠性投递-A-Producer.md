[TOC]

# 【消息队列】如何保障消息可靠性投递

在现代分布式系统中，消息队列扮演着至关重要的角色，它不仅能够解耦系统组件，还能提高系统的扩展性和可维护性。

加上面试消息队列的八股文最喜欢问这个了。消息可靠性投递，这个对于消息队列非常重要，很多时候，我们不是人为的去干预，但是仍然出现其他意外的事情

导致消息在传递过程中可能会面临丢失、重复、损坏等问题，这就要求我们必须重视消息的可靠性投递。

#### 什么是消息可靠性投递？

> 消息可靠性投递的目标是确保消息能够从生产者（Producer）可靠地传递到消费者（Consumer），并且在传递过程中不丢失、不重复、不损坏。实现这一目标需要采取多种技术手段和策略。

下面列举三个栗子：

![故障情况1](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wx%E6%95%85%E9%9A%9C1.drawio.png)

故障情况1：**消息没有发送到消息队列上**。导致消费者拿不到消息，业务功能缺失，数据错误

- **解决思路A**：在生产者端进行确认，具体操作中我们会分别针对交换机和队列来确认，如果没有成功发送到消息队列服务器上，那就可以尝试重新发送

- **解决思路B**：为目标交换机指定备份交换机，当目标交换机投递失败时，把消息投递至备份交换机

![故障情况2](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wx%E6%95%85%E9%9A%9C2.png)

故障情况2：**消息成功存入消息队列，但是消息队列服务器宕机了**。原本保存在内存中的消息也丢失了。即使服务器重新启动，消息也找不回来了

- 导致消费者拿不到消息，业务功能缺失，数据错误

- **解决思路**：消息持久化到硬盘上，哪怕服务器重启也不会导致消息丢失



![故障情况3](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wx%E6%95%85%E9%9A%9C3.png)

故障情况3：**消息成功存入消息队列，但是消费端出现问题**，例如：宕机、抛异常等等

- 导致业务功能缺失，数据错误

**解决思路**

1. 消费端消费消息成功，给服务器返回`ACK`信息，然后消息队列删除该消息

2. 消费端消费消息失败，给服务器端返回`NACK`信息。
3. 同时把消息恢复为待消费的状态，这样就可以再次取回消息，重试一次（当然，这就需要消费端接口支持幂等性）

# 故障情况1：消息没有发送到消息队列上

<img src="https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/image-20240530231127296.png" style="zoom:50%;" />

## 1. 生产者代码demo演示

## 1.1 配置POM

```xml
 <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.nateshao</groupId>
    <artifactId>code7_confirm_consumer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>code7_confirm_consumer</name>
    <description>code7_confirm_consumer</description>
    <properties>
        <java.version>17</java.version>
    </properties>

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

## 1.2 主启动类

没有特殊设定：

```java
package com.nateshao.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
@SpringBootApplication
public class RabbitMQConsumerMainType {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQConsumerMainType.class, args);
    }
}
```

## 1.3 YAML

<span style="color:blue;font-weight:bold;">注意</span>：`publisher-confirm-type`和`publisher-returns`是两个必须要增加的配置，如果没有则本节功能不生效

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: 123456
    virtual-host: /
    publisher-confirm-type: CORRELATED # 交换机的确认
    publisher-returns: true # 队列的确认
logging:
  level:
    com.nateshao.producer.config.MQProducerAckConfig: info
```

## 2. 创建配置类

在这里我们为什么要创建这个配置类呢？首先，我们需要声明回调函数来接收RabbitMQ服务器返回的确认信息：

| 方法名            | 方法功能                 | 所属接口        | 接口所属类     |
| ----------------- | ------------------------ | --------------- | -------------- |
| confirm()         | 确认消息是否发送到交换机 | ConfirmCallback | RabbitTemplate |
| returnedMessage() | 确认消息是否发送到队列   | ReturnsCallback | RabbitTemplate |

然后，就是对RabbitTemplate的功能进行增强，因为回调函数所在对象必须设置到RabbitTemplate对象中才能生效。

原本RabbitTemplate对象并没有生产者端消息确认的功能，要给它设置对应的组件才可以。

而设置对应的组件，需要调用RabbitTemplate对象下面两个方法：

| 设置组件调用的方法   | 所需对象类型            |
| -------------------- | ----------------------- |
| setConfirmCallback() | ConfirmCallback接口类型 |
| setReturnCallback()  | ReturnCallback接口类型  |

## 2.1 API说明

### ①ConfirmCallback接口

这是RabbitTemplate内部的一个接口，源代码如下：

```java
	/**
	 * A callback for publisher confirmations.
	 *
	 */
	@FunctionalInterface
	public interface ConfirmCallback {

		/**
		 * Confirmation callback.
		 * @param correlationData correlation data for the callback.
		 * @param ack true for ack, false for nack
		 * @param cause An optional cause, for nack, when available, otherwise null.
		 */
		void confirm(@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause);

	}
```

生产者端发送消息之后，回调confirm()方法

- **ack参数值为`true`：表示消息成功发送到了交换机**
- **ack参数值为`false`：表示消息没有发送到交换机**

### ②ReturnCallback接口

同样也RabbitTemplate内部的一个接口，源代码如下：

```java
	/**
	 * A callback for returned messages.
	 *
	 * @since 2.3
	 */
	@FunctionalInterface
	public interface ReturnsCallback {

		/**
		 * Returned message callback.
		 * @param returned the returned message and metadata.
		 */
		void returnedMessage(ReturnedMessage returned);

	}
```

<span style="color:blue;font-weight:bold;">注意</span>：接口中的returnedMessage()方法<span style="color:blue;font-weight:bold;font-size:25px;">仅</span>在消息<span style="color:blue;font-weight:bold;font-size:25px;">没有</span>发送到队列时调用

ReturnedMessage类中主要属性含义如下：

| 属性名     | 类型                                  | 含义                         |
| ---------- | ------------------------------------- | ---------------------------- |
| message    | org.springframework.amqp.core.Message | 消息以及消息相关数据         |
| replyCode  | int                                   | 应答码，类似于HTTP响应状态码 |
| replyText  | String                                | 应答码说明                   |
| exchange   | String                                | 交换机名称                   |
| routingKey | String                                | 路由键名称                   |

## 3.  配置类代码

### 3.1 要点1

加@Component注解，加入IOC容器

### 3.2 要点2

配置类自身实现ConfirmCallback、ReturnCallback这两个接口，然后通过this指针把配置类的对象设置到RabbitTemplate对象中。

操作封装到了一个专门的void init()方法中。

为了保证这个void init()方法在应用启动时被调用，我们使用@PostConstruct注解来修饰这个方法。

关于@PostConstruct注解大家可以参照以下说明：

> @PostConstruct注解是<span style="color:blue;font-weight:bolder;">Java中的一个标准注解</span>，它用于指定在<span style="color:blue;font-weight:bolder;">对象创建之后立即执行</span>的方法。当使用依赖注入（如Spring框架）或者其他方式创建对象时，@PostConstruct注解可以确保在对象完全初始化之后，执行相应的方法。
>
> 使用@PostConstruct注解的方法必须满足以下条件：
>
> 1. <span style="color:blue;font-weight:bolder;">方法不能有任何参数</span>。
> 2. <span style="color:blue;font-weight:bolder;">方法必须是非静态的</span>。
> 3. <span style="color:blue;font-weight:bolder;">方法不能返回任何值</span>。
>
> 当容器实例化一个带有@PostConstruct注解的Bean时，它会在<span style="color:blue;font-weight:bolder;">调用构造函数之后</span>，并在<span style="color:blue;font-weight:bolder;">依赖注入完成之前</span>调用被@PostConstruct注解标记的方法。这样，我们可以在该方法中进行一些初始化操作，比如读取配置文件、建立数据库连接等。

### 3.3 生产者中的代码

有了以上说明，下面我们就可以展示配置类的整体代码：

```java
package com.nateshao.producer.mq.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
@Configuration
@Slf4j
public class RabbitConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        // 消息发送到交换机成功或失败时调用这个方法
        log.info("confirm() 回调函数打印 CorrelationData：" + correlationData);
        log.info("confirm() 回调函数打印 ack：" + ack);
        log.info("confirm() 回调函数打印 cause：" + cause);
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        // 发送到队列失败时才调用这个方法
        log.info("returnedMessage() 回调函数 消息主体: " + new String(returned.getMessage().getBody()));
        log.info("returnedMessage() 回调函数 应答码: " + returned.getReplyCode());
        log.info("returnedMessage() 回调函数 描述：" + returned.getReplyText());
        log.info("returnedMessage() 回调函数 消息使用的交换器 exchange : " + returned.getExchange());
        log.info("returnedMessage() 回调函数 消息使用的路由键 routing : " + returned.getRoutingKey());
    }
}
```



## 4.消费者代码演示

application.yml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 15672
    username: guest
    password: 123456
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: manual # 把消息确认模式改为手动确认
        prefetch: 1 # 每次从队列中取回消息的数量
```

监听类：MyMessageListener.class

```java
package com.nateshao.producer.mq.config;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/6/1 14:18
 * @Version 1.0
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送到交换机成功！数据：" + correlationData);
        } else {
            log.info("消息发送到交换机失败！数据：" + correlationData + " 原因：" + cause);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.info("消息主体: " + new String(returned.getMessage().getBody()));
        log.info("应答码: " + returned.getReplyCode());
        log.info("描述：" + returned.getReplyText());
        log.info("消息使用的交换器 exchange : " + returned.getExchange());
        log.info("消息使用的路由键 routing : " + returned.getRoutingKey());
    }
}
```

## 5.验证效果

5.1 **验证交换机错误**

```java
@Test
public void testExchangeDirectErrorSendMessage() {
    rabbitTemplate.convertAndSend(EXCHANGE_DIRECT + "000", ROUTING_KEY, "Message Test Confirm~~~ ~~~");
}
```

控制台输出，提示：没有找到该交换机`no exchange 'exchange.direct.order~' `
```java
Shutdown Signal: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'exchange.direct.order000' in vhost '/', class-id=60, method-id=40)
confirm() 回调函数打印 CorrelationData：null
confirm() 回调函数打印 ack：false
confirm() 回调函数打印 cause：channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'exchange.direct.order000' in vhost '/', class-id=60, method-id=40)
```

5. 2**验证路由错误**

```java
/**
 * 验证路由地址写错
 */
@Test
public void testRoutingErrorMessage() {
    rabbitTemplate.convertAndSend(EXCHANGE_DIRECT, ROUTING_KEY + "routing ~", "Message Test Confirm~~~ ~~~");
}
```

控制台打印：发送交换机成功`ack：true`，但是路由失败`NO_ROUTE`

```java
returnedMessage() 回调函数 消息主体: Message Test Confirm~~~ ~~~
returnedMessage() 回调函数 应答码: 312
confirm() 回调函数打印 CorrelationData：null
confirm() 回调函数打印 ack：true
confirm() 回调函数打印 cause：null
returnedMessage() 回调函数 描述：NO_ROUTE
returnedMessage() 回调函数 消息使用的交换器 exchange : exchange.direct.order
returnedMessage() 回调函数 消息使用的路由键 routing : queue.orderrouting ~
```

## 6. 完整代码 

```java
@SpringBootTest(classes = RabbitMQProducerMainType.class)
public class RabbitMQTest {  
  
    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order";
  
    @Autowired  
    private RabbitTemplate rabbitTemplate;
  
    @Test
    public void testSendMessage() {
        rabbitTemplate.convertAndSend(EXCHANGE_DIRECT + "~", ROUTING_KEY , "Hello 千羽");
    }
  
}
```

通过调整代码，测试如下三种情况：

- 交换机正确、路由键正确
- 交换机正确、路由键不正确，无法发送到队列
- 交换机不正确，无法发送到交换机