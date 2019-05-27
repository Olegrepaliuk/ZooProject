package com.example;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@RefreshScope
@Configuration
public class EurekaClientLab2Application {
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

	public static void main(String[] args) {
		SpringApplication.run(EurekaClientLab2Application.class, args);
	}

    @Bean(name ="queueZoo")
    public Queue queueZoo() {
        return new Queue(qZoo, true);
    }

    @Bean(name="exchangeZoo")
    public TopicExchange exchangeZoo() {
        return new TopicExchange(topicName);
    }

    @Bean(name="bindingZoo")
    public Binding bindingCustomer(Queue queueZoo, TopicExchange exchangeZoo) {
        return BindingBuilder.bind(queueZoo).to(exchangeZoo).with(qZoo);
    }

    @Bean(name="queueAnimal")
    public Queue queueAnimal() {
        return new Queue(qAnimal, true);
    }

    @Bean(name="exchangeAnimal")
    public TopicExchange exchangeAnimal() {
        return new TopicExchange(topicName);
    }

    @Bean(name="bindingAnimal")
    public Binding bindingShop(Queue queueAnimal, TopicExchange exchangeAnimal) {
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

    @Bean(name="rabbitTemplateZoo")
    @Primary
    public RabbitTemplate rabbitTemplateZoo() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setRoutingKey(qZoo);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean(name="rabbitTemplateAnimal")
    public RabbitTemplate rabbitTemplateAnimal() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setRoutingKey(qAnimal);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
