package massif.scb.cache.owl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class OWLObjHasValue implements OWLObjectInterface{

	private OWLObjectProperty prop;
	private OWLIndividual ind;
	private OWLObjectInterface owlObject;
	@Override
	public void add(OWLObject object) {
		if(object instanceof OWLIndividual){
			ind = (OWLIndividual)object;
		}
		if(object instanceof OWLObjectProperty){
			prop = (OWLObjectProperty)object;
		}
		
	}

	@Override
	public void add(OWLObjectInterface object) {
		owlObject = object;
		
	}

	@Override
	public OWLObject constructOWLObject(OWLDataFactory dFact) {
		return dFact.getOWLObjectHasValue(prop, ind);
	}
	
	public String toString(){
		return "ObjectHasValue( " + prop.toString() + ", " + ind.toString() + " )";
	}

}
