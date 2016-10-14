package massif.kafka.api.listener;

public interface MessageBusConsumerListener<T> {

	/**
	 * Function used when a message was consumed
	 * @param key				The key of the message
	 * @param value				The value of the message
	 */
	void messageReceived(String key, final T value);
	
	/**
	 * Close the connection to the kafka cluster and clean
	 */
	void close();
	
}
