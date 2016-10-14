package massif.framework.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import massif.framework.util.owl.OntologyCache;
import massif.journal.api.JournalService;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

 class PrecomputedSubSetQueryFactory extends QueryFactory {
	
	protected Model model;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected Map<String,String> originalTypes; 		//remembers the original type of the enriched individuals
	protected OWLOntology tempOntology;
	protected MassifOntologyChangeListener changeListener;
	protected OWLReasoner tempReasoner;
	
	private String currentRun;
	
	protected PrecomputedSubSetQueryFactory(OWLOntology ontology, Collection<String> selectQueries, Collection<String> constructQueries){
		super(ontology,selectQueries,constructQueries);
	
		try {
			tempOntology = OntologyCache.getOntologyCache().getOWLOntology(ontology.getOntologyID().getOntologyIRI().toString(), ontology.getOntologyID().getOntologyIRI().toString(), ontology.getOWLOntologyManager());
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to load copy of <"+ontology.getOntologyID().getOntologyIRI().toString()+">",e);
		}		
		if(this.reasoner == null){
			//this.reasoner = new Reasoner(ontology);
			this.tempReasoner = new Reasoner(tempOntology);
		}
		//reasoner.flush();
		model = getOntologyModel(manager, ontology);
		originalTypes = new HashMap<String,String>();
		
		this.changeListener =  new MassifOntologyChangeListener(ontology);
		//register the change listener to the ontology manager
		manager.addOntologyChangeListener(changeListener);
		
	}
	
	protected PrecomputedSubSetQueryFactory(OWLOntology ontology, Collection<String> queries, Collection<String> constructQueries,JournalService service){
		this(ontology, queries,constructQueries);
		
		this.jsservice = service;
	}
		
	/***
	 * Interaction Methods
	 */
	public synchronized void update(){
		long time1 = System.currentTimeMillis();
		Set<OWLAxiom> addAxiom = new HashSet<OWLAxiom>(changeListener.getAdditions());
		Set<OWLAxiom> removeAxiom = new HashSet<OWLAxiom>(changeListener.getRemovals());
		
		Set<OWLAxiom> readds= removeAxioms(removeAxiom);	
		currentRun = "add";
		addAxioms(addAxiom);
		//also add the re-additions that have to be done after the removal
		currentRun = "readds";
		addAxioms(readds);
		changeListener.flush();
	}
	/**
	 * Removes the incoming axioms, also checks if the removal has influence on existing axioms.
	 * Those that have the be re-added are returned
	 * @param axioms	a list of axioms that should be removed.
	 * @return			a list with axioms that need to be re-added after the removal.
	 */
	public synchronized Set<OWLAxiom> removeAxioms(Set<OWLAxiom> axioms){
		Set<OWLIndividual> removalInd = new HashSet<OWLIndividual>();
		Set<OWLAxiom> genAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> freshAxioms = new HashSet<OWLAxiom>();
		
		OWLOntology tempOnt;
		try {
			tempOnt = manager.createOntology();
			for(OWLAxiom ax: axioms){
				if(ax instanceof OWLObjectPropertyAssertionAxiom){
					manager.addAxiom(tempOnt, ax);
					OWLIndividual ind = ((OWLObjectPropertyAssertionAxiom)ax).getSubject();
					//check if an individual has been enriched before
					Set<OWLClassExpression> classInd = EntitySearcher.getTypes(ind, ontology).collect(Collectors.toSet());
					if(classInd.size()>1){
						removalInd.add(ind);
					}
				}
				if(ax instanceof OWLDataPropertyAssertionAxiom){
					manager.addAxiom(tempOnt, ax);
					//check if an individual has been enriched before
					OWLIndividual ind = ((OWLDataPropertyAssertionAxiom)ax).getSubject();
					Set<OWLClassExpression> classInd = EntitySearcher.getTypes(ind, ontology).collect(Collectors.toSet());
					if(classInd.size()>1){
						removalInd.add(ind);
					}
				}			
			}
			//for all the individuals that have been enriched, we need to remove the additional axioms, except the original
			for(OWLIndividual ind : removalInd){
				Set<OWLClassExpression> classInd = EntitySearcher.getTypes(ind, ontology).collect(Collectors.toSet());
				String originalClass = originalTypes.get(ind.asOWLNamedIndividual().getIRI().toString());
				if(originalClass != null){
					for(OWLClassExpression clss : classInd){
						//only remove axioms if the are not the original class type			
						if(!originalClass.equals(clss.toString())){
							genAxioms.add(manager.getOWLDataFactory().getOWLClassAssertionAxiom(clss, ind));					
						}else{
							freshAxioms.add(manager.getOWLDataFactory().getOWLClassAssertionAxiom(clss, ind));
						}
					}
				}
			}
			//remove previous generated axioms from ontology
			manager.addAxioms(tempOnt, genAxioms);
			Model tempModel = getOntologyModel(manager, tempOnt);
			model.remove(tempModel.listStatements());
			tempModel.close();
			//remove the additional classes from the existing ontology
			manager.removeAxioms(ontology, genAxioms);
			//remove temp ontology
			manager.removeOntology(tempOnt);		
		} catch (OWLOntologyCreationException e) {
			logger.error("Could not create tempOntology",e);
		}
		return freshAxioms;
	}
	/**
	 * Adds the axioms directly to the model without enrichment.
	 * @param axioms		list of axioms to be added.
	 */
	public synchronized void addAxiomsDirectly(Set<OWLAxiom> axioms){
		OWLOntology tempOnt;
		try {
			tempOnt = manager.createOntology();
			manager.addAxioms(tempOnt, axioms);
			Model tempModel = getOntologyModel(manager, tempOnt);
			model.add(tempModel.listStatements());
			tempModel.close();
			manager.removeOntology(tempOnt);

		} catch (OWLOntologyCreationException e) {
			logger.error("Could not create tempOntology",e);
		}
	}
	/**
	 * Adds new axioms to the ontology and computes its inferences, adds them to the root ontology so its stays fully materialized.
	 * It should be noted that possible changes the new axioms have on the original ontology will not be captured. 
	 * Whereas changes from the orginal ontology onto the new axioms will be taken into account.
	 * @param axioms		The to-be-added axioms.
	 */
	public synchronized void addAxioms(Set<OWLAxiom> axioms){
		addAxiomsImproved(axioms, 3);
		
	}
	public synchronized void addAxiomsImproved(Set<OWLAxiom> axioms, int depth){
		//we first add the new axioms to the ontology so the inferences can be calculated
		manager.addAxioms(tempOntology, axioms);
		//we flush the reasoner, so it sees the newly added axioms
		
		// Log start
		long logstart = System.currentTimeMillis();
		
		if (jsservice != null) {
			jsservice.log(1, "ReasoningTime" + "-" + currentRun, logId, null, null, null, "transmitIn", new ArrayList<String>());
		}
		
		tempReasoner.flush();
		Set<OWLAxiom> genAxioms = new HashSet<OWLAxiom>();
		for(OWLAxiom ax:axioms){
			if(ax instanceof OWLClassAssertionAxiom){
				OWLClassAssertionAxiom clsAx = (OWLClassAssertionAxiom)ax;
				OWLNamedIndividual entity = clsAx.getIndividual().asOWLNamedIndividual();
				System.out.println("ind: " + entity);
				boolean inOnt = false;
				//we check if the ind is in the ontology, if so add his neighbours
				Set<OWLAxiom> neighbours =null;
				if(ontology.containsIndividualInSignature(entity.getIRI())){
					neighbours = RetrieveLinkedIndividuals.getReferencedAxioms2(entity, ontology, depth);
					//add neighbours
					manager.addAxioms(tempOntology, neighbours);
					tempReasoner.flush();
					inOnt = true;
				}
				
				Set<OWLClass> classes = tempReasoner.getTypes(entity, false).getFlattened();
				
				for (OWLClass type :classes) {
					//save the original type of the individual
					if(!originalTypes.containsKey(entity.getIRI().toString())){
						originalTypes.put(entity.getIRI().toString(), clsAx.getClassExpression().toString());
					}
					genAxioms.add(manager.getOWLDataFactory().getOWLClassAssertionAxiom(type, entity));
				}	
				if(inOnt){
					manager.removeAxioms(tempOntology, neighbours);
					tempReasoner.flush();
				}
				
			}
		}
		
		// Log stop		
		logger.info("Reasoning time for packetID: " + logId + " => " + (System.currentTimeMillis() - logstart) + " ms");
		if (jsservice != null) {
			jsservice.log(1, "ReasoningTime" + "-" + currentRun, logId, null, null, null, "transmitOut", new ArrayList<String>());
		}
		
		//clear the tempontology
		manager.removeAxioms(tempOntology, tempOntology.getAxioms());
		//we add the new axioms the the original ontology
		manager.addAxioms(ontology, genAxioms);
		//incrementally update the model
		axioms.addAll(genAxioms);
		//add the axioms to the model
		addAxiomsDirectly(axioms);
		tempReasoner.flush();		
	}
	/**
	 * Add axioms and fully rematerializes the whole ontology. This is useful when the new axioms have an influence on the original ontology.
	 */
	public synchronized void updateFullMaterialization(){
		reasoner.flush();
		List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
	    // generators.add(new InferredSubClassAxiomGenerator());
	    generators.add(new InferredClassAssertionAxiomGenerator());
		InferredOntologyGenerator infGen = new InferredOntologyGenerator(reasoner,generators);
		infGen.fillOntology(manager.getOWLDataFactory(), ontology);
		//we set the model equal to null because it needs to be fully regenerated
		model.close();
		model = null;
	}
	
	public List<Map<String,String>> query(Collection<String> queries){
		return query(queries, null);
	}

	@Override
	public List<Map<String, String>> query(Collection<String> queries, Map<String, String[]> filter) {
		long time = System.currentTimeMillis();
		
		if (jsservice != null) {
			jsservice.log(1, "ReasoningTime-Query", logId, null, null, null, "transmitIn", new ArrayList<String>());
		}

		List<Map<String, String>> allResults = new ArrayList<Map<String, String>>();

		if (model == null) {
			long time1 = System.currentTimeMillis();
			model = getOntologyModel(manager, ontology);
			logger.info("model creation time: " + (System.currentTimeMillis() - time1));
		}
		
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

		logger.info("Query: " + (System.currentTimeMillis() - time));
		
		if (jsservice != null) {
			jsservice.log(1, "ReasoningTime-Query", logId, null, null, null, "transmitOut", new ArrayList<String>());
		}

		return allResults;
	}

	@Override
	public List<Set<OWLAxiom>> constructQuery(Collection<String> queries) {
		long time = System.currentTimeMillis();
		
		if (jsservice != null) {
			jsservice.log(1, "ReasoningTime-Query", logId, null, null, null, "transmitIn", new ArrayList<String>());
		}
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
		logger.info("Query: " + (System.currentTimeMillis() - time));
		
		if (jsservice != null) {
			jsservice.log(1, "ReasoningTime-Query", logId, null, null, null, "transmitOut", new ArrayList<String>());
		}
		return allResults;
	}
}
