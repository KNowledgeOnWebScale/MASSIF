package massif.example.ca.call;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.contextadapter.api.ContextAdapter;
import massif.framework.util.owl.OntologyCache;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLSemanticCommunicationBus;

@Component(immediate=true,property={"tag=Call"})
public class CallAdapter implements ContextAdapter{

	private OWLSemanticCommunicationBus bus;
	// Util variables
	protected final ExecutorService workerpool = Executors.newFixedThreadPool(1);
	protected AtomicLong id = new AtomicLong();
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	
	final Logger logger = LoggerFactory.getLogger(CallAdapter.class);


	@Activate
	public void activate(BundleContext context){
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		logger.info("Started");

	}
	@Override
	public void transmitIn(Map<String, Object> in) {
		// TODO Auto-generated method stub
		workerpool.execute(new Runnable() {

			@Override
			public void run() {
				String prefix = "http://massif.example.owl#";
				long counter = id.getAndIncrement();
				Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
				OWLClass eventClass = factory.getOWLClass(prefix+"Event");

				OWLNamedIndividual event = factory.getOWLNamedIndividual(prefix+"Event_"+counter);
				OWLObjectProperty hasContextProp = factory.getOWLObjectProperty(prefix+"hasContext");
				OWLClass callClass = factory.getOWLClass(prefix+"Call");
				OWLNamedIndividual call = factory.getOWLNamedIndividual(prefix+"Call_"+counter);
				axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasContextProp, event, call));
				axioms.add(factory.getOWLClassAssertionAxiom(eventClass, event));
				axioms.add(factory.getOWLClassAssertionAxiom(callClass, call));
				OWLMessage message = new OWLMessage(event, counter+"");
				message.addAxioms(axioms);

				transmitOut(message, counter+"");

			}
			
		});
		
	}

	@Override
	public void transmitOut(OWLMessage message, String packetID) {
		// TODO Auto-generated method stub
		bus.publish(message, packetID);
	}

	@Reference(unbind="unbindOWLSemanticCommunicationBus")
	@Override
	public void bindOWLSemanticCommunicationBus(OWLSemanticCommunicationBus bus) {
		// TODO Auto-generated method stub
		this.bus = bus;
	}

	@Override
	public void unbindOWLSemanticCommunicationBus(OWLSemanticCommunicationBus bus) {
		// TODO Auto-generated method stub
		
	}

	// TODO: class provided by template

}
