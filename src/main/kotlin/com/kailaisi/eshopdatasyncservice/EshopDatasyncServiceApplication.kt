package com.kailaisi.eshopdatasyncservice

import com.kailaisi.eshopdatasyncservice.listener.InitListener
import com.kailaisi.eshopdatasyncservice.rabbitmq.QueueProcess
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
class EshopDatasyncServiceApplication{
    @Bean
    fun jedis(): JedisPool {
        val config = JedisPoolConfig()
        config.maxTotal = 3000
        config.maxIdle = 5
        config.maxWaitMillis = 1000 * 100
        config.testOnBorrow = true
        return JedisPool(config, "192.168.11.11", 1111)
    }

    @Bean
    fun servletListenerRegistrationBean(): ServletListenerRegistrationBean<*> {
        val servletListenerRegistrationBean = ServletListenerRegistrationBean<InitListener>()
        servletListenerRegistrationBean.listener = InitListener()
        return servletListenerRegistrationBean
    }
}

fun main(args: Array<String>) {
    runApplication<EshopDatasyncServiceApplication>(*args)
}
