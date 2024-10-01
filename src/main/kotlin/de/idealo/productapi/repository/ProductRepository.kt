package de.idealo.productapi.repository

import de.idealo.productapi.entitie.ProductCountEntitie
import de.idealo.productapi.entitie.ProductEntitie
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.PersistenceException
import org.hibernate.CacheMode
import org.hibernate.annotations.QueryHints
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    enum class OrderBy {
        PRICE, QUANTITY
    }

    enum class OrderDirection {
        ASC, DESC
    }

    fun getAllProducts(
        site: String,
        limit: Int?,
        offset: Int?,
        filterTitle: String?,
        orderBy: OrderBy? = OrderBy.PRICE,
        orderDirection : OrderDirection? = OrderDirection.ASC
    ): List<ProductEntitie> {
        val orderByCheck = orderBy ?: OrderBy.PRICE
        val orderDirectionCheck = orderDirection ?: OrderDirection.ASC

        val sql = StringBuilder(
            "SELECT * FROM products WHERE site=:site"
        )

        filterTitle?.let {
            sql.append(" AND title LIKE :filterTitle")
        }

        sql.append(" ORDER BY $orderByCheck $orderDirectionCheck")
        sql.append(";")

        val query = entityManager.createNativeQuery(sql.toString(), ProductEntitie::class.java)

        query.setParameter("site", site)

        filterTitle?.let {
            query.setParameter("filterTitle", it)
        }

        if (limit != null && offset != null) {
            query.firstResult = offset
            query.maxResults = limit
        }

        return query.resultList as List<ProductEntitie>
    }

    fun getProductTotalCount(site: String) : Int? {
        val sql = "SELECT COUNT(DISTINCT id) AS count FROM products WHERE site=:site".trimMargin()
        val query = entityManager.createNativeQuery(sql, ProductCountEntitie::class.java)
        query.setParameter("site", site)

        @Suppress("UNCHECKED_CAST")
        val result = query.resultList as List<ProductCountEntitie?>

        return if (result.isNotEmpty()) result[0]?.count else null
    }

    fun checkProductExists(site: String, title: String): Boolean
    {
        val sql = "SELECT COUNT(DISTINCT id) AS count FROM products WHERE site=:site AND title=:title".trimMargin()
        val query = entityManager.createNativeQuery(sql, ProductCountEntitie::class.java).setHint(QueryHints.CACHE_MODE, CacheMode.IGNORE)
        query.setParameter("site", site)
        query.setParameter("title", title)

        @Suppress("UNCHECKED_CAST")
        val result = query.resultList as List<ProductCountEntitie?>

        return if (result.isNotEmpty()) (result[0]?.count ?: 0) > 0 else false
    }

    /**
     * @param product you want to save into db
     * @return information about success
     */
    @Transactional
    fun addNewProduct(product: ProductEntitie) : Boolean {
        val sql = """
            INSERT INTO products 
            (
                title, brand, price, quantity, description, shop_url, site
            )
            VALUES (
                :title, :brand, :price, :quantity, :description, :shopUrl, :site
            )
        """.trimIndent()

        try {
            entityManager.createNativeQuery(sql)
                .setParameter("title", product.title)
                .setParameter("brand", product.brand)
                .setParameter("price", product.price)
                .setParameter("quantity", product.quantity)
                .setParameter("description", product.description)
                .setParameter("shopUrl", product.shopUrl)
                .setParameter("site", product.site)
                .executeUpdate()
        } catch (e : PersistenceException) {
            return false
        }

        return true
    }
}