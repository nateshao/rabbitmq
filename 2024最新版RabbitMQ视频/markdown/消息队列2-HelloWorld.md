# demo：输出一个HelloWorld

生产者发送消息，消费者接收消息，用最简单的方式实现

也可以看一下官网的demo：https://www.rabbitmq.com/tutorials/tutorial-one-java.html

## 1、具体操作

1、创建工程，以Java为例子

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240529204123788.png)

### ①添加依赖

```xml
<dependencies>
    <dependency>
        <groupId>com.rabbitmq</groupId>
        <artifactId>amqp-client</artifactId>
        <version>5.20.0</version>
    </dependency>
</dependencies>
```

### ②消息接收端（消费者）

```java
package com.nateshao.hello_world;

import com.rabbitmq.client.*;

import java.io.IOException;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class Consumer {

    public static void main(String[] args) throws Exception {

        // 1.创建连接工厂  
        ConnectionFactory factory = new ConnectionFactory();

        // 2. 设置参数  
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("guest");
        factory.setPassword("123456");

        // 3. 创建连接 Connection        
        Connection connection = factory.newConnection();

        // 4. 创建Channel  
        Channel channel = connection.createChannel();

        // 5. 创建队列  
        // 如果没有一个名字叫simple_queue的队列，则会创建该队列，如果有则不会创建  
        // 参数1. queue：队列名称  
        // 参数2. durable：是否持久化。如果持久化，则当MQ重启之后还在  
        // 参数3. exclusive：是否独占。  
        // 参数4. autoDelete：是否自动删除。当没有Consumer时，自动删除掉  
        // 参数5. arguments：其它参数。  
        // channel.queueDeclare("simple_queue",true,false,false,null);

        // 接收消息  
        DefaultConsumer consumer = new DefaultConsumer(channel) {

            // 回调方法,当收到消息后，会自动执行该方法  
            // 参数1. consumerTag：标识  
            // 参数2. envelope：获取一些信息，交换机，路由key...  
            // 参数3. properties：配置信息  
            // 参数4. body：数据  
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                System.out.println("consumerTag：" + consumerTag);
                System.out.println("Exchange：" + envelope.getExchange());
                System.out.println("RoutingKey：" + envelope.getRoutingKey());
                System.out.println("properties：" + properties);
                System.out.println("body：" + new String(body));
            }
        };
        // 参数1. queue：队列名称  
        // 参数2. autoAck：是否自动确认，类似咱们发短信，发送成功会收到一个确认消息  
        // 参数3. callback：回调对象  
        // 消费者类似一个监听程序，主要是用来监听消息  
        channel.basicConsume("simple_queue", true, consumer);
    }
}
```

### ③消息发送端（生产者）

```java
package com.nateshao.hello_world;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:10
 * @Version 1.0
 */
public class Producer {

    public static void main(String[] args) throws Exception {

        // 创建连接工厂  
        ConnectionFactory connectionFactory = new ConnectionFactory();

        // 设置主机地址  
        connectionFactory.setHost("127.0.0.1");

        // 设置连接端口号：默认为 5672
        connectionFactory.setPort(5672);

        // 虚拟主机名称：默认为 /
        connectionFactory.setVirtualHost("/");

        // 设置连接用户名；默认为guest  
        connectionFactory.setUsername("guest");

        // 设置连接密码；默认为guest  
        connectionFactory.setPassword("123456");

        // 创建连接  
        Connection connection = connectionFactory.newConnection();

        // 创建频道  
        Channel channel = connection.createChannel();

        // 声明（创建）队列  
        // queue      参数1：队列名称  
        // durable    参数2：是否定义持久化队列，当 MQ 重启之后还在  
        // exclusive  参数3：是否独占本次连接。若独占，只能有一个消费者监听这个队列且 Connection 关闭时删除这个队列  
        // autoDelete 参数4：是否在不使用的时候自动删除队列，也就是在没有Consumer时自动删除  
        // arguments  参数5：队列其它参数  
        channel.queueDeclare("simple_queue", true, false, false, null);

        // 要发送的信息  
        String message = "你好；千羽！";

        // 参数1：交换机名称,如果没有指定则使用默认Default Exchange  
        // 参数2：路由key,简单模式可以传递队列名称  
        // 参数3：配置信息  
        // 参数4：消息内容  
        channel.basicPublish("", "simple_queue", null, message.getBytes());

        System.out.println("已发送消息：" + message);

        // 关闭资源  
        channel.close();
        connection.close();

    }

}
```

## 2、发送消息

先运行生产者，再运行消费者。

### 运行生产者，查看效果

控制台打印

```java
已发送消息：你好；千羽！

Process finished with exit code 0
```

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240529204955314.png)

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240529204403768.png)



![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240529204558065.png)





## 3、接收消息

### 控制台打印

```java
"C:\Program Files\Java\jdk-17\bin\java.exe" "-javaagent:D:\Program Files\JetBrains\IntelliJ IDEA 2024.1.1\lib\idea_rt.jar=52244:D:\Program Files\JetBrains\IntelliJ IDEA 2024.1.1\bin" -Dfile.encoding=UTF-8 -classpath 
......
consumerTag：amq.ctag-bAgTsr5fp03WXRcIyph2sg
Exchange：
RoutingKey：simple_queue
properties：#contentHeader<basic>(content-type=null, content-encoding=null, headers=null, delivery-mode=null, priority=null, correlation-id=null, reply-to=null, expiration=null, message-id=null, timestamp=null, type=null, user-id=null, app-id=null, cluster-id=null)
body：你好；千羽！

```

### ③查看后台管理界面

因为消息被消费掉了，所以RabbitMQ服务器上没有了：

![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wximage-20240529205035663.png)