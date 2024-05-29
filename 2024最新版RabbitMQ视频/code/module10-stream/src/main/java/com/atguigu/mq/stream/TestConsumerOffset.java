package com.atguigu.mq.stream;

import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;

import java.util.concurrent.CountDownLatch;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class TestConsumerOffset {

    public static void main(String[] args) throws InterruptedException {
        Environment environment = Environment.builder()
                .host("localhost")
                .port(33333)
                .username("atguigu")
                .password("123456")
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Consumer consumer = environment.consumerBuilder()
                .stream("stream.atguigu.test")
                .offset(OffsetSpecification.first())
                .messageHandler((offset, message) -> {
                    byte[] bodyAsBinary = message.getBodyAsBinary();
                    String messageContent = new String(bodyAsBinary);
                    System.out.println("[消费者端]messageContent = " + messageContent);
                    countDownLatch.countDown();
                })
                .build();

        countDownLatch.await();

        consumer.close();
    }

}
