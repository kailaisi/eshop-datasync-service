package com.kailaisi.eshopdatasyncservice.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
@RabbitListener(queues = arrayOf("refresh-data-change-queue"))
class RefreshDataChangeQueueReceive {
    var process: QueueProcess = QueueProcess(RabbitQueue.REFRESH_AGGR_DATA_CHANGE_QUEUE)
    @RabbitHandler
    fun process(msg: String) {
        println("接收刷新到原子数据$msg")
        process.process(msg)
    }
}
