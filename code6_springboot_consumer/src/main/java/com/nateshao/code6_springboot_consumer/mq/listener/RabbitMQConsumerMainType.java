package com.nateshao.code6_springboot_consumer.mq.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitMQConsumerMainType {
    public static void main(String[] args) {
        SpringApplication.run(RabbitMQConsumerMainType.class, args);
    }
}
