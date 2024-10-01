package de.idealo.productapi.kafka

import de.idealo.productapi.entitie.ProductEntitie
import org.apache.tomcat.util.json.JSONParser
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
//@Slf4j for logging - could be used
@KafkaListener(id = "class-level", topics = ["ProductApi"])
class Listener {
    @KafkaHandler
    fun listenForNewOffer(newProduct: String) {
        val newProductObj = JSONParser(newProduct).parse() as ProductEntitie?

        if (newProductObj != null) {
            print("message from Kafka:" + (newProductObj.title ?: "no title"))
        }
    }
}