package massif.kafka.scb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import massif.kafka.api.KafkaBindings;
import massif.kafka.api.KafkaController;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.MessageBusClientProducer;
import massif.kafka.api.listener.MessageBusConsumerListener;
import massif.kafka.controller.bindings.MessageBusBindings;
import massif.kafka.controller.bindings.SerializerBindings;
import massif.kafka.scb.listeners.MessagebusConsumerListenerImpl;
import massif.kafka.scb.util.ServiceInfo;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;
import massif.watchdog.api.WatchdogEventService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true, property="scb.type=kafka")
public class KafkaSCBProxy implements OWLSemanticCommunicationBus {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// MASSIF Kafka controller
	private KafkaController messagebusController;
	
	// Kafka producer to communicate to the bus and other services
	private MessageBusClientProducer<OWLOntology> messagebusProducer;
	
	// MASSIF Watchdog service
	private WatchdogEventService watchdogService;
	
	// Ontology services
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	
	private List<MCIService> services;
	private Map<MCIService, ServiceInfo> infoMapping;
	private Map<MCIService, MessageBusConsumerListener<OWLOntology>> serviceKafkaMapping;
	
	// State of the component
	private boolean active;
	
	/**
	 * Constructor
	 */
	public KafkaSCBProxy() {
		// Component is not ready
		active = false;
		
		// Prepare Ontology variables 
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		// Prepare service variables
		services = new ArrayList<MCIService>();
		infoMapping = new HashMap<MCIService, ServiceInfo>();
		serviceKafkaMapping = new HashMap<MCIService, MessageBusConsumerListener<OWLOntology>>();
	}
	
	@Activate
	public void start() {
		// Create a producer to communicate with the bus from the contextadapters
		messagebusProducer = messagebusController.createKafkaProducer(MessageBusBindings.GROUP_NAME_MESSAGE_BUS_ADAPTERS, SerializerBindings.SERIALIZER_OWLONTOLOGY);
		
		// Service is ready
		active = true;
		
		logger.info("SCB Proxy Started");
	}
	
	@Override
	public boolean publish(OWLMessage message, String packetID) {
		logger.info("PROXY PUBLISH TO SCB");
		
		OWLNamedIndividual event = message.getOWLMessage();
		OWLDataProperty packetidProperty = factory.getOWLDataProperty(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "PacketID"));
		OWLLiteral packetidLiteral = factory.getOWLLiteral(packetID);
		
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(packetidProperty, event, packetidLiteral);
		message.addAxiom(ax);
		
		// Send to watchdog. 
		if (watchdogService != null) {
			watchdogService.eventStart(packetID);
		}		
		
		try {
			// Send the message to the kafka cluster
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology();
			manager.addAxioms(ontology, message.getAxioms());
			messagebusProducer.sendMessage(ontology, MessageBusBindings.MESSAGE_BUS_MESSAGES);
		} catch (Exception e) {
			logger.error("Could not send filter ontology to the bus", e);
		}
		
