package com.atguigu.mq.stream;

import com.rabbitmq.stream.Environment;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class TestConsumer {

    public static void main(String[] args) {
        Environment environment = Environment.builder()
                .host("localhost")
                .port(33333)
                .username("atguigu")
                .password("123456")
                .build();

        environment.consumerBuilder()
                .stream("stream.atguigu.test")
                .name("stream.atguigu.test.consumer")
                .autoTrackingStrategy()
                .builder()
                .messageHandler((offset, message) -> {
                    byte[] bodyAsBinary = message.getBodyAsBinary();
                    String messageContent = new String(bodyAsBinary);
                    System.out.println("[消费者端]messageContent = " + messageContent + " Offset=" + offset.offset());
                })
                .build();
    }

}
