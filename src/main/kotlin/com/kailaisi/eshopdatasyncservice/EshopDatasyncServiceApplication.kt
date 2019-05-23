package com.kailaisi.eshopdatasyncservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
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
        return JedisPool(config, "localhost", 6379)
    }
}

fun main(args: Array<String>) {
    runApplication<EshopDatasyncServiceApplication>(*args)
}
