package massif.scb.cache;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;


public class LevelTwo {
	
	private String filter;
	private OWLClassExpression entities;
	public LevelTwo(OWLClassExpression entities, String filter){
		this.entities = entities;
		this.filter = filter;
	}
	
	public String getFilter(){
		return filter;
	}
	
	public OWLClassExpression getEntities(){
		return entities;
	}

}
