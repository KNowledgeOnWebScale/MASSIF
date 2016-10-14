package massif.framework.query;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

public class RetrieveLinkedIndividuals {
	
	
	 private static final long MEGABYTE = 1024L * 1024L;

	  public static long bytesToMegabytes(long bytes) {
	    return bytes / MEGABYTE;
	  }
	/**
	 * Retrieves the axioms of the linked individuals that have a connection the the rootInd.
	 * The depth of the search for these individuals can be defined. Meaning that depth 1 will 
	 * only retrieve the individuals linked to the rootInd, whereas depth 2 will also retrieve 
	 * the individuals linked to the individuals that are linked to the rootInd, and so on.
	 * @param rootInd		Starting point of the search. The linked individuals to this individual will be retrieved
	 * @param ontology		The ontology in which the axioms from the individuals need to be find.
	 * @param depth			The depth of the search.
	 * @return				The axioms defining the linked individuals.
	 */
	public static Set<OWLAxiom> getReferencedAxioms2(OWLIndividual rootInd, OWLOntology ontology, int depth){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		if(depth > 0){
			getReferencedAxioms_helper2(rootInd, ontology, depth, axioms);
		}
		
		return axioms;
	}
	/**
	 * Helper method for the recursive method getReferencedAxioms
	 * @param rootInd		Temporarily starting point of the search. The linked individuals to this individual will be retrieved
	 * @param ontology		The ontology in which the axioms from the individuals need to be find.
	 * @param depth			The temporarily depth of the search.
	 * @param axioms		The list with axioms, we pass it by reference so we don't need to create new methodes each time.
	 * @return
	 */
	private static void getReferencedAxioms_helper2(OWLIndividual rootInd, OWLOntology ontology, int depth, Set<OWLAxiom> axioms){
		OWLDataFactory dfact = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLNamedIndividual ind = rootInd.asOWLNamedIndividual();
		//first we add all the referencing axioms
		for(OWLAxiom ax: ontology.getReferencingAxioms(ind,false)){
			if(ax instanceof OWLObjectPropertyAssertionAxiom){
				if(((OWLObjectPropertyAssertionAxiom) ax).getSubject().equals(ind)){
					axioms.add(ax);
				}
				
			}else{
				axioms.add(ax);
			}
		}
		
		//then we loop to find the object properties to add the axioms regarding the linked individual
		for(OWLAxiom ax: ontology.getReferencingAxioms(ind)){
			if(ax instanceof OWLObjectPropertyAssertionAxiom && ((OWLObjectPropertyAssertionAxiom) ax).getSubject().equals(ind)){
				OWLObjectPropertyAssertionAxiom axObj = (OWLObjectPropertyAssertionAxiom)ax;
				OWLIndividual objInd = axObj.getObject();
				
				for(OWLClassExpression clsExp : EntitySearcher.getTypes(objInd, ontology).collect(Collectors.toSet())){
					//adding the class assertion of this linked individual
					axioms.add(dfact.getOWLClassAssertionAxiom(clsExp,objInd));
				}
				if(depth - 1 > 0){
					//if there still is some ground to discover, we dive deeper in the pool with a recursive call
					getReferencedAxioms_helper2(objInd, ontology, depth-1,axioms);
				}
							
			}
		}			
	}
	/**
	 * Retrieves the axioms of the linked individuals that have a connection the the rootInd.
	 * The depth of the search for these individuals can be defined. Meaning that depth 1 will 
	 * only retrieve the individuals linked to the rootInd, whereas depth 2 will also retrieve 
	 * the individuals linked to the individuals that are linked to the rootInd, and so on.
	 * @param rootInd		Starting point of the search. The linked individuals to this individual will be retrieved
	 * @param ontology		The ontology in which the axioms from the individuals need to be find.
	 * @param depth			The depth of the search.
	 * @return				The axioms defining the linked individuals.
	 */
	public static Set<OWLAxiom> getReferencedAxioms(OWLIndividual rootInd, OWLOntology ontology, int depth){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		if(depth > 0){
			axioms.addAll(getReferencedAxioms_helper(rootInd, ontology, depth));
		}
		
		return axioms;
	}
	/**
	 * Helper method for the recursive method getReferencedAxioms
	 * @param rootInd		Temporarily starting point of the search. The linked individuals to this individual will be retrieved
	 * @param ontology		The ontology in which the axioms from the individuals need to be find.
	 * @param depth			The temporarily depth of the search.
	 * @return
	 */
	private static Set<OWLAxiom> getReferencedAxioms_helper(OWLIndividual rootInd, OWLOntology ontology, int depth){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		OWLDataFactory dfact = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLNamedIndividual ind = rootInd.asOWLNamedIndividual();
		//first we add all the referencing axioms
		axioms.addAll(ontology.getReferencingAxioms(ind,false));
		//then we loop to find the object properties to add the axioms regarding the linked individual
		for(OWLAxiom ax: ontology.getReferencingAxioms(ind)){
			if(ax instanceof OWLObjectPropertyAssertionAxiom){
				OWLObjectPropertyAssertionAxiom axObj = (OWLObjectPropertyAssertionAxiom)ax;
				OWLIndividual objInd = axObj.getObject();
				for(OWLClassExpression clsExp : EntitySearcher.getTypes(objInd, ontology).collect(Collectors.toSet())){
					//adding the class assertion of this linked individual
					axioms.add(dfact.getOWLClassAssertionAxiom(clsExp,objInd));
				}
				if(depth - 1 > 0){
					//if there still is some ground to discover, we dive deeper in the pool with a recursive call
					axioms.addAll(getReferencedAxioms_helper(objInd, ontology, depth-1));
				}
							
			}
		}		
		return axioms;		
	}
	static String readFile(String path, Charset encoding) throws IOException 
	{
		File file = new File(path);
	  byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
	  return new String(encoded, encoding);
	}
}
