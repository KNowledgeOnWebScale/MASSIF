package massif.framework.ontologywrapper;

import java.util.HashMap;
import java.util.Map;

import massif.framework.util.owl.OntologyCache;
import massif.ontology.api.AbstractOntologyFactory;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.astute.decision.engine.ContextModelListener;

/**
 * This class is a simple collection of all objects belonging to one ontology needed to create new individuals in the ontology
 * 
 * @author Pieter Bonte
 * @version 2.0
 */
public class WrappedOntology<T extends AbstractOntologyFactory> {
	
	// LOGGER
	private final static Logger logger = LoggerFactory.getLogger(WrappedOntology.class);
			
	// Generic class factory
	private T factory;
	
	/**
	 * The attributes needed to define the ontology
	 */
	private String iriOrginal;
	
	/**
	 * Ontology to be wrapped around
	 */
	private OWLOntology ontology;
	
	/**
	 * The iri of the wrapped ontology
	 */
	private IRI iri;

	// Constructors
	public WrappedOntology() { }
	
	public WrappedOntology(OWLOntology ontology, Class<T> factorytype) {
		this(ontology, ontology.getOntologyID().getOntologyIRI(), factorytype);
	}
	
	public WrappedOntology(OWLOntology ontology, IRI iri, Class<T> factorytype) {
		this.ontology = ontology;
		this.iri = iri;
		
		// If a cache mark is present in the ontology iri, remove it
		this.iriOrginal = OntologyCache.removeCacheFromIRI(iri.toString());
				
		this.factory = createFactory(factorytype, ontology, iriOrginal, null);
	}

	/**
	 * Constructor for a WrappedOntology Constructs a WrappedOntology with the given manager with the given prefix
	 * 
	 * @param String
	 *            iri
	 * @param OWLOntologyManager
	 *            manager
	 * @throws OWLOntologyCreationException
	 */
	public WrappedOntology(String ontologyIRI, OWLOntologyManager manager, Class<T> factorytype) throws OWLOntologyCreationException {
		iriOrginal = ontologyIRI;
		iri = IRI.create(iriOrginal);
		
		boolean ontologyImportedYet = false;
		for (OWLOntology i : manager.getOntologies()) {
			for (OWLOntology j : i.getImportsClosure())
				if (j.getOntologyID().getOntologyIRI().equals(iri))
					ontologyImportedYet = true;
		}
		
		if (!ontologyImportedYet){
			ontology = manager.loadOntologyFromOntologyDocument(iri);
		} else {
			ontology = manager.getOntology(iri);
		}
				
		this.factory = createFactory(factorytype, ontology, iriOrginal.substring(0, 2), null);
	}
	
	/**
	 * Copyconstructor for a WrappedOntology Returns a new ontology copied from the given original and copied using the given manager
	 * 
	 * @param WrappedOntology
	 *            The original ontology
	 * @param OWLOntologyManager
	 * @throws OWLOntologyCreationException
	 */
	public WrappedOntology(WrappedOntology<T> original, OWLOntologyManager manager, Class<T> factorytype, long teller) throws OWLOntologyCreationException {
		this.iriOrginal = new String(original.iriOrginal);
		this.iri = IRI.create(processPrefix(this.iriOrginal, teller));

		OWLDataFactory dataFact = manager.getOWLDataFactory();
		OWLOntology cachedOntology = original.getOntology();

		// create empty new ontology with unique name
		this.ontology = manager.createOntology(iri);
		
		// extract all direct imported ontologies from the cachedOntology and add them to the new ontology
		IRI ontIRI = cachedOntology.getOntologyID().getOntologyIRI();
		OWLImportsDeclaration id = dataFact.getOWLImportsDeclaration(ontIRI);
		AddImport ai = new AddImport(ontology, id);
		manager.applyChange(ai);
		
		this.factory = createFactory(factorytype, this.ontology, this.iriOrginal, null);
	}
	
