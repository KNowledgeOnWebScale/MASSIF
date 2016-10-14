package massif.framework.util.owl;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.framework.util.owl.CascadingEntityChecker;

/**
 * Does not only find entities in the ontology but also in its imported ontologies
 * @author jfamaey
 *
 */
public class CascadingEntityChecker implements OWLEntityChecker {
	
	final Logger LOGGER = LoggerFactory.getLogger(CascadingEntityChecker.class);

	private Map<OWLOntology, PrefixManager> ontologies;
	private OWLDataFactory factory;
	
	private OWLOntology upperOntology;
	
	public CascadingEntityChecker(OWLDataFactory factory, OWLOntology ontology) {
		this.factory = factory;
		this.upperOntology=ontology;
		ontologies = new HashMap<OWLOntology, PrefixManager>();
		ontologies.put(ontology, new DefaultPrefixManager(
				ontology.getOntologyID().getOntologyIRI().toString() + "#"));
		LOGGER.debug("adding ontology " + ontology.getOntologyID().getOntologyIRI() + " to entity checker");
		for (OWLOntology ont : ontology.getImports()) {
			ontologies.put(ont, new DefaultPrefixManager(
					ont.getOntologyID().getOntologyIRI().toString() + "#"));
			LOGGER.debug("adding ontology " + ont.getOntologyID().getOntologyIRI() + " to entity checker");
		}
	}


	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
		for (OWLOntology ontology : ontologies.keySet()) {
			PrefixManager pm = ontologies.get(ontology);
			if (ontology.containsAnnotationPropertyInSignature(pm.getIRI(name))) {
				LOGGER.debug("annotation property \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLAnnotationProperty(":" + name, pm);
	        }
		}
		LOGGER.debug("annotation property \"" + name + "\" not found");
		return null;
	}


	@Override
	public OWLClass getOWLClass(String name) {
		for (OWLOntology ontology : ontologies.keySet()) {
			PrefixManager pm = ontologies.get(ontology);
			if (pm!=null && ontology.containsClassInSignature(pm.getIRI(name))) {
				LOGGER.debug("class \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLClass(":" + name, pm);
	        }
		}
		LOGGER.info("class \"" + name + "\" not found");
		return null;
	}


	@Override
	public OWLDataProperty getOWLDataProperty(String name) {
		for (OWLOntology ontology : ontologies.keySet()) {
			PrefixManager pm = ontologies.get(ontology);
			if (pm!=null && ontology.containsDataPropertyInSignature(pm.getIRI(name))) {
				LOGGER.debug("data property \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLDataProperty(":" + name, pm);
	        }
		}
		LOGGER.debug("data property \"" + name + "\" not found");
		return null;
	}


	@Override
	public OWLDatatype getOWLDatatype(String name) {
		for (OWLOntology ontology : ontologies.keySet()) {
			PrefixManager pm = ontologies.get(ontology);
			if (ontology.containsDatatypeInSignature(pm.getIRI(name))) {
				LOGGER.debug("datatype \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLDatatype(":" + name, pm);
	        }
		}
		LOGGER.debug("datatype \"" + name + "\" not found");
		return null;
	}


	@Override
	public OWLNamedIndividual getOWLIndividual(String name) {
		for (OWLOntology ontology : ontologies.keySet()) {
			PrefixManager pm = ontologies.get(ontology);
			if (ontology.containsIndividualInSignature(pm.getIRI(name))) {
				LOGGER.debug("individual \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLNamedIndividual(":" + name, pm);
	        }else if(upperOntology.containsIndividualInSignature(pm.getIRI(name))){
	        	//added to retrieve newly added individuals to current ontology, which has been retrieved from cache(different IRI).
	        	LOGGER.debug("individual \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLNamedIndividual(":" + name, pm);
	        }
		}
		LOGGER.debug("individual \"" + name + "\" not found");
		return null;
	}


	@Override
	public OWLObjectProperty getOWLObjectProperty(String name) {
		for (OWLOntology ontology : ontologies.keySet()) {
			PrefixManager pm = ontologies.get(ontology);
			if (ontology.containsObjectPropertyInSignature(pm.getIRI(name))) {
				LOGGER.debug("object property \"" + name + "\" found in " + ontology.getOntologyID().getOntologyIRI());
	            return factory.getOWLObjectProperty(":" + name, pm);
	        }
		}
		LOGGER.debug("object property \"" + name + "\" not found");
		return null;
	}
}
