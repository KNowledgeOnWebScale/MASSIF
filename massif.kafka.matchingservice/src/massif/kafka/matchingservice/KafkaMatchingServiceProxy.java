package massif.kafka.matchingservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import massif.contextadapter.api.ContextAdapter;
import massif.journal.api.JournalService;
import massif.kafka.api.KafkaController;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.MessageBusClientProducer;
import massif.kafka.api.listener.MessageBusConsumerListener;
import massif.kafka.controller.bindings.MessageBusBindings;
import massif.kafka.controller.bindings.SerializerBindings;
import massif.kafka.matchingservice.listeners.MessagebusConsumerListenerImpl;
import massif.matchingservice.AbstractMatchingService;
import massif.matchingservice.api.MatchingService;
import massif.watchdog.api.WatchdogEventService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate=true)
public class KafkaMatchingServiceProxy extends AbstractMatchingService implements MatchingService {
	
	// Queue for registering adapters
	private List<ContextAdapter> adapterQueue;
	
	// Mapping adapters to kafka clients
	private Map<ContextAdapter, MessageBusConsumerListener<Map<String, Object>>> adapterKafkaMapping;
	private Map<ContextAdapter, String> adapterPropertyMapping;
	
	// Kafka controller to create clients
	private KafkaController messagebusController;
	
	// Kafka producer to communicate to the contextadapters
	private MessageBusClientProducer<Map<String, Object>> messagebusProducer;
	
	// State of the component
	private boolean active;
	
	/**
	 * Constructor
	 */
	public KafkaMatchingServiceProxy() {
		adapterQueue = new ArrayList<ContextAdapter>();
		adapterKafkaMapping = new HashMap<ContextAdapter, MessageBusConsumerListener<Map<String, Object>>>();
		adapterPropertyMapping = new HashMap<ContextAdapter, String>();
		
		active = false;
	}

	@Activate
	public void start() {
		// Create a producer to communicate with the contextadapters
		messagebusProducer = messagebusController.createKafkaProducer(MessageBusBindings.GROUP_NAME_MESSAGE_BUS_MATCHINGSERVICES, SerializerBindings.SERIALIZER_MAP);
		
		// Configure consumers
		for (ContextAdapter adapter : adapterQueue) {
			createAdapterMessageBusConsumer(adapter);
		}
		
		// Component is ready
		active = true;
		
		getLogger().info("Kafka MatchingService Proxy started");
	}
	
	@Override
	@Reference(unbind="unbindWatchdog", cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void bindWatchdog(WatchdogEventService watchdog) {
		getLogger().info("jeroen watchdog");
		setWatchdogEventService(watchdog);
	}

	@Override
	public void unbindWatchdog(WatchdogEventService watchdog) {
		setWatchdogEventService(null);
	}

	@Override
	@Reference(unbind="unbindJournalService", cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void bindJournalService(JournalService js) {
		setJournalService(js);
	}

	@Override
	public void unbindJournalService(JournalService js) {
		setJournalService(null);
	}
	
	@Reference(unbind="unbindContextAdapter", cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void bindContextAdapter(ContextAdapter ca, Map<String, Object> properties) {
		getLogger().info("New context adapter > " + properties);
		
		if (properties.containsKey("tag") && !adapterKafkaMapping.containsKey(ca) && !adapterQueue.contains(ca)) {
			// Map the key to the adapter
			adapterPropertyMapping.put(ca, properties.get("tag").toString());
			
			// Check if the service is active
			if (active) {
				createAdapterMessageBusConsumer(ca);
			} else {
				// Add to the queue
				adapterQueue.add(ca);
			}
		} else {
			getLogger().error("Could not bind adapter: " + ca);
		}
	}

	public void unbindContextAdapter(ContextAdapter ca) {
		// Find the adapter and close the client
		if (adapterKafkaMapping.containsKey(ca)) {
			MessageBusConsumerListener<Map<String, Object>> mc = adapterKafkaMapping.remove(ca);
			mc.close();
		}
	}
	
	@Reference(unbind="unbindKafkaController", cardinality=ReferenceCardinality.MANDATORY)
	public void bindKafkaController(KafkaController controller) {
		messagebusController = controller;
	}
	
	public void unbindKafkaController(KafkaController controller) {
		messagebusController = null;
	}
	
	@Override
	public void transmitOut(String tag, Map<String, Object> metaFragment) {
		// Check if requirements are met
		if (messagebusProducer != null && tag != null) {
			// Send to the bus
			messagebusProducer.sendMessage(metaFragment, tag);
		} else {
			getLogger().error("No message bus producer active or tag is empty");
		}
	}
	
	/**
	 * Create a consumer for an adapter
	 * @param adapter				The contextadapter you want to bind
	 */
	private void createAdapterMessageBusConsumer(ContextAdapter adapter) {
		// Create a kafka consumer handler for adapters
		MessageBusClientConsumer<Map<String, Object>> consumer = messagebusController.createKafkaConsumer(
				MessageBusBindings.GROUP_NAME_MESSAGE_BUS_ADAPTERS, 
				SerializerBindings.DESERIALIZER_MAP, 
				Collections.singletonList(adapterPropertyMapping.get(adapter))
		);
		
		// Create a consumer listener
		MessageBusConsumerListener<Map<String, Object>> consumerListener = new MessagebusConsumerListenerImpl(adapter, consumer);		
		
		// Add the adapter to our mapping
		adapterKafkaMapping.put(adapter, consumerListener);
	}

}
