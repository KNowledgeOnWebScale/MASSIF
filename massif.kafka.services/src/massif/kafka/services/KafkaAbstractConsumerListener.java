package massif.kafka.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import massif.kafka.api.listener.MessageBusConsumerListener;

import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KafkaAbstractConsumerListener implements MessageBusConsumerListener<OWLOntology> {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// GETTER logger
	public Logger getLogger() {
		return logger;
	}
	
	// Worker pool to handle incoming messages
	protected ExecutorService workerpool = Executors.newFixedThreadPool(1);
	
	/**
	 * Process the incoming ontology
	 * @param ontology			The received ontology from the kafka cluster
	 */
	protected abstract void processIncomingOntology(OWLOntology ontology);
	
	@Override
	public void messageReceived(String key, final OWLOntology value) {		
		// Process the message in a new worker
		workerpool.execute(new Runnable() {
			
			@Override
			public void run() {
				// Execute service specific method
				processIncomingOntology(value);
			}
			
		});
	}
	
}
