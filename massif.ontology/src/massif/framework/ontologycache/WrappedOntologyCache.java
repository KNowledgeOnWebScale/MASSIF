package massif.framework.ontologycache;

import java.util.concurrent.atomic.AtomicLong;

import massif.framework.ontologywrapper.WrappedOntology;
import massif.framework.util.owl.OntologyCache;
import massif.ontology.api.AbstractOntologyFactory;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * This class creates WrappedOntologies and utilizes the cached ontologies 
 * from the OntologyCache.
 * 
 * @author Pieter Bonte
 * @version 1.0
 * @param <T>
 */
public class WrappedOntologyCache {
	
	/**
	 * Counter to create caches
	 */
	private AtomicLong teller = new AtomicLong();
	
	/**
	 * The one and only instance of the cache
	 */
	private static WrappedOntologyCache instance;

	/**
	 * Wrapper around the ontology cache to handle generic ontology factories.
	 */
	private GenericWrappedOntologyCache<?> genericCache;
		
	/**
	 * The content of the cache
	 */
	private OntologyCache cache;
		
	// Constructor
	public <T extends AbstractOntologyFactory> WrappedOntologyCache(Class<T> factoryType) {
		this.cache = OntologyCache.getOntologyCache();
		
		// Generate a wrapper around the cache that is generic
		this.genericCache = new GenericWrappedOntologyCache<T>(cache, factoryType);
	}

	/**
	 * getOntologyCache This is the method to access the instance of the cache
	 * @param <E>
	 * 
	 * @return OntologyCache onstance
	 */
	public static <T extends AbstractOntologyFactory> WrappedOntologyCache getOntologyCache(Class<T> factoryType) {
		if (instance == null) {
			instance = new WrappedOntologyCache(factoryType);
		}
		
		return instance;
	}
	
	/**
	 * Gets an OWLOntology from cache
	 * 
	 * @param name
	 *            name of the ontology
	 * @param iri
	 *            IRI of the ontology
	 * @param manager
	 *            Used OWLOntologyManager
	 * @return A unique instance of the ontology
	 * @throws OWLOntologyCreationException 
	 */
	public synchronized OWLOntology getOWLOntology(String name, String iri, OWLOntologyManager manager) throws OWLOntologyCreationException {
		return cache.getOWLOntology(name, iri, manager);
	}
	
	/**
	 * getManager
	 * 
	 * @return OWLOntologyManager
	 */
	public OWLOntologyManager getManager() {
		return this.cache.getManager();
	}
	
	public OWLOntology copyOntology(OWLOntology baseOntology, OWLOntologyManager manger) {
		return cache.copyOntology(baseOntology, manger);
	}
	
	/**
	 * Create a wrapper for the ontology based on the iri
	 */
	public synchronized <T extends AbstractOntologyFactory> WrappedOntology<T> getOntology(String name, String iri, OWLOntologyManager manager) throws OWLOntologyCreationException {
		return (WrappedOntology<T>) genericCache.getOntology(name, iri, manager);
	}
	
	/**
	 * Create a wrapper for the ontology based on the ontology manager
	 */
	public synchronized <T extends AbstractOntologyFactory> WrappedOntology<T> getOntology(String name, OWLOntologyManager manager) throws OWLOntologyCreationException {
		return (WrappedOntology<T>) genericCache.getOntology(name, manager);
	}	
	
	/**
	 * Create a wrapper for the ontology based on an existing wrapper
	 */
	public synchronized <T extends AbstractOntologyFactory> WrappedOntology<T> copyOntology(WrappedOntology<T> ontology, OWLOntologyManager manager) throws OWLOntologyCreationException {
		return (WrappedOntology<T>) genericCache.copyOntology(ontology, manager, teller);
	}
	
}
