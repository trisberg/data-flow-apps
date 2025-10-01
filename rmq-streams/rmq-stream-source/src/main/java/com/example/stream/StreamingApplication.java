package com.example.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.config.ProducerMessageHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.outbound.RabbitStreamMessageHandler;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class StreamingApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamingApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(
        name = "spring.cloud.stream.rabbit.bindings.output.producer.producer-type",
        havingValue = "stream-async"
    )
	ProducerMessageHandlerCustomizer<MessageHandler> rabbitStreamMessageHandlerCustomizer() {
		return (handler, destinationName) -> ((RabbitStreamMessageHandler) handler).setHeadersMappedLast(true);
	}
}
