package massif.parallelization.api.util;

import java.io.Serializable;

public class OWLOntologyChangeSerializable implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3323196663158863882L;
	
	public enum OntologyChange{ Addition, Removal}

	private OntologyChange change;
	private String axiom;

	public OWLOntologyChangeSerializable(OntologyChange change, String axiom){
		this.change = change;
		this.axiom = axiom;
	}

	public OntologyChange getOntologyChange() {
		return change;
	}


	public String getAxiom() {
		return axiom;
	}

}
