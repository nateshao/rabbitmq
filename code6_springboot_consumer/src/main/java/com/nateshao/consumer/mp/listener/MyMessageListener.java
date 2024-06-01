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

    // 写法一：监听 + 在 RabbitMQ 服务器上创建交换机、队列
    @RabbitListener(bindings = @QueueBinding(
            // value = @Queue：给value属性赋值,QUEUE_NAME：队列名称,durable ="true" ：持久化
            value = @Queue(value = QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = EXCHANGE_DIRECT), // exchange指定交换机exchange；@Exchange给exchange属性赋值；EXCHANGE_DIRECT交换机名称
            key = {ROUTING_KEY} // 指定路由键信息
        )
    )

    // 写法二：监听
    // @RabbitListener(queues = {QUEUE_NAME})
    public void processMessage(String dataString, Message message, Channel channel) {
        log.info("消费端接收到了消息：" + dataString);
    }
}
