package massif.mciservice.api;

import massif.exceptions.NoBusFoundException;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLSemanticCommunicationBus;

public abstract class AbstractMCIService implements MCIService {

	protected OWLSemanticCommunicationBus bus;

	@Override
	public void transmitOut(OWLMessage message) throws NoBusFoundException {
		if (bus != null) {
			bus.publish((OWLMessage) message, message.getPacketID());
		} else {
			throw new NoBusFoundException("No Bus has been found to transmit message to.");
		}

	}

	@Override
	public void transmitOut(OWLMessage message, String packetID)
			throws NoBusFoundException {
		if (bus != null) {
			bus.publish((OWLMessage) message, packetID);
		} else {
			throw new NoBusFoundException("No Bus has been found to transmit message to.");
		}

	}

	@Override
	public void registerBus(OWLSemanticCommunicationBus bus) {
		this.bus = bus;
	}

}
