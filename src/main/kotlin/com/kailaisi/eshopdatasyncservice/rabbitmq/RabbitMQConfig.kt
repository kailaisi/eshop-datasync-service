package com.kailaisi.eshopdatasyncservice.rabbitmq

import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *描述：
 *<p/>作者：wu
 *<br/>创建时间：2019/5/23 16:03
 */
@Configuration
class RabbitMQConfig {
    @Bean
    fun dataChangeQueue(): Queue = Queue("data-change-queue")

    @Bean
    fun highPriorityDataChangeQueue(): Queue = Queue("high-priority-data-change-queue")

    @Bean
    fun refreshDataChangeQueue(): Queue = Queue("refresh-data-change-queue")

    @Bean
    fun aggrDataChangeQueue(): Queue = Queue("aggr-data-change-queue")

    @Bean
    fun refreshArrDataChangeQueue(): Queue = Queue("refresh-aggr-data-change-queue")

    @Bean
    fun highPriorityAggrDataChangeQueue(): Queue = Queue("high-priority-aggr-data-change-queue")
}