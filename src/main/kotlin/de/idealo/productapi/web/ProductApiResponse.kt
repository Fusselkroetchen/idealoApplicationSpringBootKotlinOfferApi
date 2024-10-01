package de.idealo.productapi.web

import de.idealo.productapi.entitie.ProductEntitie

interface ProductApiResponse

class ProductListResponse : ProductApiResponse {
    var productCount: Int? = null
    var products : List<ProductEntitie>? = null
}

class BrandGroupProductListResponse : ProductApiResponse {
    var productCount: Int? = null
    var products : Map<String, List<ProductEntitie>>? = null
}