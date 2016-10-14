package massif.framework.util.owl;

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

public class SimpleEntityChecker implements OWLEntityChecker {
	private PrefixManager pm;
    private OWLOntology ontology;
    private OWLDataFactory factory;


    public SimpleEntityChecker(OWLOntology ontology, OWLDataFactory factory,
            PrefixManager pm) {
        this.ontology = ontology;
        this.factory = factory;
        this.pm = pm;
    }


    public SimpleEntityChecker(OWLOntology ontology, OWLDataFactory factory,
            String base) {
        this(ontology, factory, new DefaultPrefixManager(base + "#"));
    }
    
    

    public OWLClass getOWLClass(String name) {
        if (ontology.containsClassInSignature(pm.getIRI(name))) {
            return factory.getOWLClass(":" + name, pm);
        } else {
            return null;
        }
    }

    public OWLObjectProperty getOWLObjectProperty(String name) {
        if (ontology.containsObjectPropertyInSignature(pm.getIRI(name))) {
            return factory.getOWLObjectProperty(":" + name, pm);
        } else {
            return null;
        }
    }

    public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
        if (ontology.containsAnnotationPropertyInSignature(pm.getIRI(name))) {
            return factory.getOWLAnnotationProperty(":" + name, pm);
        } else {
            return null;
        }
    }

    public OWLDataProperty getOWLDataProperty(String name) {
        if (ontology.containsDataPropertyInSignature(pm.getIRI(name))) {
            return factory.getOWLDataProperty(":" + name, pm);
        } else {
            return null;
        }
    }

    public OWLDatatype getOWLDatatype(String name) {
        if (ontology.containsDatatypeInSignature(pm.getIRI(name))) {
            return factory.getOWLDatatype(":" + name, pm);
        } else {
            return null;
        }
    }

    public OWLNamedIndividual getOWLIndividual(String name) {
        if (ontology.containsIndividualInSignature(pm.getIRI(name))) {
            return factory.getOWLNamedIndividual(":" + name, pm);
        } else {
            return null;
        }
    }
}
