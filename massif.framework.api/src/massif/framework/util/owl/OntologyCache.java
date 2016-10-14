package massif.framework.util.owl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This simple class caches loaded ontologies to optimize load time.
 * 
 * @author Pieter Bonte
 * @version 1.0
 */
public class OntologyCache {
	final Logger logger = LoggerFactory.getLogger(OntologyCache.class);
	private AtomicLong teller = new AtomicLong();
	/**
	 * The one and only instance of the cache
	 */
	private static final OntologyCache instance = new OntologyCache();
	
	private static final String cacheFormatPatternAdd = "_fromCache_%d";
	private static final String cacheFormatPatternRemove = "_fromCache_[0-9]*";

	/**
	 * The content of the cache and the manager used in the cache
	 */
	private final HashMap<String, OWLOntology> ontologies = new HashMap<String, OWLOntology>();
	private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	public OntologyCache() {
		//***update imported ontology iris***
		IRI ontologyIRI = IRI.create("http://purl.org/NET/c4dm/timeline.owl");
		IRI localOntIRI = IRI.create("http://ramp.intec.ugent.be/ontology/cleaned/timeline.n3");
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, localOntIRI);
		manager.addIRIMapper(mapper);
		ontologyIRI = IRI.create("http://www.w3.org/2006/time");
		localOntIRI = IRI.create("http://ramp.intec.ugent.be/ontology/cleaned/time.rdf");
		mapper = new SimpleIRIMapper(ontologyIRI, localOntIRI);
		manager.addIRIMapper(mapper);
		ontologyIRI = IRI.create("http://www.w3.org/2006/time-entry");
		localOntIRI = IRI.create("http://ramp.intec.ugent.be/ontology/cleaned/time-entry.rdf");
		mapper = new SimpleIRIMapper(ontologyIRI, localOntIRI);
		manager.addIRIMapper(mapper);
		ontologyIRI = IRI.create("http://purl.org/dc/terms/");
		localOntIRI = IRI.create("http://ramp.intec.ugent.be/ontology/cleaned/dcterms_od.rdf");
		mapper = new SimpleIRIMapper(ontologyIRI, localOntIRI);
		manager.addIRIMapper(mapper);
		ontologyIRI = IRI.create("http://www.w3.org/2006/vcard/ns");
		localOntIRI = IRI.create("http://ramp.intec.ugent.be/ontology/cleaned/ns.owl");
		mapper = new SimpleIRIMapper(ontologyIRI, localOntIRI);
		manager.addIRIMapper(mapper);
		IRI ontologyIRI6 = IRI.create("http://purl.org/ontology/olo/core");
		IRI localOntIRI6 = IRI.create("http://ramp.intec.ugent.be/ontology/cleaned/olo.owl");
		mapper = new SimpleIRIMapper(ontologyIRI6, localOntIRI6);		
		manager.addIRIMapper(mapper);
		//***end update***
	}

	/**
	 *Allows to access the singleton cache object
	 * 
	 * @return OntologyCache instance
	 */
	public static OntologyCache getOntologyCache() {
		return instance;
	}


	/**
	 * Retrieves OWLOntology from cache if present, otherwise the ontology is loaded first.
	 * 
	 * @param IRI
	 *            IRI of the ontology
	 * @param manager
	 *            Used OWLOntologyManager
	 * @return A unique instance of the ontology
	 * @throws OWLOntologyCreationException 
	 */
	
	public synchronized OWLOntology getOntology(String iri, OWLOntologyManager manager) throws OWLOntologyCreationException {
		logger.debug("getting from cache: " + iri);
		IRI iri_obj = IRI.create(iri);
		
		if (!ontologies.containsKey(iri)) {
			//the ontology is not in the cache yet		
			//check if it has been loaded somehow			
			OWLOntology ont = this.manager.getOntology(iri_obj);;
			if (ont == null){
				ont = this.manager.loadOntologyFromOntologyDocument(iri_obj);
			}			
			ontologies.put(iri, ont);
			return copyOntology(ont, manager);						
		} else{
			//return a copy of the cached ontology
			return copyOntology(ontologies.get(iri), manager);
		}
		
	}
	/**
	 * Retrieves OWLOntology from cache if present, otherwise the ontology is loaded first.
	 * 
	 * @param name
	 *            name of the ontology, comparable to the ontology prefix.
	 * @param IRI
	 *            IRI of the ontology
	 * @param manager
	 *            Used OWLOntologyManager
	 * @return A unique instance of the ontology
	 * @throws OWLOntologyCreationException 
	 */
	@Deprecated
	public synchronized OWLOntology getOWLOntology(String name, String iri, OWLOntologyManager manager) throws OWLOntologyCreationException {
		logger.debug("getting from cache: " + name);
		IRI iri_obj = IRI.create(iri);
		
		if (!ontologies.containsKey(name)) {
			//the ontology is not in the cache yet		
			//check if it has been loaded somehow			
			OWLOntology ont = manager.getOntology(iri_obj);;
			if (ont == null){
				ont = manager.loadOntologyFromOntologyDocument(iri_obj);
			}			
			ontologies.put(name, ont);
			return copyOntology(ont, manager);						
		} else{
			//return a copy of the cached ontology
			return copyOntology(ontologies.get(name), manager);
		}
		
	}
	/**
	 * Retrieves a cached ontology by its name.
	 * @param name	Name of the ontology, comparable with the prefix.
	 * @param manager
	 * @return
	 */
	@Deprecated
	public synchronized OWLOntology getOWLOntology(String name, OWLOntologyManager manager) {
		logger.debug("getting from cache: " + name);

		if (ontologies.containsKey(name)) {
			return copyOntology(ontologies.get(name), manager);
			
		} else{
			return null;
		}
		
		
	}


	/**
	 * Allows to clear the cache. 
	 */
	public synchronized void clear() {
		ontologies.clear();
		logger.debug("Cache was successfully cleared");
	}

	/**
	 * getManager
	 * 
	 * @return OWLOntologyManager
	 */
	public OWLOntologyManager getManager() {
		return this.manager;
	}


	/**
	 * Makes a deep copy of an ontology object.
	 * @param baseOntology	The ontology to be copied.
	 * @param manger		The ontology manager to manage the new ontology.
	 * @return				A deep copy of the original ontology.
	 */
	public OWLOntology copyOntology(OWLOntology baseOntology, OWLOntologyManager manger) {
		String ontIRIString = addCacheToPrefix(baseOntology.getOntologyID().getOntologyIRI().get().toString(), teller.incrementAndGet());
		IRI ontIRI = IRI.create(ontIRIString);
		OWLOntology ontology = null;
		try {
			ontology = manger.copyOntology(baseOntology, OntologyCopy.DEEP);
			//ontology = manager.createOntology(ontIRI);
			
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to create ontology", e);
		}
//		OWLDataFactory factory = manger.getOWLDataFactory();
//		OWLImportsDeclaration id = factory.getOWLImportsDeclaration(baseOntology.getOntologyID().getOntologyIRI().get());
//		AddImport ai = new AddImport(ontology, id);
//		manger.applyChange(ai);
		
		//Dirty hack
		/*String unparsedOntology = "Prefix: owl: <http://www.w3.org/2002/07/owl#>\nPrefix: rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPrefix: rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPrefix: xml: <http://www.w3.org/XML/1998/namespace>\nPrefix: xsd: <http://www.w3.org/2001/XMLSchema#>\n\n\n\nOntology: <@ONTOLOGYNAME@>\n\nImport: <@ONTOLOGYIMPORT@>";
		unparsedOntology = unparsedOntology.replace("@ONTOLOGYNAME@", ontIRIString).replace("@ONTOLOGYIMPORT@", baseOntology.getOntologyID().getOntologyIRI().get().toString());
		InputStream stream = new ByteArrayInputStream(unparsedOntology.getBytes(StandardCharsets.UTF_8));
		try {
			ontology = manger.loadOntologyFromOntologyDocument(stream);
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to parse ontology: " + unparsedOntology,e);
		}*/
		return ontology;

	}

	public static String addCacheToPrefix(String iri, long teller) {
		int index = iri.length();
		if(iri.contains("owl")){
			index = iri.lastIndexOf("owl");
		}
		if(iri.contains(".rdf")){
			index = iri.lastIndexOf(".rdf");
		}
		String basicIRI = iri.substring(0, index) + cacheFormatPatternAdd + iri.substring(index);
		
		return String.format(basicIRI, teller);
	}
	public static String removeCacheFromIRI(String cacheIRI){
		return cacheIRI.replaceAll(cacheFormatPatternRemove, "");
	}
}
