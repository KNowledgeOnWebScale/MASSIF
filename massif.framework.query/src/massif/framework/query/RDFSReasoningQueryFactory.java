package massif.framework.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

public class RDFSReasoningQueryFactory extends QueryFactory {

	protected RDFSReasoningQueryFactory(OWLOntology ontology, Collection<String> selectQueries,
			Collection<String> constructQueries) {
		super(ontology, selectQueries, constructQueries);
		// TODO Auto-generated constructor stub
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
		
		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Iterator<String> it = queries.iterator();
		while (it.hasNext() && (executeAll || allResults.size() == 0)) {
			String query = it.next();
			
			List<Map<String, String>> result = exec(inf, query, filter);
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
	
	

	@Override
	public List<Set<OWLAxiom>> constructQuery(Collection<String> queries) {
		List<Set<OWLAxiom>> allResults = new ArrayList<Set<OWLAxiom>>();
		Model model = getOntologyModel(manager, ontology);
		
		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Iterator<String> it = queries.iterator();
		while (it.hasNext() && (executeAll || allResults.size() == 0)) {
			String query = it.next();
			
			Set<OWLAxiom> result = execConstruct(inf, query);
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
