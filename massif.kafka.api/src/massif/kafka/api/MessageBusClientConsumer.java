package massif.kafka.api;

import massif.kafka.api.listener.MessageBusConsumerListener;

public interface MessageBusClientConsumer<T> extends MessageBusClient {
	
	/**
	 * Set the consumer listener to be notified when a message was consumed
	 * @param clientListener			The listener you want to add
	 */
	void setMessageReceivedListener(MessageBusConsumerListener<T> clientListener);
	
}
