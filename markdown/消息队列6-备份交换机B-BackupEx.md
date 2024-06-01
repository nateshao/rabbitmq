# 故障情况2：备份交换机

故障情况2：**消息成功存入消息队列，但是消息队列服务器宕机了**。原本保存在内存中的消息也丢失了。即使服务器重新启动，消息也找不回来了

- 导致消费者拿不到消息，业务功能缺失，数据错误

- **解决思路**：消息持久化到硬盘上，哪怕服务器重启也不会导致消息丢失



![](https://nateshao-blog.oss-cn-shenzhen.aliyuncs.com/wx%E5%A4%87%E4%BB%BD%E4%BA%A4%E6%8D%A2%E6%9C%BA.png)

# 一、创建备份交换机

## 1、创建备份交换机

<span style="color:blue;font-weight:bolder;">注意</span>：备份交换机<span style="color:blue;font-weight:bolder;">一定</span>要选择<span style="color:blue;font-weight:bolder;">fanout类型</span>，因为原交换机转入备份交换机时并不会指定路由键

![image-20231203231926247](assets/image-20231203231926247.png)



![image-20231202183701454](assets/image-20231202183701454.png)



## 2、创建备份交换机要绑定的队列

### ①创建队列

![image-20231202183906676](assets/image-20231202183906676.png)





![image-20231202183949674](assets/image-20231202183949674.png)



### ②绑定交换机

<span style="color:blue;font-weight:bolder;">注意</span>：这里是要和备份交换机绑定

![image-20231203232801504](assets/image-20231203232801504.png)



## 3、针对备份队列创建消费端监听器

```java
    public static final String EXCHANGE_DIRECT_BACKUP = "exchange.direct.order.backup";
    public static final String QUEUE_NAME_BACKUP  = "queue.order.backup";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QUEUE_NAME_BACKUP, durable = "true"),
            exchange = @Exchange(value = EXCHANGE_DIRECT_BACKUP),
            key = {""}
    ))
    public void processMessageBackup(String dateString,
                                     Message message,
                                     Channel channel) {
        log.info("BackUp: " + dateString);
    }
```



# 二、设定备份关系

## 1、原交换机删除

·

![image-20231202184840124](assets/image-20231202184840124.png)



## 2、重新创建原交换机

![image-20231202185211633](assets/image-20231202185211633.png)



![image-20231202185342087](assets/image-20231202185342087.png)



## 3、原交换机重新绑定原队列

![image-20231202190111581](assets/image-20231202190111581.png)



![image-20231202185955138](assets/image-20231202185955138.png)



![image-20231202190036520](assets/image-20231202190036520.png)



# 三、测试

- 启动消费者端
- 发送消息，但是路由键不对，于是转入备份交换机
