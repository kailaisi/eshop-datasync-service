package com.kailaisi.eshopdatasyncservice.rabbitmq

import com.alibaba.fastjson.JSONObject
import com.kailaisi.eshopdatasyncservice.service.EshopProductService
import com.kailaisi.eshopdatasyncservice.util.FastJsonUtil
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import redis.clients.jedis.JedisPool

/**
 *描述： * （1）然后通过spring cloud fegion调用product-service服务的各种接口， 获取数据
 * （2）将原子数据在redis中进行增删改
 * （3）将维度数据变化消息写入rabbitmq中另外一个queue，供数据聚合服务来消费
 *<p/>作者：wu
 *<br/>创建时间：2019/5/21 9:26
 */
@Component
@RabbitListener(queues = arrayOf("data-change-queue"))
class DataChangeQueueReceive {
    @Autowired
    lateinit var productService: EshopProductService
    @Autowired
    lateinit var jedisPool: JedisPool
    @Autowired
    lateinit var rabbitMQSender: RabbitMQSender

    @RabbitHandler
    fun process(msg: String) {
        val bean = FastJsonUtil.json2Bean(msg, DataChange::class.java)
        when (bean.data_type) {
            DataType.BRAND -> processBrandDataChangeMessage(bean)
            DataType.PRODUCT -> processProductDataChangeMessage(bean)
            DataType.CATEGORY -> processCategoryDataChangeMessage(bean)
            DataType.PRODUCT_DESC -> processProductDescDataChangeMessage(bean)
            DataType.PRODUCT_PROPERTY -> processProductPropertyDataChangeMessage(bean)
            DataType.PRODUCT_SPECIFICATION -> processProductSpecificationDataChangeMessage(bean)
        }
    }

    /**
     * 产品属性
     */
    private fun processProductPropertyDataChangeMessage(bean: DataChange) {
        when(bean.event_type){
            EventType.ADD,EventType.UPDATE->{
                val productDescById = productService.findProductDescById(bean.id)
                jedisPool.resource.set("product_property_${bean.productId}",productDescById)
            }
            EventType.DELETE->jedisPool.resource.del("product_property_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.productId!!, bean.productId!!)
        rabbitMQSender.send(RabbitQueue.AGGR_DATA_CHANGE_QUEUE, FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 产品规格
     */
    private fun processProductSpecificationDataChangeMessage(bean: DataChange) {
        when(bean.event_type){
            EventType.ADD,EventType.UPDATE->{
                val productDescById = productService.findProductDescById(bean.id)
                jedisPool.resource.set("product_specification_${bean.productId}",productDescById)
            }
            EventType.DELETE->jedisPool.resource.del("product_specification_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.productId!!, bean.productId!!)
        rabbitMQSender.send(RabbitQueue.AGGR_DATA_CHANGE_QUEUE, FastJsonUtil.bean2Json(sendData))
    }

    /**
     * 产品描述
     */
    private fun processProductDescDataChangeMessage(bean: DataChange) {
        when(bean.event_type){
            EventType.ADD,EventType.UPDATE->{
                val productDescById = productService.findProductDescById(bean.id)
                jedisPool.resource.set("product_desc_${bean.productId}",productDescById)
            }
            EventType.DELETE->jedisPool.resource.del("product_desc_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT_DESC, bean.productId!!, bean.productId!!)
        rabbitMQSender.send(RabbitQueue.AGGR_DATA_CHANGE_QUEUE, FastJsonUtil.bean2Json(sendData))
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
        rabbitMQSender.send(RabbitQueue.AGGR_DATA_CHANGE_QUEUE, FastJsonUtil.bean2Json(sendData))
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
        rabbitMQSender.send(RabbitQueue.AGGR_DATA_CHANGE_QUEUE, FastJsonUtil.bean2Json(sendData))
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
        rabbitMQSender.send(RabbitQueue.AGGR_DATA_CHANGE_QUEUE, FastJsonUtil.bean2Json(sendData))
    }
}