package massif.scb.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class OWLMessage implements Serializable {

	private static final long serialVersionUID = -4194798112604032009L;
	
	private HashSet<OWLAxiom> axioms;
	private OWLNamedIndividual message;
	private String packetID;
	private Set<String> triggeredFilterIRIs;
	
	public OWLMessage(OWLNamedIndividual message) {
		this.axioms = new HashSet<OWLAxiom>();
		this.message = message;
		this.packetID = null;
	}
	
	public OWLMessage(OWLNamedIndividual message, String packetID) {
		this.axioms = new HashSet<OWLAxiom>();
		this.message = message;
		this.packetID = packetID;
	}
		
	public void addTriggeredFilters(Set<String> filter){
		this.triggeredFilterIRIs = filter;
	}
	public Set<String> getTriggeredFilters(){
		return triggeredFilterIRIs==null? Collections.EMPTY_SET : triggeredFilterIRIs;
	}
	public void addAxiom(OWLAxiom axiom) {
		axioms.add(axiom);
	}
	
	public void addAxioms(Collection<OWLAxiom> axiom) {
		axioms.addAll(axiom);
	}
		
	public Set<OWLAxiom> getAxioms() {
		return Collections.unmodifiableSet(axioms);
	}
		
	public OWLNamedIndividual getOWLMessage() {
		return message;
	}
	
	public String getPacketID() {
		return packetID;
	}
	
	public void setPacketID(String packetID) {
		this.packetID = packetID;
	}
	
}
