package com.example;

import com.example.listeners.impl.ListenerZoo;
import com.example.listeners.impl.ListenerAnimal;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class RabbitmqConsumerApplication {
	@Value("${queue.zoo.name}")
	private String qZoo;

	@Value("${queue.animal.name}")
	private String qAnimal;

	@Value("${spring.rabbitmq.host}")
	private String brokerUrl;

	@Value("${topic.exchange.name}")
	private String topicName;

	@Value("${spring.rabbitmq.username}")
	private String user;

	@Value("${spring.rabbitmq.password}")
	private String pwd;


	private static final String LISTENER_METHOD = "receiveMessage";

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqConsumerApplication.class, args);
	}

	@Bean(name ="queueZoo")
	Queue queueZoo() {
		return new Queue(qZoo, true);
	}

	@Bean(name="exchangeZoo")
	TopicExchange exchangeZoo() {
		return new TopicExchange(topicName);
	}

	@Bean(name="bindingZoo")
	Binding bindingCustomer(Queue queueZoo, TopicExchange exchangeZoo) {
		return BindingBuilder.bind(queueZoo).to(exchangeZoo).with(qZoo);
	}

	@Bean(name="queueAnimal")
	Queue queueAnimal() {
		return new Queue(qAnimal, true);
	}

	@Bean(name="exchangeAnimal")
	TopicExchange exchangeAnimal() {
		return new TopicExchange(topicName);
	}

	@Bean(name="bindingAnimal")
	Binding bindingShop(Queue queueAnimal, TopicExchange exchangeAnimal) {
		return BindingBuilder.bind(queueAnimal).to(exchangeAnimal).with(qAnimal);
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(brokerUrl);
		connectionFactory.setUsername(user);
		connectionFactory.setPassword(pwd);

		return connectionFactory;
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean(name="containerZoo")
	SimpleMessageListenerContainer containerZoo(ConnectionFactory connectionFactory,
													 MessageListenerAdapter listenerAdapterZoo) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setMessageConverter(jsonMessageConverter());
		container.setQueueNames(qZoo);
		container.setMessageListener(listenerAdapterZoo);
		return container;
	}

	@Bean(name="listenerAdapterZoo")
	public MessageListenerAdapter listenerAdapterZoo(ListenerZoo receiver) {
		MessageListenerAdapter msgAdapter = new MessageListenerAdapter(receiver);
		msgAdapter.setMessageConverter(jsonMessageConverter());
		msgAdapter.setDefaultListenerMethod(LISTENER_METHOD);

		return msgAdapter;
	}

	@Bean(name="containerAnimal")
	SimpleMessageListenerContainer containerAnimal(ConnectionFactory connectionFactory,
													 MessageListenerAdapter listenerAdapterAnimal) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setMessageConverter(jsonMessageConverter());
		container.setQueueNames(qAnimal);
		container.setMessageListener(listenerAdapterAnimal);
		return container;
	}

	@Bean(name="listenerAdapterAnimal")
	public MessageListenerAdapter listenerAdapterAnimal(ListenerAnimal receiver) {
		MessageListenerAdapter msgAdapter = new MessageListenerAdapter(receiver);
		msgAdapter.setMessageConverter(jsonMessageConverter());
		msgAdapter.setDefaultListenerMethod(LISTENER_METHOD);

		return msgAdapter;
	}
}
