package de.idealo.productapi

import org.apache.kafka.clients.admin.AdminClientConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import javax.sql.DataSource

@SpringBootApplication
class ProductapiApplication

fun main(args: Array<String>) {
	runApplication<ProductapiApplication>(*args)
}

@Bean
@Primary
@ConfigurationProperties(prefix = "spring.datasource")
fun primaryDataSource(): DataSource? {
	return DataSourceBuilder.create().build()
}

@Bean
fun admin() = KafkaAdmin(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092"))

@Bean
fun productApiKafkaTopic() =
	TopicBuilder.name("ProductApi")
		.partitions(10)
		.replicas(3)
		.compact()
		.build()

