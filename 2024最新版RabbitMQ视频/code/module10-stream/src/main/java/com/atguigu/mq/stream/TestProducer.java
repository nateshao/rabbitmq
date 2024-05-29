package com.atguigu.mq.stream;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class TestProducer {

    public static void main(String[] args) throws InterruptedException {
        Environment environment = Environment.builder()
                .host("localhost")
                .port(33333)
                .username("atguigu")
                .password("123456")
                .build();

        Producer producer = environment.producerBuilder()
                .stream("stream.atguigu.test")
                .build();

        byte[] messagePayload = "hello rabbit stream".getBytes(StandardCharsets.UTF_8);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        producer.send(
                producer.messageBuilder().addData(messagePayload).build(),
                confirmationStatus -> {
                    if (confirmationStatus.isConfirmed()) {
                        System.out.println("[生产者端]the message made it to the broker");
                    } else {
                        System.out.println("[生产者端]the message did not make it to the broker");
                    }

                    countDownLatch.countDown();
                });

        countDownLatch.await();

        producer.close();

        environment.close();
    }

}
