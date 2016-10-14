package massif.kafka.controller.bindings;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;

import massif.kafka.controller.serialization.MapDeserializer;
import massif.kafka.controller.serialization.MapSerializer;
import massif.kafka.controller.serialization.OWLOntologyDeserializer;
import massif.kafka.controller.serialization.OWLOntologySerializer;

public class SerializerBindings {

	/**
	 * Serializer {@link Map}
	 */
	public final static Class<MapSerializer> SERIALIZER_MAP = MapSerializer.class;
	
	/**
	 * Deserializer {@link Map}
	 */
	public final static Class<MapDeserializer> DESERIALIZER_MAP = MapDeserializer.class;
	
	/**
	 * Serializer {@link OWLOntology}
	 */
	public final static Class<OWLOntologySerializer> SERIALIZER_OWLONTOLOGY = OWLOntologySerializer.class;
	
	/**
	 * Deserializer {@link OWLOntology}
	 */
	public final static Class<OWLOntologyDeserializer> DESERIALIZER_OWLONTOLOGY = OWLOntologyDeserializer.class;
	
}
