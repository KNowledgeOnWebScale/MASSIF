package massif.framework.ontologycache;

import java.util.concurrent.atomic.AtomicLong;

import massif.framework.ontologywrapper.WrappedOntology;
import massif.framework.util.owl.OntologyCache;
import massif.ontology.api.AbstractOntologyFactory;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class GenericWrappedOntologyCache<T extends AbstractOntologyFactory> {
	
	/**
	 * The current ontology ontology cache
	 */
	private OntologyCache cache;
	
	/**
	 * The generic type of the Ontology Factory
	 */
	private Class<T> ttype;
	
	// Constructor
	public GenericWrappedOntologyCache(OntologyCache cache, Class<T> factoryType) {
		this.cache = cache;
		this.ttype = factoryType;
	}

	/**
	 * Create a wrapper for the ontology based on the iri
	 */
	public synchronized WrappedOntology<T> getOntology(String name, String iri, OWLOntologyManager manager) throws OWLOntologyCreationException {
		return new WrappedOntology<T>(cache.getOWLOntology(name, iri, manager), ttype);
	}	

	/**
	 * Create a wrapper for the ontology based on the ontology manager
	 */
	public synchronized WrappedOntology<T> getOntology(String name, OWLOntologyManager manager) throws OWLOntologyCreationException {
		return new WrappedOntology<T>(cache.getOWLOntology(name, manager), ttype);
	}	

	/**
	 * Create a wrapper for the ontology based on an existing wrapper
	 */
	public synchronized WrappedOntology<T> copyOntology(WrappedOntology<?> ontology, OWLOntologyManager manager, AtomicLong teller) throws OWLOntologyCreationException {
		return new WrappedOntology<T>((WrappedOntology<T>) ontology, manager, ttype, teller.incrementAndGet());
	}
	
}
