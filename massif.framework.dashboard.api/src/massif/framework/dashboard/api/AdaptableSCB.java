package massif.framework.dashboard.api;

import java.util.List;
/**
 * Allows to adapt the SCB ontology
 * 
 * @author pbonte
 *
 */
public interface AdaptableSCB {
	
	public enum Syntax {jsonld,rdfxml,turtle, dl, functional, latexDocument, manchester};
	
	/**
	 * Retrieves the ABox and TBox of the loaded ontology, inclusive the registered filters.
	 * @return	A representation of the ontology.
	 */
	public String getOntologyPrint();
	
	public String getOntologyPrint(Syntax syntax);
	/**
	 * Adds new concepts to the loaded ontology.
	 * @param concepts	defines new classes to be added to the ontology.
	 * @return returns false if new concepts make ontology inconsistent, true otherwise.
	 */
	public boolean addToOntology(String concepts);

}
