//package com.nateshao.producer.mq.config;
//
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.ReturnedMessage;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * @Author 千羽
// * @公众号 程序员千羽
// * @Date 2024/6/1 14:18
// * @Version 1.0
// */
//@Component
//@Slf4j
//public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @PostConstruct
//    public void init() {
//        rabbitTemplate.setConfirmCallback(this);
//        rabbitTemplate.setReturnsCallback(this);
//    }
//
//    @Override
//    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//        if (ack) {
//            log.info("消息发送到交换机成功！数据：" + correlationData);
//            log.info("消息发送到交换机成功！数据ack：" + ack);
//        } else {
//            log.info("消息发送到交换机失败！数据：" + correlationData + " 原因：" + cause);
//            log.info("消息发送到交换机失败！数据ack：" + ack + " 原因：" + cause);
//        }
//    }
//
//    @Override
//    public void returnedMessage(ReturnedMessage returned) {
//        log.info("消息主体: " + new String(returned.getMessage().getBody()));
//        log.info("应答码: " + returned.getReplyCode());
//        log.info("描述：" + returned.getReplyText());
//        log.info("消息使用的交换器 exchange : " + returned.getExchange());
//        log.info("消息使用的路由键 routing : " + returned.getRoutingKey());
//    }
//}
