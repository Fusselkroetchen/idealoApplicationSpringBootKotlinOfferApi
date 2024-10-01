package de.idealo.productapi.web

import de.idealo.productapi.entitie.ProductEntitie
import de.idealo.productapi.repository.ProductRepository
import de.idealo.productapi.repository.ProductRepository.OrderBy
import de.idealo.productapi.repository.ProductRepository.OrderDirection
import de.idealo.productapi.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult


@RestController
@RequestMapping
class ProductApiController(val productService: ProductService) {
    /**
     * @param groupByBrand group by brand
     * @param filterTitle filter products in list. Use wildcards and other operators: https://www.w3schools.com/sql/sql_like.asp
     */
    private fun getProductList(
        limit: Int?,
        offset: Int?,
        site: String,
        filterTitle: String?,
        groupByBrand: Boolean?,
        orderBy: OrderBy? = OrderBy.PRICE,
        orderDirection : OrderDirection? = OrderDirection.ASC
    ) : DeferredResult<ResponseEntity<ProductApiResponse?>?> {
        val deferredResult = DeferredResult<ResponseEntity<ProductApiResponse?>?>()

        productService.getProductListResponse(
            limit, offset, site, filterTitle, groupByBrand, orderBy, orderDirection
        ) {
            if (it == null) {
                deferredResult.setResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
                )
            }

            deferredResult.setResult(
                ResponseEntity.status(HttpStatus.OK).body(it)
            )
        }

        return deferredResult
    }

    @CrossOrigin
    @RequestMapping("/{version:[0-9.]+}/{site:[A-Za-z]{2}}/findProduct/{filterTitle:[A-Za-z]+}")
    fun findProduct(
        @PathVariable version: String,
        @PathVariable site: String,
        @PathVariable filterTitle: String,
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("offset", required = false) offset: Int?,
        @RequestParam("orderBy", required = false) orderBy: ProductRepository.OrderBy?,
        @RequestParam("orderDirection", required = false) orderDirection: ProductRepository.OrderDirection?,
        @RequestParam("groupByBrand", required = false) groupByBrand: Boolean?
    ): DeferredResult<ResponseEntity<ProductApiResponse?>?> {
        return getProductList(
            limit, offset, site, "$filterTitle%", groupByBrand, orderBy, orderDirection
        )
    }

    @CrossOrigin
    @RequestMapping("/{version:[0-9.]+}/{site:[A-Za-z]{2}}/getProducts")
    fun getProducts(
        @PathVariable version: String,
        @PathVariable site: String,
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("offset", required = false) offset: Int?,
        @RequestParam("orderBy", required = false) orderBy: ProductRepository.OrderBy?,
        @RequestParam("orderDirection", required = false) orderDirection: ProductRepository.OrderDirection?,
        @RequestParam("filterTitle", required = false) filterTitle: String?,
        @RequestParam("groupByBrand", required = false) groupByBrand: Boolean?
    ): DeferredResult<ResponseEntity<ProductApiResponse?>?> {
        return getProductList(
            limit, offset, site, filterTitle, groupByBrand, orderBy, orderDirection
        )
    }

    @CrossOrigin
    @RequestMapping("/{version:[0-9.]+}/{site:[A-Za-z]{2}}/getBrandGroupedProducts")
    fun getBrandGroupedProducts(
        @PathVariable version: String,
        @PathVariable site: String,
        @RequestParam("orderBy", required = false) orderBy: ProductRepository.OrderBy?,
        @RequestParam("orderDirection", required = false) orderDirection: ProductRepository.OrderDirection?,
        @RequestParam("filterTitle", required = false) filterTitle: String?
    ): DeferredResult<ResponseEntity<ProductApiResponse?>?> {
        return getProductList(
            null, null, site, filterTitle, true, orderBy, orderDirection
        )
    }

    @CrossOrigin
    @PostMapping("/{version:[0-9.]+}/{site:[A-Za-z]{2}}/addNewProduct")
    fun addNewProduct(
        @PathVariable version: String,
        @PathVariable site: String,
        @RequestParam("title", required = true) title: String,
        @RequestParam("brand", required = true) brand: String,
        @RequestParam("price", required = true) price : Double,
        @RequestParam("quantity", required = true) quantity: Int,
        @RequestParam("description", required = true) description: String,
        @RequestParam("shopUrl", required = true) shopUrl: String
    ): ResponseEntity<String?>? {
        val product = ProductEntitie()
        product.title = title
        product.brand = brand
        product.price = price
        product.quantity = quantity
        product.description = description
        product.shopUrl = shopUrl
        product.site = site

        if (!productService.addNewProduct(product)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Maybe Product exists or other error occurred"
            )
        }

        return ResponseEntity.status(HttpStatus.OK).body(
            "successfully added new product"
        )
    }
}