package com.kailaisi.eshopdatasyncservice.fallback

import com.kailaisi.eshopdatasyncservice.service.EshopProductService
import org.springframework.stereotype.Component

@Component
class EshopProductServiceFallback : EshopProductService {
    override fun findProductByIds(id: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findProductDescByIds(id: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findCategoryByIds(id: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findBrandById(id: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findBrandByIds(id: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findCategoryById(id: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findProductById(id: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findProductDescById(id: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findProductSpecificationById(id: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findProductPropertyById(id: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
