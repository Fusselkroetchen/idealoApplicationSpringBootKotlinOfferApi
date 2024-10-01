package de.idealo.productapi.service

import com.google.gson.Gson
import de.idealo.productapi.entitie.ProductEntitie
import de.idealo.productapi.repository.ProductRepository
import de.idealo.productapi.repository.ProductRepository.OrderBy
import de.idealo.productapi.repository.ProductRepository.OrderDirection
import de.idealo.productapi.utils.makeListWithNotNullEl
import de.idealo.productapi.web.BrandGroupProductListResponse
import de.idealo.productapi.web.ProductApiResponse
import de.idealo.productapi.web.ProductListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productServiceDataProcessor : ProductServiceDataProcessor
) {
    @Autowired
    private lateinit var productApiKafkaTemplate: KafkaTemplate<String, String>

    fun getProductListResponse(
        limit: Int?,
        offset: Int?,
        site: String,
        filterTitle: String?,
        groupByBrand: Boolean?,
        orderBy: OrderBy? = OrderBy.PRICE,
        orderDirection : OrderDirection? = OrderDirection.ASC,
        callback: (ProductApiResponse?) -> Boolean
    ) = runBlocking {
        val productListResponse = ProductListResponse()
        val brandGroupProductListResponse = BrandGroupProductListResponse()

        val job1 = CoroutineScope(Dispatchers.Default).launch {
            if (groupByBrand == true) {
                brandGroupProductListResponse.productCount = productRepository.getProductTotalCount(site)
            } else {
                productListResponse.productCount = productRepository.getProductTotalCount(site)
            }
        }

        val job2 = CoroutineScope(Dispatchers.Default).launch {
            if (groupByBrand == true) {
                brandGroupProductListResponse.products =  productServiceDataProcessor.groupByBrand(
                    productRepository.getAllProducts(
                        site, limit, offset, filterTitle, orderBy, orderDirection
                    )
                )
            } else {
                productListResponse.products = productRepository.getAllProducts(
                    site, limit, offset, filterTitle, orderBy, orderDirection
                )
            }
        }

        job1.join()
        job2.join()

        if (groupByBrand == true) {
            callback(brandGroupProductListResponse)
        } else {
            callback(productListResponse)
        }
    }

    fun addNewProduct(
        product : ProductEntitie
    ) : Boolean {
        val notNullList = makeListWithNotNullEl(product.site, product.title)
        if (notNullList.count() != 2) return false

        if (productRepository.checkProductExists(notNullList[0], notNullList[1])) {
            return false //Product already exists
        }

        val success = productRepository.addNewProduct(product)

        if (success) { // send kafka topic
            val gson = Gson()
            val productJSONString = gson.toJson(product)

            productApiKafkaTemplate.send("ProductApi", productJSONString)
        }

        return success
    }

}