package massif.rabbitmq.api.messagebus;

public interface ConsumerListener {
	
	/**
	 * When a message was received on the exchange the bundle was registered
	 * @param message 						The object that was received
	 */
	void consumeMessage(byte[] message);
	
	public void addClient(MessageBusClient client);
	
}
