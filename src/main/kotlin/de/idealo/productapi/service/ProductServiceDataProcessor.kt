package de.idealo.productapi.service

import de.idealo.productapi.entitie.ProductEntitie
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProductServiceDataProcessor {
    fun groupByBrand(list : List<ProductEntitie>) : Map<String, List<ProductEntitie>>? {
        val brandGroupMap : MutableMap<String, MutableList<ProductEntitie>> = mutableMapOf()

        for (product in list) {
            val brandName = product.brand ?: continue

            if (!brandGroupMap.containsKey(brandName)) {
                brandGroupMap[brandName] = ArrayList()
            }
            brandGroupMap[brandName]?.add(product)
        }

        return brandGroupMap
    }
}