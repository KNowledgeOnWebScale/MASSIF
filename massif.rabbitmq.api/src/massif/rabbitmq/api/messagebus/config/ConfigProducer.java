package massif.rabbitmq.api.messagebus.config;

import java.util.Properties;

import massif.rabbitmq.api.messagebus.MessageBusClient;

public interface ConfigProducer {

	MessageBusClient addProducer(String exchange, Properties properties);
	
}
