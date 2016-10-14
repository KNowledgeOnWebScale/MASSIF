package massif.mciservice.api;

import java.util.List;

import massif.exceptions.NoBusFoundException;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;
import massif.watchdog.api.WatchdogComponent;

/**
 * Interface that defines a service to communicate with the semantic bus
 */
public interface MCIService extends WatchdogComponent {

	/**
	 * Handles incoming semantic message from the bus
	 * @param message			Semantic build message
	 */
	public void transmitIn(OWLMessage message);

	/**
	 * Transmits a message to the bus
	 * @param message			Semantic build message
	 * @throws NoBusFoundException				No bus is active
	 */
	public void transmitOut(OWLMessage message) throws NoBusFoundException;

	/**
	 * Transmits a message to the bus
	 * @param message			Semantic build message
	 * @param packetID			Custom packet id that the bus should use
	 * @throws NoBusFoundException				No bus is active
	 */
	public void transmitOut(OWLMessage message, String packetID) throws NoBusFoundException;

	/**
	 * Registers the bus service to the MCI service
	 * @param bus				The MASSIF bus service
	 */
	public void registerBus(OWLSemanticCommunicationBus bus);

	/**
	 * Filters that the MCI service is interested in
	 * @return					The filters defined by the MCI service
	 */
	public List<OWLMessageFilter> getFilter();

}
