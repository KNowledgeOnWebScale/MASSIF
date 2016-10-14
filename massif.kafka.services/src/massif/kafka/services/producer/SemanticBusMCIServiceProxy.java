package massif.kafka.services.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import massif.exceptions.NoBusFoundException;
import massif.kafka.api.KafkaBindings;
import massif.kafka.api.MessageBusClientProducer;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;
import massif.watchdog.api.WatchdogEventService;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticBusMCIServiceProxy implements MCIService {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * The kafka client to communicate with the MCI services
	 */
	private MessageBusClientProducer<OWLOntology> messagebusProducer;
	
	/**
	 * List of filters the service is listening for
	 */
	private List<OWLMessageFilter> filters;

	/**
	 * Constructor
	 */
	public SemanticBusMCIServiceProxy(MessageBusClientProducer<OWLOntology> messagebusProducer) {
		this.filters = new ArrayList<OWLMessageFilter>();
		this.messagebusProducer = messagebusProducer;
	}
	
	/**
	 * SETTER filter
	 * @param filter		Add filter to the service
	 */
	public void addfilter(OWLMessageFilter filter){
		filters.add(filter);
	}
	
	@Override
	public void registerWatchdog(WatchdogEventService watchdog) {
		// Nothing to do
	}

	@Override
	public void transmitIn(OWLMessage message) {
		// Add triggered filters to the message 
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		Set<String> triggeredFilters = message.getTriggeredFilters();
		
		for (String triggeredFilter : triggeredFilters) {
			OWLNamedIndividual ax = factory.getOWLNamedIndividual(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "TriggeredFilter"));
			OWLClass filterClass = factory.getOWLClass(IRI.create(triggeredFilter));
			OWLAxiom axiom = factory.getOWLClassAssertionAxiom(filterClass, ax);
			message.addAxiom(axiom);
		}
		
		try {
			// Import the message in a new ontology
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology();
			manager.addAxioms(ontology, message.getAxioms());
			
			// Send the ontology to the Semantic bus
			messagebusProducer.sendMessage(ontology);
		} catch (Exception e) {
			logger.error("Could not send message ontology to the bus", e);
		}
	}

	@Override
	public void transmitOut(OWLMessage message) throws NoBusFoundException {
		// Nothing to do
	}

	@Override
	public void transmitOut(OWLMessage message, String packetID) throws NoBusFoundException {
		// Nothing to do
	}

	@Override
	public void registerBus(OWLSemanticCommunicationBus bus) {
		// Nothing to do
	}

	@Override
	public List<OWLMessageFilter> getFilter() {
		return this.filters;
	}

}
