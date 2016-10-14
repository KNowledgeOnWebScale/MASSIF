package massif.kafka.api;

public class KafkaBindings {

	/**
	 * Prefix used to communicate semantic data
	 */
	public static final String KAFKA_MESSAGE_BUS_PREFIX = "http://kafka.service.owl#";
	
	/**
	 * Key used for service pid of the kafka component
	 */
	public static final String KAFKA_SERVICE_PID = "service.pid";
	
	/**
	 * Key used for factory pid of the kafka component
	 */
	public static final String KAFKA_FACTORY_PID = "service.factoryPid";
	
}
