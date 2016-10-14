package massif.kafka.matchingservice;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import massif.kafka.api.KafkaController;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.listener.MessageBusConsumerListener;
import massif.kafka.controller.bindings.MessageBusBindings;
import massif.kafka.controller.bindings.SerializerBindings;
import massif.watchdog.api.WatchdogDummyComponent;
import massif.watchdog.api.WatchdogEventService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KafkaWatchdogServiceProxy implements MessageBusConsumerListener<Map<Object, Object>> {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// Worker pool for incoming messages
	private ExecutorService workerpool = Executors.newFixedThreadPool(1);
	
	// Kafka controller to create clients
	private KafkaController messagebusController;
	
	// Kafka message bus consumer to handle incoming watchdog events
	private MessageBusClientConsumer<Map<Object, Object>> messagebusConsumer;
	
	// MASSIF Watchdog service
	private WatchdogEventService watchdogService;	
	
	@Activate
	public void start() {
		// Create a kafka message bus client
		messagebusConsumer = messagebusController.createKafkaConsumer(
				MessageBusBindings.GROUP_NAME_MESSAGE_BUS_WATCHDOG, 
				SerializerBindings.DESERIALIZER_MAP, 
				Collections.singletonList(MessageBusBindings.MESSAGE_BUS_WATCHDOG)
		);
		
		messagebusConsumer.setMessageReceivedListener(this);
		
		logger.info("Watchdog Service Proxy Started");
	}
	
	@Reference(unbind="unbindWatchdog", cardinality=ReferenceCardinality.MANDATORY)
	public void bindWatchdog(WatchdogEventService watchdog) {
		watchdogService = watchdog;
	}
	
	public void unbindWatchdog(WatchdogEventService watchdog) {
		watchdogService = null;
	}
	
	@Reference(unbind="unbindKafkaController", cardinality=ReferenceCardinality.MANDATORY)
	public void bindKafkaController(KafkaController controller) {
		messagebusController = controller;
	}
	
	public void unbindKafkaController(KafkaController controller) {
		messagebusController = null;
	}

	@Override
	public void messageReceived(String key, final Map<Object, Object> value) {
		logger.info("Message received");
		
		workerpool.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.info(value.toString());

					String eventstatus = ((String) value.get("event")).toLowerCase();
					String packetid = (String) value.get("packetid");
					
					// Check status
					if (eventstatus.equals("start")) {
						// Event started
						watchdogService.eventStart(packetid);
					} else if (eventstatus.equals("running")) {
						// Event running
						watchdogService.eventRunning(new WatchdogDummyComponent(value.get("component").toString()), packetid);
					} else if (eventstatus.equals("ready")) {
						// Event ready
						watchdogService.eventReady(new WatchdogDummyComponent(value.get("component").toString()), packetid);
					}
				} catch (Exception e) {
					logger.error("Could not parse incoming message");
				}
			}
			
		});
	}

	@Override
	public void close() {
		if (messagebusConsumer != null) {
			messagebusConsumer.close();
		}
	}
	
	
	
}
