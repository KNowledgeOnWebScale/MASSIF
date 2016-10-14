package massif.kafka.api;

public interface MessageBusClient {

	/**
	 * Disconnect from the kafka cluster and clean up
	 */
	void close();
}
