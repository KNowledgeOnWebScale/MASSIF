package massif.kafka.controller.threading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.listener.MessageBusConsumerListener;
import massif.kafka.controller.model.MessagebusClientConfigChanged;
import massif.kafka.controller.threading.listener.ConsumerThreadListener;
import massif.kafka.controller.threading.listener.model.ConsumerListenerCallback;
import massif.kafka.controller.threading.model.ClientConsumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RangeAssignor;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumer<T> extends KafkaClient implements MessageBusClientConsumer<T>, MessageBusConsumerListener<T>, ClientConsumer<String, T>, MessagebusClientConfigChanged {
	
	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * The kafka consumer
	 */
	private org.apache.kafka.clients.consumer.KafkaConsumer<String, T> client;
	
	/**
	 * The kafka consumer that listens for messages
	 */
	private ConsumerThreadListener<String, T> clientListener;
	
	/**
	 * Defines a callback for the consumer
	 */
	private ConsumerListenerCallback<String, T> consumerCallback;
	
	/**
	 * Defines the listener for this client to be notified if a message was received
	 */
	private MessageBusConsumerListener<T> clientConsumerListener;
	
	/**
	 * Defines the current topics the consumer is listening to
	 */
	private List<String> currentConsumingTopics;

	// Constructor
	public KafkaConsumer(String name) {
		super(name);
		
		currentConsumingTopics = new ArrayList<String>();
	}
	
	// Constructor
	public KafkaConsumer(String name, String host) {
		super(name, host);
		
		currentConsumingTopics = new ArrayList<String>();
	}
	
	@Override
	protected Properties setupDefaultProperties() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "massif.default");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "50");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, Collections.singletonList(RangeAssignor.class));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
				
	    return props;
	}

	@Override
	protected Properties setupKafkahost(String host) {
		Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
	    
	    return props;
	}
	
	@Override
	public void updateKafkaClient(String host) {
		logger.info("Updating kafka host " + getName() + " to " + host);
		
		setProperties(setupKafkahost(host));
				
		// Save the listener
		MessageBusConsumerListener<T> currentListener = this.clientConsumerListener; 
		
		// Reconnect the client
		close();
		connect(currentConsumingTopics);
		setMessageReceivedListener(currentListener);
	}
	
	/**
	 * Set the deserializer used to handle the imcoming messages
	 * @param deserializer			Class used to deserialize
	 */
	public void setValueDeserializer(Class<?> deserializer) {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
		
		setProperties(properties);
	}
	
	@Override
	public boolean connect(List<String> topics) {
		// Initiate the topics if empty
		if (topics == null) {
			topics = new ArrayList<String>();
		}
		
		logger.info("Connecting consumer: " + topics.toString());
		
		// Define a default listener
		clientConsumerListener = this;
		
		// Define a callback for the consumer
		consumerCallback = new ConsumerListenerCallback<String, T>() {

			@Override
			public void messageReceived(String key, T value) {
				clientConsumerListener.messageReceived(key, value);
			}
			
		};
		
		try {
			client = new org.apache.kafka.clients.consumer.KafkaConsumer<>(getProperties());
		} catch (KafkaException e) {
			logger.warn("Could not create a kafka client");
		}
		
		// Subscribe the client
		currentConsumingTopics = topics;
		client.subscribe(topics);
		
		// Stop the listener if 1 already exists
		if (clientListener != null) {
			clientListener.stopListening();
		}
		
		// Start listening for records
		clientListener = new ConsumerThreadListener<>(client, this);
		clientListener.start();
		
		return true;
	}

	@Override
	public void close() {
		if (client != null) {
			// Stop listening
			if (clientListener != null) {
				clientListener.stopListening();
			}
			
			client = null;
		}
	}
		
	@Override
	public void setMessageReceivedListener(MessageBusConsumerListener<T> clientConsumer) {
		this.clientConsumerListener = clientConsumer;
	}
	
	@Override
	public ConsumerListenerCallback<String, T> getClientCallback() {
		return this.consumerCallback;
	}
	
	@Override
	public synchronized void messageReceived(String key, T value) {
		logger.info("Default printout :: Received record :: Key: " + key + " >> Value: " + value);
	}

}