	/**
	 * Creates a factory based on the generic type
	 * @param clasz				The type of the ontology factory
	 * @param ontology			The ontology you want to use
	 * @param url				The url of the ontology
	 * @param decisionEngine	The listener of the ontology
	 * @return
	 */
	private T createFactory(Class<T> clasz, OWLOntology ontology, String url, ContextModelListener decisionEngine) {
		try {
			// Create an instance of the factory
			T factory = clasz.newInstance();
			factory.createFactory(ontology, url, decisionEngine);
						
			return factory;
		} catch (Exception e) {
			logger.error("Could not create ontology factory", e);
		}
		
		return null;
	}
	
	/**
	 * Processes the prefix to make an unique iri for the new created ontology Only for ontolgy copied from the cache Adds _fromCache_<UID>
	 * to the existing prefix
	 * 
	 * @param prefix
	 *            cached ontology iri
	 * @param teller
	 *            unique id
	 * @return Processed prefix: added _fromCache_<UID>
	 */
	private String processPrefix(String prefix, long teller) {
		int index = prefix.lastIndexOf(".owl");

		return prefix.substring(0, index) + "_fromCache_" + teller + prefix.substring(index);
	}
	
	/** Static Factory methods
	 * Used to created specific WrappedOntology object
	 */
	public static WrappedOntology<AbstractOntologyFactory> createWrapperWithoutFactory(OWLOntology ontology) {
		WrappedOntology<AbstractOntologyFactory> newOnt = new WrappedOntology<AbstractOntologyFactory>();
		newOnt.ontology = ontology;
		
		return newOnt;
	}
	
	/**
	 * Generates and adds an individual from a specific class to the ontology. 
	 * This method should only be used to add individuals that are NOT yet in the ontology.
	 * @param individualIRI		The IRI of the new individual.
	 * @param classIRI			The IRI of class of the new individual.
	 * @return					The WrappedOntology containing the new individual.
	 */
	public WrappedOntology<T> addIndividual(String individualIRI, String classIRI){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//create class
		OWLClass cls = dFact.getOWLClass(IRI.create(classIRI));
		//create individual
		OWLIndividual manInd = dFact.getOWLNamedIndividual(IRI.create(individualIRI));
		//couple class and individual
		OWLAxiom ax = dFact.getOWLClassAssertionAxiom(cls, manInd);
		//add it to the ontology
		manager.addAxiom(ontology, ax);
		//Add declaration 
		OWLDeclarationAxiom ax2 = dFact.getOWLDeclarationAxiom(manInd.asOWLNamedIndividual());
		manager.addAxiom(ontology, ax2);
		
		return this;
	}
	
	/**
	 * Removes an individual from a specific class in the ontology. 
	 * @param individualIRI		The IRI of the  individual.
	 * @param classIRI			The IRI of class of the  individual.
	 * @return					The WrappedOntology containing the removed individual.
	 */
	public WrappedOntology<T> removeIndividual(String individualIRI, String classIRI){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//create class
		OWLClass cls = dFact.getOWLClass(IRI.create(classIRI));
		//create individual
		OWLIndividual ind = dFact.getOWLNamedIndividual(IRI.create(individualIRI));
		//couple class and individual
		OWLAxiom ax = dFact.getOWLClassAssertionAxiom(cls, ind);
		//add it to the ontology
		manager.removeAxiom(ontology, ax);		
		//Remove declaration (not present for all individuals)
		OWLDeclarationAxiom ax2 = dFact.getOWLDeclarationAxiom(ind.asOWLNamedIndividual());
		manager.removeAxiom(ontology, ax2);
		
		return this;
	}
	
	/**
	 * Generates an object property between two existing individuals in the ontology.
	 * @param subjectIRI		The IRI of the subject.
	 * @param propertyIRI		The object property IRI.
	 * @param objectIRI			The IRI of the object.
	 * @return					The WrappedOntology containing the new link between individuals.
	 */
	public WrappedOntology<T> addObjectProperty(String subjectIRI, String propertyIRI, String objectIRI){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//retrieve individuals
		OWLIndividual subjectInd = dFact.getOWLNamedIndividual(IRI.create(subjectIRI));
		OWLIndividual objectInd = dFact.getOWLNamedIndividual(IRI.create(objectIRI));
		//retrieve property
		OWLObjectProperty prop = dFact.getOWLObjectProperty(IRI.create(propertyIRI));
		OWLObjectPropertyAssertionAxiom ax = dFact.getOWLObjectPropertyAssertionAxiom(prop, subjectInd, objectInd);
		//add it to the ontology
		manager.addAxiom(ontology, ax);
		
		return this;
	}
	
