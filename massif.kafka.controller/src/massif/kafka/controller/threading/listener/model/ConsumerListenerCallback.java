package massif.kafka.controller.threading.listener.model;

public interface ConsumerListenerCallback<K, V> {

	/**
	 * Listener when a message was received
	 * @param key			The key of the record
	 * @param value			The value of the record
	 */
	void messageReceived(K key, V value);
	
}
