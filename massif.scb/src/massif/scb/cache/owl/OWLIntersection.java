package massif.scb.cache.owl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;

public class OWLIntersection implements OWLObjectInterface{

	private Set<OWLClassExpression> intersection;
	private Set<OWLObjectInterface> intersections2;
	
	public OWLIntersection(){
		this.intersection = new HashSet<OWLClassExpression>();
		this.intersections2 = new HashSet<OWLObjectInterface>();
	}
	@Override
	public void add(OWLObject object) {
		intersection.add((OWLClassExpression)object);		
	}
	@Override
	public void add(OWLObjectInterface object) {
		intersections2.add(object);
		
	}

	public String toString(){
		String s ="OWLInterSection( ";
		for(OWLClassExpression classExp : intersection){
			s += classExp.toString() +" ; ";
		}
		for(OWLObjectInterface classExp : intersections2){
			s += classExp.toString() +" ; ";
		}
		return s +" ) ";
		
	}
	@Override
	public OWLObject constructOWLObject(OWLDataFactory dFact) {
		Set<OWLClassExpression> intersections = new HashSet<>(intersection);
		for(OWLObjectInterface obj: intersections2){
			intersections.add((OWLClassExpression)obj.constructOWLObject(dFact));
		}
		return dFact.getOWLObjectIntersectionOf(intersections);
	}
}
