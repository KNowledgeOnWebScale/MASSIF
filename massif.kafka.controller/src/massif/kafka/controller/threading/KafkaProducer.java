package massif.kafka.controller.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import massif.kafka.api.MessageBusClientProducer;
import massif.kafka.controller.model.MessagebusClientConfigChanged;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducer<T> extends KafkaClient implements MessageBusClientProducer<T>, MessagebusClientConfigChanged {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * The kafka producer
	 */
	private org.apache.kafka.clients.producer.KafkaProducer<String, T> client;
	
	/**
	 * The list of topics subscribed to
	 */
	private List<String> topics;
	
	// Constructor
	public KafkaProducer(String name) {
		super(name);
	}
	
	// Constructor
	public KafkaProducer(String name, String kafkahost) {
		super(name, kafkahost);
	}

	@Override
	protected Properties setupDefaultProperties() {
		Properties props = new Properties();
	    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    //props.put(ProducerConfig.CLIENT_ID_CONFIG, "MASSIFKafkaClientProducer");
	    props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DefaultPartitioner.class);
	    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG , StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , StringSerializer.class);
	    
	    return props;
	}
	
	@Override
	protected Properties setupKafkahost(String cluster) {
		Properties props = new Properties();
	    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster);
	    
	    return props;
	}
	
	@Override
	public void updateKafkaClient(String host) {
		logger.info("Updating kafka host " + getName() + " to " + host);
		
		setProperties(setupKafkahost(host));
		
		// Save the topics
		List<String> topics = this.topics;
		
		// Reconnect the kafka host
		close();
		connect(topics);
	}
	
	/**
	 * Set the value serializer to be used when sending and receiving message
	 * @param clasz				The full class path and name of the serializer
	 */
	public void setValueSerializer(Class<?> serializer) {
		Properties properties = new Properties();
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);
		
		setProperties(properties);
	}

	@Override
	public boolean connect(List<String> topics) {
		// The topics we want to send to
		this.topics = topics;
		
		// Connect to the kafka cluster
		return connect();
	}
	
	/**
	 * Connects to a kafka cluster with no topics defined
	 * @return				True if success
	 */
	public boolean connect() {
		// Initiate the topics if empty
		if (topics == null) {
			topics = new ArrayList<String>();
		}
		
		logger.info("Connecting producer: " + topics.toString());
				
		try {
			// Connect to the kafka cluster
			client = new org.apache.kafka.clients.producer.KafkaProducer<>(getProperties());
		} catch (KafkaException e) {
			logger.warn("Could not create a kafka client");
		}
		
		return true;
	}

	@Override
	public void close() {
		// Close the client
		if (client != null) {
			client.close();
			client = null;
		}
		
		// Clear the topics
		if (this.topics != null) {
			this.topics.clear();
		}
	}	

	@Override
	public boolean sendMessage(T message) {
		if (this.topics != null) {
			for (String topic : topics) {
				sendMessage(message, topic);
			}
			
			return true;
		}
		
		// No topics defined
		logger.error("There were no topics defined in the Producer");
		
		return false;
	}
	
	@Override
	public boolean sendMessage(T message, List<String> topics) {
		if (topics != null) {
			for (String topic : topics) {
				sendMessage(message, topic);
			}
			
			return true;
		}
		
		// No topics defined
		logger.error("There were no topics defined in the Producer");
		
		return false;
	}

	@Override
	public boolean sendMessage(T message, String topic) {
		if (topic != null) {
			logger.info("Sending message to topic " + topic);
			
			// Create a record
			ProducerRecord<String, T> record = new ProducerRecord<String, T>(topic, message);
			
			// Send the record to the kafka cluster
			this.client.send(record);
			
			return true;
		}
		
		// No topic
		logger.error("There was no topic defined to send to");
		
		return false;
	}
	
}
