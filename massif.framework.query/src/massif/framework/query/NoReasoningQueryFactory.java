package massif.framework.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

class NoReasoningQueryFactory extends QueryFactory{
	
	private PelletReasoner pelletReasoner;

	NoReasoningQueryFactory(OWLOntology ontology, Collection<String> selectQueries, Collection<String> constructQueries){
		super(ontology,selectQueries, constructQueries);	
				
	}

	/***
	 * Interaction Methods
	 */
	public synchronized void update() {
		logger.warn("update(): This operation is not supported for this type of QueryFactory.");
	}
	
	public synchronized void removeAxioms(Set<OWLAxiom> axioms){
		logger.warn("removeAxioms(): This operation is not supported for this type of QueryFactory.");
	}
	
	public synchronized void addAxiomsDirectly(Set<OWLAxiom> axioms){		
		logger.warn("addAxiomsDirectly(): This operation is not supported for this type of QueryFactory.");
	}
	
	public synchronized void addAxioms(Set<OWLAxiom> axioms){
		logger.warn("addAxioms(): This operation is not supported for this type of QueryFactory.");
	}
	
	public synchronized void updateFullMaterialization(){
		logger.warn("updateFullMaterialization(): This operation is not supported for this type of QueryFactory.");
	}
	
	public List<Map<String,String>> query(Collection<String> queries){
		return query(queries, null);		
	}
	
	@Override
	public List<Map<String, String>> query(Collection<String> queries, Map<String, String[]> filter) {
		List<Map<String,String>> allResults = new ArrayList<Map<String,String>>();
		Model model = getOntologyModel(manager, ontology);
		
		Iterator<String> it = queries.iterator();
		while (it.hasNext() && (executeAll || allResults.size() == 0)) {
			String query = it.next();
			
			List<Map<String, String>> result = exec(model, query, filter);
			if (result.size() > 0) {
				if (!executeAll) {
					// Only log if we stop at the first triggered query
					logger.info(query + " triggered");
				}
				
				allResults.addAll(result);
			}
		}
		
		return allResults;
	}
	
	/***
	 * Getters and setters
	 */

	public OWLReasoner getReasoner(){
		return pelletReasoner;
	}

	@Override
	public List<Set<OWLAxiom>> constructQuery(Collection<String> queries) {
		List<Set<OWLAxiom>> allResults = new ArrayList<Set<OWLAxiom>>();
		Model model = getOntologyModel(manager, ontology);
		
		Iterator<String> it = queries.iterator();
		while (it.hasNext() && (executeAll || allResults.size() == 0)) {
			String query = it.next();
			
			Set<OWLAxiom> result = execConstruct(model, query);
			if (result.size() > 0) {
				if (!executeAll) {
					// Only log if we stop at the first triggered query
					logger.info(query + " triggered");
				}
				
				allResults.add(result);
			}
		}
		return allResults;
	}

	
}
