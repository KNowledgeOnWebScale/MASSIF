package massif.parallelization.api.util;

import java.io.Serializable;
import java.util.Set;

public class OWLMessageSerializable implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4435542010702570296L;
	
	private String axioms;
	private String messageIRI;
	private String packetID;
	private Set<String> triggeredFilterIRIs;
	
	public OWLMessageSerializable(String messageIRI) {
		this.messageIRI = messageIRI;
		this.packetID = null;
	}
	
	public OWLMessageSerializable(String messageIRI, String packetID) {
		this(messageIRI);
		this.packetID = packetID;
	}
	public OWLMessageSerializable(String messageIRI, String packetID, String axioms){
		this(messageIRI,packetID);
		this.axioms = axioms;
	}
		
	
	public String getAxioms() {
		return axioms;
	}
		
	public String getOWLMessage() {
		return messageIRI;
	}
	
	public String getPacketID() {
		return packetID;
	}
	
	public void setPacketID(String packetID) {
		this.packetID = packetID;
	}
	public void addTriggeredFilters(Set<String> filter){
		this.triggeredFilterIRIs = filter;
	}
	public Set<String> getTriggeredFilters(){
		return triggeredFilterIRIs;
	}
}
