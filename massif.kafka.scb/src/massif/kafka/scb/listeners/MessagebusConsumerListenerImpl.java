package massif.kafka.scb.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import massif.kafka.api.KafkaBindings;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.api.listener.MessageBusConsumerListener;
import massif.kafka.scb.util.ServiceInfo;
import massif.scb.api.OWLMessage;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagebusConsumerListenerImpl implements MessageBusConsumerListener<OWLOntology> {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// Worker pool to handle incoming messages
	protected ExecutorService workerpool = Executors.newFixedThreadPool(1);
	
	/**
	 * Service information that was bound to the consumer
	 */
	private ServiceInfo serviceInformation;
	
	/**
	 * The create kafka consumer
	 */
	private MessageBusClientConsumer<OWLOntology> consumer;
	
	/**
	 * Constructor
	 */
	public MessagebusConsumerListenerImpl(ServiceInfo serviceInformation, MessageBusClientConsumer<OWLOntology> consumer) {
		this.serviceInformation = serviceInformation;

		this.consumer = consumer;
		this.consumer.setMessageReceivedListener(this);
	}
	
	@Override
	public void messageReceived(String key, final OWLOntology value) {
		logger.info("Received a message");
				
		workerpool.execute(new Runnable() {
			
			@Override
			public void run() {
				// Reconstruct the message
				OWLMessage msg = new OWLMessage(createOWLMessage(value));
				Set<String> triggeredFilters = createTriggeredFilters(value);
				msg.addTriggeredFilters(triggeredFilters);
				msg.addAxioms(value.getAxioms());
				
				// Find the packet ID
				OWLDataProperty packetidProperty = value.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "PacketID"));
				for (OWLLiteral literal : msg.getOWLMessage().getDataPropertyValues(packetidProperty, value)) {
					msg.setPacketID(literal.getLiteral());
				}
				
				// Send to the service
				serviceInformation.getService().transmitIn(msg);
			}
			
		});
	}
	
	/**
	 * Try create an OWLMessage from an ontology
	 * @param o			The incoming ontology
	 * @return			The created message if found
	 */
	private OWLNamedIndividual createOWLMessage(OWLOntology o) {
		for (OWLAxiom e : o.getAxioms()) {
			if (e instanceof OWLClassAssertionAxiom ) {
				OWLClassExpression owlClass = ((OWLClassAssertionAxiom) e).getClassExpression();			
				// Check if it is an event. This is bad, but hey, I want this to work
				if (owlClass.toString().contains("#Event")) {
					return ((OWLClassAssertionAxiom) e).getIndividual().asOWLNamedIndividual();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Find the triggered filter from an ontology
	 * @param o			The incoming ontology
	 * @return			The axioms of the triggered filter
	 */
	private Set<String> createTriggeredFilters(OWLOntology o){
		String triggeredFitlerIRI = KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "TriggeredFilter";
		
		Set<String> triggeredFilters = new HashSet<String>();
		OWLNamedIndividual test = o.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(triggeredFitlerIRI));
		for(OWLClassExpression clss :test.getTypes(o)){
			triggeredFilters.add(clss.asOWLClass().getIRI().toString());
		}
		
		//remove these unnecessary axioms
		for(OWLAxiom ax: o.getAxioms()){
			for(String filter:triggeredFilters){
				if(ax.toString().contains(filter) || ax.toString().contains(triggeredFitlerIRI)){
					o.getOWLOntologyManager().removeAxiom(o, ax);
				}
			}
		}
		return triggeredFilters;
	}

	@Override
	public void close() {
		if (consumer != null) {
			consumer.close();
		}
	}

}
