package massif.kafka.matchingservice.listeners;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import massif.contextadapter.api.ContextAdapter;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.listener.MessageBusConsumerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagebusConsumerListenerImpl implements MessageBusConsumerListener<Map<String, Object>> {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// Worker pool to handle incoming messages
	protected ExecutorService workerpool = Executors.newFixedThreadPool(1);

	// The adapter that was bound to the consumer
	private ContextAdapter adapter;
	
	// The consumer that was created to handle the contextadapter
	private MessageBusClientConsumer<Map<String, Object>> consumer;
	
	/**
	 * Constructor
	 */
	public MessagebusConsumerListenerImpl(ContextAdapter adapter, MessageBusClientConsumer<Map<String, Object>> consumer) {
		this.adapter = adapter;

		// Set this object as a listener
		this.consumer = consumer;
		this.consumer.setMessageReceivedListener(this);
	}
	
	@Override
	public void messageReceived(String key, final Map<String, Object> value) {
		logger.info("Received a message");
		
		workerpool.execute(new Runnable() {
			
			@Override
			public void run() {
				logger.info("Received map: " + value);
				
				// Adapters handle the incoming message
				adapter.transmitIn(value);
			}
			
		});
	}

	@Override
	public void close() {
		if (consumer != null) {
			consumer.close();
		}
	}
		
}
