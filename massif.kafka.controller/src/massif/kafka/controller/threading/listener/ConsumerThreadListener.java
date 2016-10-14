package massif.kafka.controller.threading.listener;

import massif.kafka.controller.threading.model.ClientConsumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

public class ConsumerThreadListener<K, V> extends Thread {

	/**
	 * The kafka client that is connected to the cluster
	 */
	private KafkaConsumer<K, V> client;
	
	/**
	 * The consumer that was created
	 */
	private ClientConsumer<K, V> consumer;
	
	/**
	 * Boolean if the consumer is listening
	 */
	private boolean listening;
	
	// Constructor
	public ConsumerThreadListener(KafkaConsumer<K, V> client, ClientConsumer<K, V> consumer) {
		// Set the name of the thread
		super(consumer.getName());
		
		// Set the consumer that was created by the controller
		this.consumer = consumer;
		
		// Set the client we want to listen on
		this.listening = true;
		this.client = client;
	}
	
	@Override
	public void run() {
		try {
			// Loop until we die
			while (listening) {
				// Fetch records {
				ConsumerRecords<K, V> records = client.poll(50);
				
				// Handle the records
				for (ConsumerRecord<K, V> record : records) {
					// Send message to listener
					consumer.getClientCallback().messageReceived(record.key(), record.value());
				}
			}
		} catch (WakeupException e) {
			// Ignore exception if closing
			if (listening)
				throw e;
		} finally {
			// Close the consumer
			this.client.close();
		}
	}
	
	/**
	 * Stop listening for consumer records
	 */
	public void stopListening() {
		this.listening = false;
		
		// Interrupt the client
		this.client.wakeup();
	}

}
