package com.atguigu.mq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class MyMessageListener {

    public static final String QUEUE_NAME = "fed.queue.demo";

    @RabbitListener(queues = {QUEUE_NAME})
    public void processMessage(String dataString, Message message, Channel channel) throws IOException {
        log.info("[Federation]" + dataString);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}