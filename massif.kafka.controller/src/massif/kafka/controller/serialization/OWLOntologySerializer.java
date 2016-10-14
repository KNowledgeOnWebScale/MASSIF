package massif.kafka.controller.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLOntologySerializer implements Serializer<OWLOntology> {

	/**
	 * To process outgoing messages
	 */
	private ByteArrayOutputStream output;
	
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// Nothing to do
	}

	@Override
	public byte[] serialize(String topic, OWLOntology data) {
		// Prepare output
		output = new ByteArrayOutputStream();
		
		// Save ontology to string
		try {
			// Use the owlontology manager to parse the ontology
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology parseOntology = manager.createOntology();
			manager.addAxioms(parseOntology, data.getAxioms());
			
			manager.saveOntology(parseOntology, output);
		} catch (Exception e) {
			throw new SerializationException("Could not serialize OWLOntology to byte[] due to ontology parse error");
		}
		
		// Return the parsed bytes
		return output.toByteArray();
	}

	@Override
	public void close() {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				throw new SerializationException("Could not close outputstream in serialize OWLOntology to byte[]");
			}
			
			output = null;
		}
	}

}
