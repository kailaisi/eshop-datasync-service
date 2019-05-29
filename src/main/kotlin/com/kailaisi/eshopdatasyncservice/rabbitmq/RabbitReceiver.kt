package com.kailaisi.eshopdatasyncservice.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 *描述：
 *<p/>作者：wu
 *<br/>创建时间：2019/5/29 15:29
 */
@Component
@RabbitListener(queues = arrayOf("data-change-queue"))
class RabbitReceiver {
    var process: QueueProcess = QueueProcess(RabbitQueue.REFRESH_AGGR_DATA_CHANGE_QUEUE)
    @RabbitHandler
    fun process(msg: String) {
        println("接收到高优先级原子数据$msg")
        process.process(msg)
    }
}

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
