package massif.kafka.controller.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.coode.owlapi.rdfxml.parser.RDFXMLParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;

public class OWLOntologyDeserializer implements Deserializer<OWLOntology> {

	/**
	 * To process incoming message
	 */
	private InputStream input;
	
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// Nothing to do
	}

	@Override
	public OWLOntology deserialize(String topic, byte[] data) {
		// Prepare input
		input = new ByteArrayInputStream(data);
		
		// Create a raw ontology to put axioms in
		OWLOntology parseOntology = null;
		RDFXMLParser parser = new RDFXMLParser();
		
		try {
			parseOntology = OWLManager.createOWLOntologyManager().createOntology();
			parser.parse(new StreamDocumentSource(input), parseOntology);
		} catch (Exception e) {
			Logger.getGlobal().info("Could not deserialize byte[] to OWLOntology due to error in parsing the ontology");
		}
		
		return parseOntology;
	}

	@Override
	public void close() {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				throw new SerializationException("Could not close inputstream in deserializing byte[] to OWLOntology");
			}
			
			input = null;
		}
	}

}
