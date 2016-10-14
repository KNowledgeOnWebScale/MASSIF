package massif.kafka.controller.threading.model;

import massif.kafka.controller.threading.listener.model.ConsumerListenerCallback;

public interface ClientConsumer<K, V> extends Client {
	
	/**
	 * Get the callback client
	 * @return			The callback we can use to 
	 */
	ConsumerListenerCallback<K, V> getClientCallback();

}
