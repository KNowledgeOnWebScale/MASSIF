package massif.framework.dashboard.api.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

public class Converter {
	
	public static Set<OWLAxiom> checkForIncorrectAnnotations(Set<OWLAxiom> axioms, OWLOntology ontology) {
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		Set<OWLOntology> allOnts = ontology.getImports();
		allOnts.add(ontology);
		for (OWLAxiom ax : axioms) {
			boolean found = false;
			for (OWLOntology ont : allOnts) {
				if (ax instanceof OWLAnnotationAssertionAxiom) {
					OWLAnnotationAssertionAxiom anno = (OWLAnnotationAssertionAxiom) ax;
					OWLIndividual subject = new OWLNamedIndividualImpl(IRI.create(anno.getSubject().toString()));
					System.out.println(anno.getProperty().getIRI());
					System.out.println(ont.getObjectPropertiesInSignature());
					System.out.println(ont.getDataPropertiesInSignature());
					if (ont.containsObjectPropertyInSignature(anno.getProperty().getIRI())) {
						OWLIndividual object = new OWLNamedIndividualImpl(IRI.create(anno.getValue().toString()));
						OWLObjectProperty objProp = new OWLObjectPropertyImpl(anno.getProperty().getIRI());

						newAxioms.add(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objProp, subject,
								object));
						found = true;
						break;
					} else if (ont.containsDataPropertyInSignature(anno.getProperty().getIRI())) {
						OWLDataProperty dataProp = new OWLDataPropertyImpl(anno.getProperty().getIRI());
						String value = anno.getValue().toString();
						if(value.contains("^")){
							value = value.substring(1, value.indexOf('^')-1);
						}
						OWLLiteral lit = new OWLLiteralImpl(value, null, anno.getValue().getDatatypesInSignature().iterator().next());
						 newAxioms.add(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dataProp,
						 subject, lit));
						found = true;
						break;
					} 
				} 
			}
			if(!found){
				newAxioms.add(ax);
			}
		}

		return newAxioms;
	}

}
