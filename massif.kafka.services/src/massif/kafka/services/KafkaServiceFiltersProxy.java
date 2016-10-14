package massif.kafka.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import massif.kafka.api.KafkaBindings;
import massif.kafka.api.KafkaController;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.MessageBusClientProducer;
import massif.kafka.controller.bindings.MessageBusBindings;
import massif.kafka.controller.bindings.SerializerBindings;
import massif.kafka.services.producer.SemanticBusMCIServiceProxy;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

@Component(immediate=true)
public class KafkaServiceFiltersProxy extends KafkaAbstractConsumerListener {
		
	/**
	 * Service id used for parallel use
	 */
	private int serviceIDNumber = Integer.MAX_VALUE;
	
	// MASSIF Kafka controller
	private KafkaController messagebusController;
	
	// MASSIF Kafka consumer to receive filters
	private MessageBusClientConsumer<OWLOntology> messagebusConsumer;
	
	// MASSIF Semantic Bus Service
	private OWLSemanticCommunicationBus scb;
	
	// List mapping services
	private Map<String, MCIService> serviceMapping;
	private Map<String, Map<String, Object>> serviceProperties;
	
	/**
	 * Constructor
	 */
	public KafkaServiceFiltersProxy() {
		this.serviceMapping = new HashMap<String, MCIService>();
		this.serviceProperties = new HashMap<String, Map<String, Object>>();
	}
	
	@Activate
	public void start() {
		// Create a consumer that listens for service filters
		messagebusConsumer = messagebusController.createKafkaConsumer(
				MessageBusBindings.GROUP_NAME_MESSAGE_BUS_SEMANTIC_BUS,
				SerializerBindings.DESERIALIZER_OWLONTOLOGY, 
				Collections.singletonList(MessageBusBindings.MESSAGE_BUS_FILTERS)
		);
		
		// Set this class as a listener of the consumer
		messagebusConsumer.setMessageReceivedListener(this);
		
		getLogger().info("Service Filters Proxy started");
	}
	
	@Reference(unbind="unbindKafkaController", cardinality=ReferenceCardinality.MANDATORY)
	public void bindKafkaController(KafkaController controller) {
		messagebusController = controller;
	}
	
	public void unbindKafkaController(KafkaController controller) {
		messagebusController = null;
	}
	
	@Reference(unbind="unbindMassifSemanticBus", cardinality=ReferenceCardinality.MANDATORY)
	public void bindMassifSemanticBus(OWLSemanticCommunicationBus scb) {
		this.scb = scb;
	}
	
	public void unbindMassifSemanticBus(OWLSemanticCommunicationBus scb) {
		this.scb = null;
	}

	@Override
	protected void processIncomingOntology(OWLOntology ontology) {
		// Check if services has stopped
		if (!processStoppedServices(ontology)) {
			// Try find a message filter in the received ontology
			OWLMessageFilter messageFilter = createMessageFilter(ontology);
			
			if (messageFilter != null) {
				getLogger().info("Received a filter to be subscribed on the bus: " + messageFilter.toString());

				for (OWLAxiom e : ontology.getAxioms()) {
					messageFilter.addAxiom(e);
				}
				
				// Subscribe to the semantic bus
				for (OWLAnnotation annotation : messageFilter.getOWLFilter().getAnnotations(ontology)) {
					// Extract the service name of the new registered MCI
					String serviceName = ((OWLLiteral) annotation.getValue()).getLiteral();
					String duplicateName = serviceName;
					
					if (serviceName.contains("#")) {
						serviceName = serviceName.substring(0, serviceName.indexOf("#"));
					}
					
					// Checks if the service is already registered
					if (serviceMapping.containsKey(duplicateName)) {
						SemanticBusMCIServiceProxy mciService = (SemanticBusMCIServiceProxy) serviceMapping.get(duplicateName);
						mciService.addfilter(messageFilter);
						
						// Register the filter to the bus
						scb.subscribe(mciService, messageFilter);
					} else {
						// Create a new kafka producer
						MessageBusClientProducer<OWLOntology> producer = messagebusController.createKafkaProducer(
								MessageBusBindings.GROUP_NAME_MESSAGE_BUS_SERVICES + ".proxy." + serviceName, 
								SerializerBindings.SERIALIZER_OWLONTOLOGY,
								Collections.singletonList(duplicateName)
						);
												
						// Create a new proxy mci service
						SemanticBusMCIServiceProxy proxyService = new SemanticBusMCIServiceProxy(producer);
						proxyService.addfilter(messageFilter);
						
						// Create properties needed to register a new service on the bus
						Map<String, Object> properties = new HashMap<String,Object>();
						properties.put("component.name", serviceName);
						properties.put("service.id", serviceIDNumber--);
						
						// Save the mciservice in this controller
						serviceMapping.put(duplicateName, proxyService);
						serviceProperties.put(duplicateName, properties);
						
						// Bind the mciservice to the bus
						scb.bindMCIService(proxyService, properties);
					}
				}
			}
		}
		
	}

	/**
	 * Try find a service message filter in the received ontology
	 * @param o			The received ontology
	 * @return			The found message filter
	 */
	private OWLMessageFilter createMessageFilter(OWLOntology o) {
		for (OWLAxiom e : o.getAxioms()) {
			if (e.isLogicalAxiom()) {
				if (e instanceof OWLSubClassOfAxiom) {
					OWLSubClassOfAxiom subclass = (OWLSubClassOfAxiom) e;
					
					OWLMessageFilter filter = new OWLMessageFilter(subclass.getSubClass().asOWLClass());
					return filter;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if services were stopped. These are cleaned and unsubscribed
	 * @param ontology				The incoming ontology
	 * @return						True if we received stopped services
	 */
	private boolean processStoppedServices(OWLOntology ontology) {
		// Extract service information from the ontology
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLNamedIndividual stoppedService = factory.getOWLNamedIndividual(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "StoppedService"));
		OWLDataProperty dataProp = factory.getOWLDataProperty(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "StoppedServiceName"));
		Set<OWLLiteral> stoppedServiceNames = stoppedService.getDataPropertyValues(dataProp, ontology);
				
		// Check if info states that the service has stopped
		if (!stoppedServiceNames.isEmpty()) {
			getLogger().info("Service Stopped: " + stoppedServiceNames);
			
			for(OWLLiteral stoppedName: stoppedServiceNames){
				String serviceName = stoppedName.getLiteral();
				
				if (serviceMapping.containsKey(serviceName) && serviceProperties.containsKey(serviceName)) {
					// The stopped MCI service
					MCIService stoppedMCI = serviceMapping.get(serviceName);
					
					// Unsubscribe the service
					scb.unbindMCIService(stoppedMCI, serviceProperties.remove(serviceName));

					// Remove from local lists
					serviceMapping.remove(serviceName);
				} else {
					getLogger().error("Service Name <" + serviceName + "> not found");
				}
			}
			
			// Return as message handled
			return true;
		}
		
		return false;
	}

	@Override
	public void close() {
		if (messagebusConsumer != null) {
			messagebusConsumer.close();
		}
	}
	
}
