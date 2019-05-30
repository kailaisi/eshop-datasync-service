package com.kailaisi.eshopdatasyncservice.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
@RabbitListener(queues = arrayOf("high-priority-data-change-queue"))
class HighPriorityDataChangeQueueReceive {
    var process: QueueProcess = QueueProcess(RabbitQueue.HIGH_PRIORITY_AGGR_DATA_CHANGE_QUEUE)
    @RabbitHandler
    fun process(msg: String) {
        println("接收到高优先级原子数据$msg")
        process.process(msg)
    }
}