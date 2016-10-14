package massif.parallelization.api.util;

import java.util.List;
import java.util.Queue;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import massif.scb.api.OWLMessage;

/**
 * Bundles the changes a service needs after duplication.
 * This consists of all changes to the ontology that are necessary and all the messasges in the queue.
 * 
 * @author pbonte
 *
 */

public class ServiceUpdate {
	
	private Queue<OWLMessage> queue;
	

	private List<OWLOntologyChange> changes;

	public ServiceUpdate(Queue<OWLMessage> queue, List<OWLOntologyChange> changes){
		this.queue = queue;
		this.changes = changes;
	}

	public Queue<OWLMessage> getQueue() {
		return queue;
	}

	public List<OWLOntologyChange> getChanges() {
		return changes;
	}
}
