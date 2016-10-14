package massif.kafka.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import massif.kafka.api.KafkaController;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.MessageBusClientProducer;
import massif.kafka.controller.model.MessagebusClientConfigChanged;
import massif.kafka.controller.threading.KafkaConsumer;
import massif.kafka.controller.threading.KafkaProducer;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true)
public class KafkaControllerImpl implements KafkaController {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** Ip or Hostname Kafka is running on */
	private String kafkaHost;

	/** Ip or Hostname zookeeper is running on */
	private String zookeeperHost;
	
	/**
	 * The created kafka clients
	 */
	private List<MessagebusClientConfigChanged> kafkaConsumerClients;
	private List<MessagebusClientConfigChanged> kafkaProducerClients;
	
	// Constructor
	public KafkaControllerImpl() {
		logger.info("Kafka controller constructor called");
		
		// Initiate the clients list
		kafkaConsumerClients = new ArrayList<MessagebusClientConfigChanged>();
		kafkaProducerClients = new ArrayList<MessagebusClientConfigChanged>();
	}
	
	@Activate
	public void start(BundleContext context) {
		// Start the bundle with the properties
		readProperties(loadProperties(context, "properties.properties"));	
		
		// Tell the user we started the bundle
		logger.info(context.getBundle().getSymbolicName() + " started");
	}
	
	@Modified
	public void update(Map<Object, Object> properties) {
		Properties p = new Properties();
		// Transform to a Properties object
		for (Entry<Object, Object> entry : properties.entrySet()) {
			p.setProperty(entry.getKey().toString(), entry.getValue().toString());
		}
		
		// Read the properties
		readProperties(p);
		
		// Update all clients 
		for (MessagebusClientConfigChanged mbclient : kafkaProducerClients) {
			mbclient.updateKafkaClient(kafkaHost);
		}
		
		for (MessagebusClientConfigChanged mbclient : kafkaConsumerClients) {
			mbclient.updateKafkaClient(zookeeperHost);
		}
	}
	
	/**
	 * Prepare the bundle for boot up
	 * @param context			The context of the bundle
	 * @param properties		The properties of the bundle
	 */
	protected void readProperties(Properties properties) {
		// The hostname of the kafka host
		kafkaHost = properties.getProperty("kafka.host");
		
		// The hostname of the zookeeper host
		zookeeperHost = properties.getProperty("zookeeper.host");
	}
	
	/**
	 * Read the properties.properties file
	 * @param context			The context of the bundle
	 * @param path				The path of the file
	 */
	protected Properties loadProperties(BundleContext context, String path) {
		URL url = context.getBundle().getEntry(path);
		InputStream in;
		
		// The properties file that will be returned
		Properties properties = new Properties();
		
		try {
			in = url.openStream();
			properties.load(in);
			in.close();
		} catch (IOException e) {
			logger.error("Could not load property from location <" + url.toString() + ">.", e);
		}
		return properties;
	}
	
	@Override
	public <T> MessageBusClientProducer<T> createKafkaProducer(String name, Class<?> serializer) {
		// Create and connect
		KafkaProducer<T> producer = new KafkaProducer<T>(name, kafkaHost);
		producer.setValueSerializer(serializer);
		producer.connect();
		
		// Save the client
		kafkaProducerClients.add(producer);
		
		return producer;
	}
	
	@Override
	public <T> MessageBusClientProducer<T> createKafkaProducer(String name, Class<?> serializer, List<String> topics) {
		// topics should exist
		if (topics == null) {
			topics = new ArrayList<>();
		}
		
		// Create and connect
		KafkaProducer<T> producer = new KafkaProducer<T>(name, kafkaHost);
		producer.setValueSerializer(serializer);
		producer.connect(topics);
		
		// Save the client
		kafkaProducerClients.add(producer);
		
		return producer;
	}
	
	@Override
	public <T> MessageBusClientConsumer<T> createKafkaConsumer(String name, Class<?> deserializer, List<String> topics) {
		// topics should exist
		if (topics == null) {
			topics = new ArrayList<>();
		}
		
		// Create and connect
		KafkaConsumer<T> consumer = new KafkaConsumer<T>(name, zookeeperHost);
		consumer.setValueDeserializer(deserializer);
		consumer.connect(topics);
		
		// Save the client
		kafkaConsumerClients.add(consumer);
		
		return consumer;		
	}
	
}
