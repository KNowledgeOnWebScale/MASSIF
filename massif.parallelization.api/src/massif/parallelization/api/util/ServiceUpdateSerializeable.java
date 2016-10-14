package massif.parallelization.api.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;

import massif.parallelization.api.util.OWLOntologyChangeSerializable.OntologyChange;
import massif.scb.api.OWLMessage;

/**
 * Bundles the changes a service needs after duplication.
 * This consists of all changes to the ontology that are necessary and all the messasges in the queue.
 * 
 * Compared to ServiceUpdate, this class can easily be serialized
 * 
 * @author pbonte
 *
 */

public class ServiceUpdateSerializeable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8609258217743875317L;


	private Queue<OWLMessageSerializable> queue;
	

	private List<OWLOntologyChangeSerializable> changes;

	public ServiceUpdateSerializeable(Queue<OWLMessage> queue, List<OWLOntologyChange> changes){
		this.queue = serializeQueue(queue);
		this.changes = serializeChanges(changes);
	}

	public Queue<OWLMessage> getQueue() {
		return deserializeQueue(queue);
	}

	public List<OWLOntologyChange> getChanges() {
		return deserializeChanges(changes);
	}
	
	private Queue<OWLMessageSerializable> serializeQueue(Queue<OWLMessage> queue){
		Queue<OWLMessageSerializable> newQueue = new LinkedList<OWLMessageSerializable>();
		for(OWLMessage message: queue){
			String axioms = OWLAxiomSerializer.serializeAxioms(message.getAxioms());
			OWLMessageSerializable serialMessage = new OWLMessageSerializable(message.getOWLMessage().getIRI().toString(), message.getPacketID(), axioms );
			serialMessage.addTriggeredFilters(message.getTriggeredFilters());
			newQueue.add(serialMessage);
		}
		return newQueue;
	}
	
	private Queue<OWLMessage> deserializeQueue(Queue<OWLMessageSerializable> queueSerialized){
		Queue<OWLMessage> newQueue = new LinkedList<OWLMessage>();
		for(OWLMessageSerializable messageSerial: queueSerialized){
			Set<OWLAxiom> axioms = OWLAxiomSerializer.deserializeAxioms(messageSerial.getAxioms());
			OWLNamedIndividual ind = OWLAxiomSerializer.convertToIndividual(messageSerial.getOWLMessage());
			OWLMessage message = new OWLMessage(ind, messageSerial.getPacketID());
			message.addAxioms(axioms);
			message.addTriggeredFilters(messageSerial.getTriggeredFilters());
			newQueue.add(message);
		}
		return newQueue;
	}
	private List<OWLOntologyChangeSerializable> serializeChanges(List<OWLOntologyChange> changes){
		List<OWLOntologyChangeSerializable> changesSerialized = new ArrayList<OWLOntologyChangeSerializable>();
		for(OWLOntologyChange change: changes){
			OWLOntologyChangeSerializable newChange;
			if(change.isAddAxiom()){
				newChange = new OWLOntologyChangeSerializable(OntologyChange.Addition, OWLAxiomSerializer.serializeAxiom(change.getAxiom()));				
			}else{
				newChange = new OWLOntologyChangeSerializable(OntologyChange.Removal, OWLAxiomSerializer.serializeAxiom(change.getAxiom()));				

			}
			changesSerialized.add(newChange);
		}
		return changesSerialized;
	}
	private List<OWLOntologyChange> deserializeChanges(List<OWLOntologyChangeSerializable> serialChanges){
		List<OWLOntologyChange> newChanges = new ArrayList<OWLOntologyChange>();
			for(OWLOntologyChangeSerializable serialChange: serialChanges){
				OWLOntologyChange change;
				OWLAxiom ax = OWLAxiomSerializer.deserializeAxiom(serialChange.getAxiom());
				if(serialChange.getOntologyChange().equals(OntologyChange.Addition)){
					change = new AddAxiom(null, ax);
				}else{
					change = new RemoveAxiom(null, ax);
				}
				newChanges.add(change);
			}
		return newChanges;
	}
}
