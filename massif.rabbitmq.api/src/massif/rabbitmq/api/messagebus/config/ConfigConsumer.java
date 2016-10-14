package massif.rabbitmq.api.messagebus.config;

import java.util.Properties;

import massif.rabbitmq.api.messagebus.ConsumerListener;
import massif.rabbitmq.api.messagebus.MessageBusClient;

public interface ConfigConsumer {

	MessageBusClient addConsumer(ConsumerListener listener, String exchange, String tag, Properties properties);
	
}
