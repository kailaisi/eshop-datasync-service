package com.kailaisi.eshopdatasyncservice.rabbitmq

import com.kailaisi.eshopdatasyncservice.service.EshopProductService
import com.kailaisi.eshopdatasyncservice.util.FastJsonUtil
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import redis.clients.jedis.JedisPool
import java.lang.Thread.currentThread
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

/**
 *描述： * （1）然后通过spring cloud fegion调用product-service服务的各种接口， 获取数据
 * （2）将原子数据在redis中进行增删改
 * （3）将维度数据变化消息写入rabbitmq中另外一个queue，供数据聚合服务来消费
 *<p/>作者：wu
 *<br/>创建时间：2019/5/21 9:26
 */
@Component
@RabbitListener(queues = arrayOf("high-priority-data-change-queue"))
class HighPriorityDataChangeQueueReceive() {
    @Autowired
    lateinit var productService: EshopProductService
    @Autowired
    lateinit var jedisPool: JedisPool
    @Autowired
    lateinit var rabbitMQSender: RabbitMQSender
    //消息队列
    var dimRabbitMessageSendSet = Collections.synchronizedSet(HashSet<String>())

    init {
        println(currentThread().name)
        thread (start = true){
            println(currentThread().name)
            while (true) {
                dimRabbitMessageSendSet.forEach {
                    rabbitMQSender.send(RabbitQueue.HIGH_PRIORITY_AGGR_DATA_CHANGE_QUEUE, it)
                    println("发送高优先级聚合数据$it")
                }
                dimRabbitMessageSendSet.clear()
                try {
                    sleep(5000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @RabbitHandler
    fun process(msg: String) {
        println("接收到高优先级原子数据$msg")
        try {
            val bean = FastJsonUtil.json2Bean(msg, DataChange::class.java)
            when (bean.data_type) {
                DataType.BRAND -> processBrandDataChangeMessage(bean)
                DataType.PRODUCT -> processProductDataChangeMessage(bean)
                DataType.CATEGORY -> processCategoryDataChangeMessage(bean)
                DataType.PRODUCT_DESC -> processProductDescDataChangeMessage(bean)
                DataType.PRODUCT_PROPERTY -> processProductPropertyDataChangeMessage(bean)
                DataType.PRODUCT_SPECIFICATION -> processProductSpecificationDataChangeMessage(bean)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 产品属性
     */
    private fun processProductPropertyDataChangeMessage(bean: DataChange) {
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val productDescById = productService.findProductDescById(bean.id)
                jedisPool.resource.set("product_property_${bean.productId}", productDescById)
            }
            EventType.DELETE -> jedisPool.resource.del("product_property_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.productId!!, bean.productId!!)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 产品规格
     */
    private fun processProductSpecificationDataChangeMessage(bean: DataChange) {
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val productDescById = productService.findProductDescById(bean.id)
                jedisPool.resource.set("product_specification_${bean.productId}", productDescById)
            }
            EventType.DELETE -> jedisPool.resource.del("product_specification_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.productId!!, bean.productId!!)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 产品描述
     */
    private fun processProductDescDataChangeMessage(bean: DataChange) {
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val productDescById = productService.findProductDescById(bean.id)
                jedisPool.resource.set("product_desc_${bean.productId}", productDescById)
            }
            EventType.DELETE -> jedisPool.resource.del("product_desc_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT_DESC, bean.productId!!, bean.productId!!)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 产品信息
     */
    private fun processProductDataChangeMessage(bean: DataChange) {
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val product = productService.findProductById(bean.id)
                jedisPool.resource.set("product_${bean.id}", product)
            }
            EventType.DELETE -> jedisPool.resource.del("product_${bean.id}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.id, bean.id)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 产品种类
     */
    private fun processCategoryDataChangeMessage(bean: DataChange) {
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val category = productService.findCategoryById(bean.id)
                jedisPool.resource.set("category_${bean.id}", category)
            }
            EventType.DELETE -> jedisPool.resource.del("category_${bean.id}")
        }
        val sendData = AggrDataChange(DataType.CATEGORY, bean.id, null)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 品牌信息
     */
    private fun processBrandDataChangeMessage(bean: DataChange) {
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val brand = productService.findBrandById(bean.id)
                jedisPool.resource.set("brand_${bean.id}", brand)
            }
            EventType.DELETE -> jedisPool.resource.del("brand_${bean.id}")
        }
        val sendData = AggrDataChange(DataType.BRAND, bean.id, null)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
    }
}

