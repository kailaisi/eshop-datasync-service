package com.kailaisi.eshopdatasyncservice.rabbitmq

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.kailaisi.eshopdatasyncservice.service.EshopProductService
import com.kailaisi.eshopdatasyncservice.util.FastJsonUtil
import org.apache.commons.lang.StringUtils
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import redis.clients.jedis.JedisPool
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
@RabbitListener(queues = arrayOf("refresh-data-change-queue"))
class RefreshDataChangeQueueReceive {
    @Autowired
    lateinit var productService: EshopProductService
    @Autowired
    lateinit var jedisPool: JedisPool
    @Autowired
    lateinit var rabbitMQSender: RabbitMQSender
    //消息队列
    var dimRabbitMessageSendSet = Collections.synchronizedSet(HashSet<String>())
    //brand的消息队列
    var productDataChangeList = arrayListOf<DataChange>()
    //brand的消息队列
    var brandDataChangeList = arrayListOf<DataChange>()
    //brand的消息队列
    var categoryDataChangeList = arrayListOf<DataChange>()

    init {
        println(Thread.currentThread().name)
        thread(start = true) {
            println(Thread.currentThread().name)
            while (true) {
                dimRabbitMessageSendSet.forEach {
                    rabbitMQSender.send(RabbitQueue.REFRESH_AGGR_DATA_CHANGE_QUEUE, it)
                    println("发送刷新聚合数据$it")
                }
                dimRabbitMessageSendSet.clear()
                try {
                    Thread.sleep(5000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @RabbitHandler
    fun process(msg: String) {
        println("接收刷新到原子数据$msg")
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
        var jedis = jedisPool.resource
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val productDescById = productService.findProductDescById(bean.id)
                jedis.set("product_property_${bean.productId}", productDescById)
            }
            EventType.DELETE -> jedis.del("product_property_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.productId!!, bean.productId!!)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
        jedis.close()
    }

    /**
     * 产品规格
     */
    private fun processProductSpecificationDataChangeMessage(bean: DataChange) {
        var jedis = jedisPool.resource
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val productDescById = productService.findProductDescById(bean.id)
                jedis.set("product_specification_${bean.productId}", productDescById)
            }
            EventType.DELETE -> jedis.del("product_specification_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT, bean.productId!!, bean.productId!!)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
        jedis.close()
    }

    /**
     * 产品描述
     */
    private fun processProductDescDataChangeMessage(bean: DataChange) {
        var resource = jedisPool.resource
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                val productDescById = productService.findProductDescById(bean.id)
                resource.set("product_desc_${bean.productId}", productDescById)
            }
            EventType.DELETE -> resource.del("product_desc_${bean.productId}")
        }
        val sendData = AggrDataChange(DataType.PRODUCT_DESC, bean.productId!!, bean.productId!!)
        dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
        resource.close()
    }

    /**
     * 产品信息
     */
    private fun processProductDataChangeMessage(bean: DataChange) {
        var resource = jedisPool.resource
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                productDataChangeList.add(bean)
                if (productDataChangeList.size > 20) {
                    val ids = arrayListOf<Long>()
                    productDataChangeList.forEach { ids.add(it.id) }
                    val join = StringUtils.join(ids, ",")
                    val objectJson = JSONArray.parseArray(productService.findProductByIds(join))
                    objectJson.forEach {
                        if (it is JSONObject) {
                            resource.set("product_${it.getLong("id")}", it.toString())
                            val sendData = AggrDataChange(DataType.PRODUCT, it.getLong("id"), null)
                            dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
                        }
                    }
                    brandDataChangeList.clear()
                }
            }
            EventType.DELETE -> {
                resource.del("product_${bean.id}")
                val sendData = AggrDataChange(DataType.PRODUCT, bean.id, null)
                dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
            }
        }
        resource.close()
    }

    /**
     * 产品种类
     */
    private fun processCategoryDataChangeMessage(bean: DataChange) {
        var resource = jedisPool.resource
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                categoryDataChangeList.add(bean)
                if (categoryDataChangeList.size > 20) {
                    val ids = arrayListOf<Long>()
                    categoryDataChangeList.forEach { ids.add(it.id) }
                    val join = StringUtils.join(ids, ",")
                    val objectJson = JSONArray.parseArray(productService.findCategoryByIds(join))
                    objectJson.forEach {
                        if (it is JSONObject) {
                            resource.set("category_${it.getLong("id")}", it.toString())
                            val sendData = AggrDataChange(DataType.CATEGORY, it.getLong("id"), null)
                            dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
                        }
                    }
                    brandDataChangeList.clear()
                }
            }
            EventType.DELETE -> {
                resource.del("category_${bean.id}")
                val sendData = AggrDataChange(DataType.CATEGORY, bean.id, null)
                dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
            }
        }
        resource.close()
    }

    /**
     * 品牌信息
     */
    private fun processBrandDataChangeMessage(bean: DataChange) {
        var resource = jedisPool.resource
        when (bean.event_type) {
            EventType.ADD, EventType.UPDATE -> {
                brandDataChangeList.add(bean)
                if (brandDataChangeList.size > 20) {
                    val ids = arrayListOf<Long>()
                    brandDataChangeList.forEach { ids.add(it.id) }
                    val join = StringUtils.join(ids, ",")
                    val objectJson = JSONArray.parseArray(productService.findBrandByIds(join))
                    objectJson.forEach {
                        if (it is JSONObject) {
                            resource.set("brand_${it.getLong("id")}", it.toString())
                            val sendData = AggrDataChange(DataType.BRAND, it.getLong("id"), null)
                            dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
                        }
                    }
                    brandDataChangeList.clear()
                }
            }
            EventType.DELETE -> {
                resource.del("brand_${bean.id}")
                val sendData = AggrDataChange(DataType.BRAND, bean.id, null)
                dimRabbitMessageSendSet.add(FastJsonUtil.bean2Json(sendData))
            }
        }
        resource.close()
    }
}

