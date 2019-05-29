package com.kailaisi.eshopdatasyncservice.service

import com.kailaisi.eshopdatasyncservice.fallback.EshopProductServiceFallback
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 *描述：
 *<p/>作者：wu
 *<br/>创建时间：2019/5/21 10:26
 */
@FeignClient("product-server", fallback = EshopProductServiceFallback::class)
interface EshopProductService {
    @RequestMapping("/brand/findById", method = [RequestMethod.POST])
    fun findBrandById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/brand/findByIds", method = [RequestMethod.POST])
    fun findBrandByIds(@RequestParam(value = "id") id: String): String

    @RequestMapping("/category/findById", method = [RequestMethod.POST])
    fun findCategoryById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/category/findByIds", method = [RequestMethod.POST])
    fun findCategoryByIds(@RequestParam(value = "id") id: String): String

    @RequestMapping("/product/findById", method = [RequestMethod.POST])
    fun findProductById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product/findByIds", method = [RequestMethod.POST])
    fun findProductByIds(@RequestParam(value = "id") id: String): String

    @RequestMapping("/product_desc/findById", method = [RequestMethod.POST])
    fun findProductDescById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product_desc/findByIds", method = [RequestMethod.POST])
    fun findProductDescByIds(@RequestParam(value = "id") id: String): String

    @RequestMapping("/product-specification/findById", method = [RequestMethod.POST])
    fun findProductSpecificationById(@RequestParam(value = "id") id: Long): String

    @RequestMapping("/product-property/findById", method = [RequestMethod.POST])
    fun findProductPropertyById(@RequestParam(value = "id") id: Long): String

}