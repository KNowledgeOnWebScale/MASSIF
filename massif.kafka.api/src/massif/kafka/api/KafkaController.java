package massif.kafka.api;

import java.util.List;

public interface KafkaController {

	/**
	 * Create a new {@link KafkaProducer}
	 * @param <T>			The type you want to send
	 * @param name			The name of the client to be identified on the bus
	 * @param serializer	Class used to serialize the messages
	 * @return				A messagebus client that can control the producer
	 */
	<T> MessageBusClientProducer<T> createKafkaProducer(String name, Class<?> serializer);
	
	/**
	 * Create a new {@link KafkaProducer}
	 * @param <T>			The type you want to send
	 * @param name			The name of the client to be identified on the bus
	 * @param topics		A list of predefined topics to send to
	 * @param serializer	Class used to serialize the messages
	 * @return				A messagebus client that can control the producer
	 */
	<T> MessageBusClientProducer<T> createKafkaProducer(String name, Class<?> serializer, List<String> topics);
	
	/**
	 * Create a new {@link KafkaConsumer}
	 * @param <T>			The type of message you want to receive
	 * @param name			The name of the client to be identified on the bus
	 * @param topics		A list of topics the consumer should be listening to
	 * @param deserializer	Class used to deserialize the messages
	 * @return				A messagebus client that control the comsumer
	 */
	<T> MessageBusClientConsumer<T> createKafkaConsumer(String name, Class<?> deserializer, List<String> topics);
	
}
