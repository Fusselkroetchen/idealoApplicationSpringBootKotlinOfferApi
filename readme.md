# Product API

First I have to mention, that I use title instead of name for the product. 
I think it fits the nature of a product better.

## Infos

You can find postmen profile under ./moreinfos/OfferApiTrial.postman_collection.json

#### API-Infos
Did not had the time to make endpoint documentation with org.springframework.restdocs. Here you have some informations:
Most requests are able to have GET and POST.
##### Param: filterTitle
The Parameter filterTitle is for finding product by name. You have all benefits of SQL Like statement to search for the title.
Use wildcards and other operators: https://www.w3schools.com/sql/sql_like.asp


## Ask Questions:

### • ... publishing API events to a data stream (e.g., Kafka, SNS, or similar).

This is implemented in the code. Just start localhost kafka server and create following topic:
ProductApi

https://kafka.apache.org/quickstart

### • ... how you would secure your API considering both authentication and authorization.

#### Authentication:

With following dependency's authentication for rest path are possible:

pom.xml

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-web</artifactId>
    </dependency>

Match Path's you want to secure with Authentication:

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .anyExchange().authenticated()
            .and().build()
    }

#### SQL-Injection

Here the most does the JPA Springboot Data library. Parameters become checked for SQL-Injections as long you dont paste them directly in the SQL-String and use the following example code:

    query.setParameter("site", site)

With a placeholder in the sql string:

    val sql = "SELECT * FROM products WHERE site=:site"

If you have other scenarios where you need to edit the SQL string directly, then you need to take care to protect against SQL injections yourself.

For example, with a variable ORDER BY, I had to specify the variable directly in the SQL string because this is not a parameter.
Here I have used an enum to have it safe against SQL injection so that no one can use other values than this.

Also, Spring Boot Request Mapping Regex definitions helps a lot with SQL-Injection.

#### SSO Proxy

SSO Proxy is another solution. Simply install SSO Proxy in the EKS cluster and route this rest api via it. The client then uses the URLs from the SSO proxy.
The client needs a key and a secret to obtain a bearer token from an SSO server so that it can access the REST API via the SSO proxy.

### • ... observability measures (logs & metrics)

use following dependency in pom.xml

    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <scope>runtime</scope>
    </dependency>

Configure prometheus and the actuator in application.properties.
This service provides metric data and health status at http://127.0.0.1:8080/actuator/* or, depending on the infrastructure in which it runs, at a different url.

The cloud infrastructure can grab these infos for grafana etc. Check also with intellij debug tool.

make your own metric with following dependency:

    <dependency>
      <groupId>de.idealo.common.spring-boot</groupId>
      <artifactId>idealo-spring-boot-starter-micrometer-prometheus</artifactId>
      <version>9.0.9</version>
    </dependency

more details read docs.

### ... the infrastructure needed to run your solution appropriately on AWS.

These Details are missing here because im still learning AWS.

I Build once a service like this which runs in Travel EKS-Cluster on AWS:
Here you can see Github-Action deployment:
https://github.com/idealo/travel-app-service-flight-deals/blob/master/.github/workflows/build-aws-ecr.yml