		return true;
	}

	@Override
	public boolean subscribe(MCIService handler, OWLMessageFilter filter) {
		return false;
	}
	
	@Reference(unbind="unbindKafkaController", cardinality=ReferenceCardinality.MANDATORY)
	public void bindKafkaController(KafkaController controller) {
		messagebusController = controller;
	}
	
	public void unbindKafkaController(KafkaController controller) {
		messagebusController = null;
	}

	@Override
	@Reference(unbind="unbindMCIService", policy = ReferencePolicy.DYNAMIC, cardinality=ReferenceCardinality.MULTIPLE)
	public void bindMCIService(MCIService service, Map<String, Object> properties) {
		logger.info("Binding MCIService: " + service);
		
		// Register the bus on the service
		service.registerBus(this);
		
		ServiceInfo sInfo = new ServiceInfo(service, properties);
		if (!services.contains(sInfo)) {
			services.add(service);
			infoMapping.put(service, sInfo);
		}
		
		if (active) {
			createServiceMessageBusConsumer(sInfo);
		}
	}

	@Override
	public void unbindMCIService(MCIService service, Map<String, Object> properties) {
		if (services.contains(service)) {
			services.remove(service);
		}

		if (serviceKafkaMapping.containsKey(service)) {
			ServiceInfo serviceInfo = infoMapping.get(service);
			
			// Create Stop Message
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLIndividual test = factory.getOWLNamedIndividual(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "StoppedService"));
			OWLDataProperty dataProp = factory.getOWLDataProperty(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "StoppedServiceName"));
			
			String serviceName = serviceInfo.getProperties().get(KafkaBindings.KAFKA_SERVICE_PID).toString();
			if (serviceInfo.getProperties().containsKey(KafkaBindings.KAFKA_FACTORY_PID)) {
				serviceName = convertserviceName(serviceName, serviceInfo.getProperties().get(KafkaBindings.KAFKA_FACTORY_PID).toString());
			}
			
			OWLLiteral literal = factory.getOWLLiteral(serviceName);
			OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(dataProp, test, literal);
			Set<OWLAxiom> testAxioms = new HashSet<OWLAxiom>();
			testAxioms.add(ax);
			
			// Send the update in a separate ontology to the filter consumer
			try {
				OWLOntology ontology = manager.createOntology();
				manager.addAxioms(ontology, testAxioms);
				messagebusProducer.sendMessage(ontology, MessageBusBindings.MESSAGE_BUS_FILTERS);
			} catch (Exception e) {
				logger.error("Could not send stopped service update to kafka", e);
			}
			
			// Stop client consumer
			MessageBusConsumerListener<OWLOntology> busClient = serviceKafkaMapping.remove(service);
			busClient.close();

			// Remove from mapping
			infoMapping.remove(serviceInfo);
		}
	}
	
	@Reference(unbind="unbindWatchdog", cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void bindWatchdog(WatchdogEventService watchdog) {
		logger.info("Watchdog is bound");
		watchdogService = watchdog;
	}
	
	public void unbindWatchdog(WatchdogEventService watchdog) {
		watchdogService = null;
	}

	@Override
	public OWLOntology getBaseOntology() {
		return null;
	}

	@Override
	public OWLDataFactory getDataFactory() {
		return null;
	}

	@Override
	public OWLOntologyManager getOntologyManager() {
		return null;
	}

	@Override
	public PrefixManager getPrefixManager() {
		return null;
	}
	
	/**
	 * Generate a service name from the factory
	 * @param serviceName				The service name
	 * @param factoryServiceName		The factory name
	 * @return							The generated name
	 */
	private String convertserviceName(String serviceName, String factoryServiceName){
		return serviceName + "#" + factoryServiceName;
	}
	
	/**
	 * Create a messagebus consumer for the given service
	 * @param serviceInformation			The service information of the registering service
	 */
	private void createServiceMessageBusConsumer(ServiceInfo serviceInformation) {
		// The service we want to create a kafka consumer for
		MCIService service = serviceInformation.getService();
		String serviceName = serviceInformation.getComponentName();

		// Check if we have multiple service instances
		Map<String, Object> serviceProps = serviceInformation.getProperties();
		if (serviceProps.containsKey("massif.parallel.serviceid")) {
			String factoryServiceName = serviceProps.get("massif.parallel.serviceid").toString();
			serviceName = convertserviceName(serviceName, factoryServiceName);
		}
		
		// Create a consumer to receive messages from the semantic bus
		MessageBusClientConsumer<OWLOntology> consumer = messagebusController.createKafkaConsumer(
				MessageBusBindings.GROUP_NAME_MESSAGE_BUS_SERVICES, 
				SerializerBindings.DESERIALIZER_OWLONTOLOGY, 
				Collections.singletonList(serviceName)
		);
		
		MessageBusConsumerListener<OWLOntology> consumerListener = new MessagebusConsumerListenerImpl(serviceInformation, consumer);
		
		// Save the listener
		serviceKafkaMapping.put(service, consumerListener);
		
		// Register the filters of the service on the bus
		for (final OWLMessageFilter filter : service.getFilter()) {
			logger.info("Binding consumer (" + serviceName + ") for filter " + filter);
	
			// Prepare filter to match to this service
			filter.addAnnotation(serviceName);
	
			try {
				// Send filter to semantic bus that is listening for filters
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				OWLOntology ontology = manager.createOntology();
				manager.addAxioms(ontology, filter.getAxioms());
				messagebusProducer.sendMessage(ontology, MessageBusBindings.MESSAGE_BUS_FILTERS);
			} catch (Exception e) {
				logger.error("Could not send filter ontology to the bus", e);
			}
		}
	}
	
}
