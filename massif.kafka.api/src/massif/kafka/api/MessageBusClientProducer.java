package massif.kafka.api;

import java.util.List;

public interface MessageBusClientProducer<T> extends MessageBusClient {
	
	/**
	 * Send a message to all the subscribed topics on the kafka cluster
	 * @param message				The message you want to send
	 * @return					True if success
	 */
	boolean sendMessage(T message);
	
	/**
	 * Send a message to a specific topic on the kafka cluster
	 * @param message				The message you want to send
	 * @param topic					The topic you want to send to
	 * @return					True if success
	 */
	boolean sendMessage(T message, String topic);
	
	/**
	 * Send a message to a specific list of topics on the kafka cluster
	 * @param message				The message you want to send
	 * @param topics				List of topics you want to send to
	 * @return					True if success
	 */
	boolean sendMessage(T message, List<String> topics);

}
