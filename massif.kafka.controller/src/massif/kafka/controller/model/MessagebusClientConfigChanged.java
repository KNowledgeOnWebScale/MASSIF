package massif.kafka.controller.model;

public interface MessagebusClientConfigChanged {

	/**
	 * Update the host of the client
	 * @param host				The new host that where we need to connect
	 */
	void updateKafkaClient(String host);
	
}
