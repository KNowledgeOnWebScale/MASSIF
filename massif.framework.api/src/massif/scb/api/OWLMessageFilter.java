package massif.scb.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import massif.scb.api.OWLMessageFilter;

public class OWLMessageFilter {

	public enum Validity{ VOLATILE, TEMPORAL, STATIC};
	private Set<OWLAxiom> axioms;
	private OWLClass filter;
	private boolean isLoadBalanced;
	private Validity validity;
	
	public OWLMessageFilter(OWLClass filter) {
		this.filter = filter;
		axioms = new HashSet<OWLAxiom>();
		isLoadBalanced = false;
		validity = Validity.TEMPORAL;
	}
	public OWLMessageFilter(OWLClass filter, Validity validity){
		this.validity = validity;
	}
	public void setValidity(Validity validity){
		this.validity = validity;
	}
	public Validity getValidity(){
		return validity;
	}
	public void addAxiom(OWLAxiom axiom) {
		axioms.add(axiom);
	}
	
	public OWLClass getOWLFilter() {
		return filter;
	}
	
	public Set<OWLAxiom> getAxioms() {
		return Collections.unmodifiableSet(axioms);
	}
	
	public boolean isLoadBalanced(){
		return isLoadBalanced;
	}
	
	/**
	 * Allows to load balance certain inputs over multiple service, if those are present.
	 * @param loadBalanced true: input shall be spread over multiple service, false: all service will receive this input.
	 */
	public void setLoadBalanced(boolean loadBalanced){
		this.isLoadBalanced = loadBalanced;
	}
	
	public String toString() {
		return OWLMessageFilter.class.getSimpleName() + "(" + filter.getIRI() + ")";
	}
	
	public void addAnnotation(String annotation) {
		/*OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLAnnotation l = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(annotation));
		OWLAxiom lax = factory.getOWLAnnotationAssertionAxiom(filter.getIRI(), l);
		addAxiom(lax);*/
		
		//TODO adapt @jschabal
		throw new UnsupportedOperationException("Needs to be adapted since to switch to owl api 5!");
	}
	
}
