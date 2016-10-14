package massif.scb.cache.owl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

public class OWLSomeValuesFrom implements OWLObjectInterface{

	private OWLObjectProperty prop;
	private OWLClass owlClass;
	private OWLObjectInterface owlObject;
	
	@Override
	public void add(OWLObject object) {
		if(object instanceof OWLObjectProperty){
			prop = (OWLObjectProperty)object;
		}
		if(object instanceof OWLClass){
			owlClass = (OWLClass)object;
		}
		
	}

	@Override
	public void add(OWLObjectInterface object) {
		owlObject = object;
		
	}
	public String toString(){
		String tempString = owlClass!=null? owlClass.toString():owlObject.toString();
		return "SomeValuesFrom( " + prop.toString() + ", " + tempString + " )";
	}

	@Override
	public OWLObject constructOWLObject(OWLDataFactory dFact) {
		if(owlClass!=null){
			return dFact.getOWLObjectSomeValuesFrom(prop, owlClass);
		}else{
			OWLObject intermediateObject = owlObject.constructOWLObject(dFact);
			if(intermediateObject instanceof OWLObjectIntersectionOf){
				return dFact.getOWLObjectSomeValuesFrom(prop, (OWLObjectIntersectionOf)owlObject.constructOWLObject(dFact));
			}
			else if(intermediateObject instanceof OWLObjectSomeValuesFrom){
				
				return dFact.getOWLObjectSomeValuesFrom(prop, (OWLObjectSomeValuesFrom)owlObject.constructOWLObject(dFact));
			}
			return null;
		}
	}

}
