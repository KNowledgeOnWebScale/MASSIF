package massif.framework.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
/**
 * Tracks the additions and removals of axioms from a given ontology.
 * @author pbonte
 *
 */
public class MassifOntologyChangeListener implements OWLOntologyChangeListener{

	private Set<OWLAxiom> additions;
	private Set<OWLAxiom> removals;
	private OWLOntology ontology;
	
	
	public  MassifOntologyChangeListener(OWLOntology ontology) {
		additions = new HashSet<OWLAxiom>();
		removals = new HashSet<OWLAxiom>();
		this.ontology = ontology;
		
	}
	
	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)  {
		for(OWLOntologyChange change: changes){
			//additional filter step because all changes throughout all active ontologies trigger this method
			if(ontology.getImportsClosure().contains(change.getOntology())){
				if(change.isAddAxiom()){
					additions.add(change.getAxiom());
				}else{
					removals.add(change.getAxiom());
				}
			}
		}		
	}
	public Set<OWLAxiom> getAdditions(){
		return additions;
	}
	public Set<OWLAxiom> getRemovals(){
		return removals;
	}
	/**
	 * Clears the removal and addition buffer
	 */
	public void flush(){
		synchronized (additions) {
			additions.clear();
		}
		synchronized (removals) {
			removals.clear();
		}
	}

}
