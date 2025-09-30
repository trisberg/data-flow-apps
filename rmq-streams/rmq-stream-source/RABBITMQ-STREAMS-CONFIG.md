# RabbitMQ Streams Configuration Guide

This guide explains how to configure RabbitMQ streams publishing with Spring Cloud Stream.

## Key Configuration Areas

### 1. Basic Stream Configuration

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: foo-data  # Stream name
          producer:
            useNativeEncoding: true  # Better performance for streams
```

### 2. RabbitMQ Stream-Specific Settings

```yaml
spring:
  cloud:
    stream:
      rabbit:
        bindings:
          output:
            producer:
              stream:
                enabled: true                    # Enable stream mode
                batchSize: 100                   # Messages per batch
                batchBufferSize: 16384          # Batch buffer size in bytes
                compressionType: gzip           # Compression: none, gzip, snappy, lz4
                deduplicationEnabled: true      # Message deduplication
                routingKeyExpression: "headers['partitionKey']"  # Partitioning
                filterValueExpression: "headers['x-stream-filter-value']"  # Bloom filtering
```

### 3. Connection Configuration

```yaml
spring:
  cloud:
    stream:
      rabbit:
        binder:
          host: localhost
          port: 5552                    # Stream port (not 5672)
          username: guest
          password: guest
          virtualHost: /
          connectionTimeout: 30000
          requestedHeartbeat: 30
```

### 4. Performance Tuning

#### Batching for High Throughput
```yaml
spring:
  rabbitmq:
    stream:
      producer:
        batch-size: 100                     # Messages per batch
        batch-publishing-delay: PT100MS     # Max delay before sending batch
```

#### Connection Pooling
```yaml
spring:
  rabbitmq:
    stream:
      connection-pool:
        max-size: 10                        # Max connections in pool
        max-idle-time: PT30M               # Idle timeout
```

### 5. Message Filtering Support

#### Bloom Filters (Fast Chunk-Level Filtering)
```java
// In your producer function
Message<Foo> message = MessageBuilder
    .withPayload(fooData)
    .setHeader("x-stream-filter-value", "order.created")  // Filter value
    .build();
```

#### SQL Filter Expressions (Precise Message-Level Filtering)
Consumers can use SQL filters:
```yaml
# Consumer configuration example
spring:
  cloud:
    stream:
      rabbit:
        bindings:
          input:
            consumer:
              stream:
                sqlFilter: "subject = 'order.created' AND region IN ('AMER', 'EMEA')"
```

### 6. Partitioning for Scalability

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          producer:
            partitionKeyExpression: "headers['customerId']"
            partitionCount: 3
```

```java
// In your producer function
Message<Foo> message = MessageBuilder
    .withPayload(fooData)
    .setHeader("customerId", "customer-123")  // Partition key
    .build();
```

### 7. Error Handling and Reliability

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          producer:
            maxAttempts: 3
            backOffInitialInterval: 1000
            backOffMaxInterval: 10000
            backOffMultiplier: 2.0
      rabbit:
        bindings:
          output:
            producer:
              confirm: true                 # Publisher confirms
              confirmTimeout: 5000         # Confirm timeout
              mandatory: true              # Fail if unroutable
              deliveryMode: PERSISTENT     # Persistent messages
```

### 8. Stream Retention Policy

```yaml
spring:
  rabbitmq:
    stream:
      environment:
        max-length-bytes: 20GB         # Max stream size
        max-age: P7D                   # Retention period (7 days)
        stream-max-segment-size-bytes: 500MB  # Segment size
```

### 9. SSL/TLS Configuration

```yaml
spring:
  cloud:
    stream:
      rabbit:
        binder:
          sslProperties:
            enabled: true
            keyStore: /path/to/keystore.p12
            keyStorePassword: ${KEYSTORE_PASSWORD}
            keyStoreType: PKCS12
            trustStore: /path/to/truststore.p12
            trustStorePassword: ${TRUSTSTORE_PASSWORD}
            trustStoreType: PKCS12
            algorithm: TLSv1.2
            verifyHostname: true
```

### 10. Monitoring and Management

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,bindings
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.example.stream: DEBUG
    org.springframework.cloud.stream: DEBUG
    org.springframework.rabbit: DEBUG
```

## Best Practices

### 1. Use Native Encoding
Always set `useNativeEncoding: true` for better performance with streams.

### 2. Configure Appropriate Batch Sizes
- For high throughput: `batchSize: 100-1000`
- For low latency: `batchSize: 1-10`

### 3. Enable Compression
Use `compressionType: gzip` or `lz4` to reduce network usage.

### 4. Implement Proper Error Handling
Configure retry policies and publisher confirms for reliability.

### 5. Use Filtering for Selective Consumption
Implement Bloom filters and SQL filters to reduce network traffic.

### 6. Monitor Performance
Use Spring Boot Actuator and Prometheus metrics to monitor stream performance.

## Environment-Specific Configuration

### Development
```yaml
spring:
  profiles:
    active: development
  cloud:
    stream:
      rabbit:
        binder:
          host: localhost
          port: 5552
logging:
  level:
    com.example.stream: DEBUG
```

### Production
```yaml
spring:
  profiles:
    active: production
  cloud:
    stream:
      rabbit:
        binder:
          host: ${RABBITMQ_HOST}
          port: ${RABBITMQ_PORT:5552}
          username: ${RABBITMQ_USERNAME}
          password: ${RABBITMQ_PASSWORD}
          sslProperties:
            enabled: true
logging:
  level:
    root: WARN
    com.example.stream: INFO
```

## Testing Your Configuration

1. **Start RabbitMQ with Streams Plugin**:
   ```bash
   docker run -it --rm --name rabbitmq \
     -p 5672:5672 -p 5552:5552 -p 15672:15672 \
     rabbitmq:3.12-management
   
   # Enable streams plugin
   docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stream
   ```

2. **Build and Run**:
   ```bash
   ./mvnw clean package
   java -jar target/rmq-stream-source-0.0.1-SNAPSHOT.jar
   ```

3. **Monitor Stream**:
   - Visit RabbitMQ Management UI: http://localhost:15672
   - Check stream statistics and consumer details

## Troubleshooting

### Common Issues

1. **Connection Refused**: Check if RabbitMQ streams plugin is enabled and port 5552 is accessible.

2. **Authentication Failed**: Verify username/password and virtual host settings.

3. **Stream Not Created**: Ensure destination name is valid and user has permissions.

4. **Poor Performance**: Tune batch sizes, compression, and connection pool settings.

5. **Messages Not Received**: Check stream filtering expressions and consumer group settings.

### Debug Configuration

```yaml
logging:
  level:
    org.springframework.amqp: DEBUG
    org.springframework.rabbit: DEBUG
    com.rabbitmq.stream: DEBUG
```

This will provide detailed logging for troubleshooting connection and messaging issues.
