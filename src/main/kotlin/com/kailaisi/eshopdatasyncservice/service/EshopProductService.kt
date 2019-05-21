package com.kailaisi.eshopdatasyncservice.service

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 *描述：
 *<p/>作者：wu
 *<br/>创建时间：2019/5/21 10:26
 */
@FeignClient("product-server")
interface EshopProductService {
    @RequestMapping("/brand/findById", method = arrayOf(RequestMethod.POST))
    fun findBrandById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/cagetory/findById", method = arrayOf(RequestMethod.POST))
    fun findCategoryById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product/findById", method = arrayOf(RequestMethod.POST))
    fun findProductById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product_desc/findById", method = arrayOf(RequestMethod.POST))
    fun findProductDescById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product-specification/findById", method = arrayOf(RequestMethod.POST))
    fun findProductSpecificationById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product-property/findById", method = arrayOf(RequestMethod.POST))
    fun findProductPropertyById(@RequestParam(value = "id") id: Long): String

}