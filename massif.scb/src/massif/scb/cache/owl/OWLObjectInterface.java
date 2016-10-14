package massif.scb.cache.owl;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;

public interface OWLObjectInterface {
	
	public void add(OWLObject object);
	
	public void add(OWLObjectInterface object);
	
	public OWLObject constructOWLObject(OWLDataFactory dFact);


}
