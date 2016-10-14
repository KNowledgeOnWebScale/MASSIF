package massif.scb.api;

import java.util.Map;

import massif.mciservice.api.MCIService;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;

public interface OWLSemanticCommunicationBus {

	public boolean publish(OWLMessage message, String packetID);

	public boolean subscribe(MCIService handler, OWLMessageFilter filter);
	
	public boolean unsubscribe(MCIService handler, OWLMessageFilter filter);

	public void bindMCIService(MCIService mciService, Map<String, Object> properties);

	public void unbindMCIService(MCIService mciService, Map<String, Object> properties);

	public OWLOntology getBaseOntology();

	public OWLDataFactory getDataFactory();

	public OWLOntologyManager getOntologyManager();

	public PrefixManager getPrefixManager();
	

}
