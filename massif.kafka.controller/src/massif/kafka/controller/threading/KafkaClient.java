package massif.kafka.controller.threading;

import java.util.List;
import java.util.Properties;

import massif.kafka.controller.threading.model.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KafkaClient implements Client {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * The name of the client
	 */
	private String name;
	
	/**
	 * The properties of the kafka client
	 */
	private Properties properties;
	
	/**
	 * Define default properties for the kafka client
	 * @return			The default properties
	 */
	protected abstract Properties setupDefaultProperties();
	
	/**
	 * Set the kafka server
	 * @param server			The kafka host
	 * @return					The updated properties
	 */
	protected abstract Properties setupKafkahost(String server);
			
	/**
	 * Connect to the given list of kafka topics
	 * @param topic				A list of topic to subscribe to
	 * @return					True if success
	 */
	public abstract boolean connect(List<String> topic);
			
	// Constructor
	public KafkaClient(String name) {
		logger.info("Starting a client for " + name);
		
		this.name = name;
		this.properties = new Properties();
		
		// Define default properties
		setProperties(setupDefaultProperties());
	}
	
	// Constructor
	public KafkaClient(String name, String kafkahost) {
		this(name);
		
		// Add kafka host
		setProperties(setupKafkahost(kafkahost));
	}
		
	/**
	 * Setter properties
	 * @param properties
	 */
	protected void setProperties(Properties properties) {
		// Add and/or replace properties
		this.properties.putAll(properties);
	}
	
	/**
	 * Getter properties
	 * @return
	 */
	protected Properties getProperties() {
		return this.properties;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
		
}
