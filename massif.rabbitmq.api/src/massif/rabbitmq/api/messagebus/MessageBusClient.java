package massif.rabbitmq.api.messagebus;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public interface MessageBusClient {

	/**
	 * Method that will send the given object to the rabbit mq instance linked to the 
	 * @param <T>				The type of the object
	 * @param p					The current producer
	 * @param routingkey		The key you want to send to
	 * @param obj				The object you want to send
	 * @return					The message you want to send
	 */
	<T> boolean sendMessage(String routingkey, T obj);
	
	<T extends Set<OWLAxiom>> boolean sendMessage(String routingkey, T obj, OWLOntologyManager manager);
			
	/**
	 * Transform a serialized message to an object
	 * @param <T>				The type of the object
	 * @param message			The message you want to create object from
	 * @return					The created object
	 */
	<T> Object createObjectFromBytes(byte[] message, Class<T> clz) throws Exception;
	
	OWLOntology createObjectFromBytes(byte[] message) throws Exception;
	
	/**
	 * Close the messagebus client
	 */
	void close();
	
}
