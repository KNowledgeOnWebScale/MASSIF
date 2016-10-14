package massif.kafka.services;

import java.util.Collections;

import massif.kafka.api.KafkaBindings;
import massif.kafka.api.KafkaController;
import massif.kafka.api.MessageBusClientConsumer;
import massif.kafka.controller.bindings.MessageBusBindings;
import massif.kafka.controller.bindings.SerializerBindings;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLSemanticCommunicationBus;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

@Component(immediate=true)
public class KafkaServiceMessagesProxy extends KafkaAbstractConsumerListener {
	
	// MASSIF Kafka controller
	private KafkaController messagebusController;
	
	// MASSIF Kafka consumer to receive messages
	private MessageBusClientConsumer<OWLOntology> messagebusConsumer;
	
	// MASSIF Semantic Bus Service
	private OWLSemanticCommunicationBus scb;
	
	@Activate
	public void start() {
		// Create a consumer that listens for massif events
		messagebusConsumer = messagebusController.createKafkaConsumer(
				MessageBusBindings.GROUP_NAME_MESSAGE_BUS_SEMANTIC_BUS,
				SerializerBindings.DESERIALIZER_OWLONTOLOGY, 
				Collections.singletonList(MessageBusBindings.MESSAGE_BUS_MESSAGES)
		);
		
		// Set this class as a listener of the consumer
		messagebusConsumer.setMessageReceivedListener(this);
		
		getLogger().info("Service Messages Proxy started");
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
		// Try find a message in the received ontology
		OWLNamedIndividual message = createOWLMessage(ontology);
		
		if (message != null) {
			getLogger().info("Received a new message on the bus");
			
			// Reconstruct the message
			OWLMessage msg = new OWLMessage(message);
			msg.addAxioms(ontology.getAxioms());
			
			// Find the packet ID
			OWLDataProperty packetidProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IRI.create(KafkaBindings.KAFKA_MESSAGE_BUS_PREFIX + "PacketID"));
			for (OWLLiteral literal : msg.getOWLMessage().getDataPropertyValues(packetidProperty, ontology)) {
				msg.setPacketID(literal.getLiteral());
			}
						
			// Push received message on scb
			scb.publish(msg, msg.getPacketID());
		}
	}

	/**
	 * Try find an {@link OWLMessage} in the received ontology
	 * @param o				The incoming ontology
	 * @return				An event to be handled on the bus
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

	@Override
	public void close() {
		if (messagebusConsumer != null) {
			messagebusConsumer.close();
		}
	}
	
}