	/**
	 * Removes an object property between two existing individuals in the ontology.
	 * @param subjectIRI		The IRI of the subject.
	 * @param propertyIRI		The object property IRI.
	 * @param objectIRI			The IRI of the object.
	 * @return					The WrappedOntology containing the removed link between individuals.
	 */
	public WrappedOntology<T> removeObjectProperty(String subjectIRI, String propertyIRI, String objectIRI){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//retrieve individuals
		OWLIndividual subjectInd = dFact.getOWLNamedIndividual(IRI.create(subjectIRI));
		OWLIndividual objectInd = dFact.getOWLNamedIndividual(IRI.create(objectIRI));
		//retrieve property
		OWLObjectProperty prop = dFact.getOWLObjectProperty(IRI.create(propertyIRI));
		OWLObjectPropertyAssertionAxiom ax = dFact.getOWLObjectPropertyAssertionAxiom(prop, subjectInd, objectInd);
		//add it to the ontology
		manager.removeAxiom(ontology, ax);
		
		return this;
	}
	
	/**
	 * Generates an data property an existing individuals in the ontology and an OWLLiteral.
	 * @param subjectIRI		The IRI of the subject.
	 * @param propertyIRI		The IRI of the data property.
	 * @param literal			The literal that needs to be added
	 * @return					The WrappedOntology containing the new link.
	 */
	public WrappedOntology<T> addDataProperty(String subjectIRI,String propertyIRI, OWLLiteral literal){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//retrieve individuals
		OWLIndividual subjectInd = dFact.getOWLNamedIndividual(IRI.create(subjectIRI));	
		//retrieve property
		OWLDataProperty prop = dFact.getOWLDataProperty(IRI.create(propertyIRI));
		OWLDataPropertyAssertionAxiom ax = dFact.getOWLDataPropertyAssertionAxiom(prop, subjectInd, literal);
		//add it to the ontology
		manager.addAxiom(ontology, ax);
		
		return this;
	}
	
	/**
	 * Removes an data property between an existing individuals in the ontology and an OWLLiteral.
	 * @param subjectIRI		The IRI of the subject.
	 * @param propertyIRI		The IRI of the data property.
	 * @param literal			The literal that needs to be removed
	 * @return					The WrappedOntology containing the removed link.
	 */
	public WrappedOntology<T> removeDataProperty(String subjectIRI,String propertyIRI, OWLLiteral literal){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//retrieve individuals
		OWLIndividual subjectInd = dFact.getOWLNamedIndividual(IRI.create(subjectIRI));	
		//retrieve property
		OWLDataProperty prop = dFact.getOWLDataProperty(IRI.create(propertyIRI));
		OWLDataPropertyAssertionAxiom ax = dFact.getOWLDataPropertyAssertionAxiom(prop, subjectInd, literal);
		//add it to the ontology
		manager.removeAxiom(ontology, ax);
		
		return this;
	}
	
	/**
	 * Return an individual for a specific IRI
	 * @param iri		IRI of the individual
	 * @return			The individual
	 */
	public OWLNamedIndividual getIndividual(String iri){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//retrieve individuals
		OWLNamedIndividual subjectInd = dFact.getOWLNamedIndividual(IRI.create(iri));	
		
		return subjectInd;		
	}
	
	public OWLClass getOWLClass(String classIRI){
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLDataFactory dFact = manager.getOWLDataFactory();
		//create class
		OWLClass cls = dFact.getOWLClass(IRI.create(classIRI));
		
		return cls;
	}
	
	/**
	 * getPrefix
	 * 
	 * @return String prefix
	 */
	public String getPrefix() {
		return iriOrginal;
	}

	/**
	 * getOntology
	 * 
	 * @return OWLOntology ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * getFactory
	 * 
	 * @return OntologyFactory factory
	 */
	public T getFactory() {
		return factory;
	}

	/**
	 * getIri
	 * 
	 * @return IRI iri
	 */
	public IRI getIri() {
		return iri;
	}

}
