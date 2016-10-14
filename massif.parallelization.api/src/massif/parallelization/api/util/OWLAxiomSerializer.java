package massif.parallelization.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFParser;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OWLAxiomSerializer {
	
	private static Logger logger = LoggerFactory.getLogger(OWLAxiomSerializer.class);
	
	public static String serializeAxioms(Set<OWLAxiom> ax) {
		// Create bytearray to send over the network
		ByteArrayOutputStream outbytearray = new ByteArrayOutputStream();
		
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology parseOntology = manager.createOntology();
			manager.addAxioms(parseOntology, ax);
			
			// Save ontology to string
			manager.saveOntology(parseOntology, outbytearray);
		} catch (Exception e) {
			logger.error(e.toString());
			return null;
		}
		
		return outbytearray.toString();
	}
	
	public static String serializeAxiom(OWLAxiom ax){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(1);
		axioms.add(ax);
		return serializeAxioms(axioms);
	}
	
	public static Set<OWLAxiom> deserializeAxioms(String ax) {
		// Check if object is valid
		if (ax == null) {
			logger.error("Object we received to deserialize was null!");
			return null;
		}
		
		// Prepare input
		InputStream input = new ByteArrayInputStream(ax.getBytes());
		
		// Create a raw ontology to put axioms in
		OWLOntology parseOntology = null;
		
		try {
			parseOntology = OWLManager.createOWLOntologyManager().createOntology();
			
			RDFXMLParser parser = new RDFXMLParser();
			OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
			parser.parse(new StreamDocumentSource(input), parseOntology,config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<OWLAxiom> filteredSet = new HashSet<OWLAxiom>();
		for(OWLAxiom axiom: parseOntology.getAxioms()){
			if(!(axiom instanceof OWLDeclarationAxiom)){
				filteredSet.add(axiom);
			}
		}
		
		return filteredSet;
		
	}
	public static OWLAxiom deserializeAxiom(String ax){
		Set<OWLAxiom> axioms = deserializeAxioms(ax);
		if(axioms.size() > 1){
			logger.error("Tried to deserialize one axiom but found more, only first one has been returned");
		}
		if(axioms.size() == 0){
			logger.error("No axioms found to be deserialized!");
			return null;
		}

		return axioms.iterator().next();

	}
	
	public static OWLNamedIndividual convertToIndividual(String iri){
		OWLDataFactory fact = OWLManager.getOWLDataFactory();
		return fact.getOWLNamedIndividual(iri);
	}
}
