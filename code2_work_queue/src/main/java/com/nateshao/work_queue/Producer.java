package com.nateshao.work_queue;


import com.nateshao.work_queue.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
/**
 * @Author 千羽
 * @公众号 程序员千羽
 * @Date 2024/5/29 16:00
 * @Version 1.0
 */
public class Producer {

    public static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws Exception {

        Connection connection = ConnectionUtil.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        for (int i = 1; i <= 10; i++) {

            String body = i + "hello rabbitmq~~~";

            channel.basicPublish("", QUEUE_NAME, null, body.getBytes());

        }

        channel.close();

        connection.close();

    }

}