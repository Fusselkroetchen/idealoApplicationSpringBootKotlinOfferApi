package de.idealo.productapi.entitie

import jakarta.persistence.*

@Entity
@Table(name = "products")
class ProductEntitie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null
    var title: String? = null
    var brand: String? = null
    var price: Double? = null
    var quantity: Int? = null
    var description: String? = null
    var shopUrl: String? = null
    var site: String? = null
}

@Entity
@Table(name = "products")
class ProductCountEntitie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var count: Int? = null
}