package massif.contextadapter.api;

import java.util.Map;

import massif.scb.api.OWLMessage;
import massif.scb.api.OWLSemanticCommunicationBus;



/**
 * The ContextAdapter interface with all methods needed to communicate with any ContextAdapter
 * @version 1.0
 */
public interface ContextAdapter { 

	public void transmitIn(Map<String,Object> in);
	/**
	 * Transmits message out to the rest of the framework
	 * @param message	Message containing axioms
	 * @param packetID	ID of the current packet flowing through the framework
	 */
	public void transmitOut(OWLMessage message,String packetID);
	/**
	 * OSGi method: allows OSGi framework to bind semantic bus
	 * Needs to be configured using osgi DS (xml-file)
	 * @param bus	Central communication bus
	 */
	public void bindOWLSemanticCommunicationBus(OWLSemanticCommunicationBus bus);
	/**
	 * OSGi method: allows OSGi framework to unbind semantic bus
	 * Needs to be configured using osgi DS (xml-file)
	 * @param bus	Central semantic communication bus
	 */
	public void unbindOWLSemanticCommunicationBus(OWLSemanticCommunicationBus bus);
	
}