package massif.scb.cache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import massif.framework.util.owl.CascadingEntityChecker;
import massif.scb.api.OWLMessageFilter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import com.google.common.collect.Multimap;


public class SCBCache {
	// Stores Level 1 of Cache
	private TreeMap<String, List<LevelTwo>> cache;
	private boolean enabled;

	public SCBCache() {
		cache = new TreeMap<String, List<LevelTwo>>();
		enabled = true;
	}
	public SCBCache(boolean enableCache){
		cache = new TreeMap<String, List<LevelTwo>>();
		enabled = enableCache;
	}
	public void setEnableCache(boolean enableCache){
		this.enabled = enableCache;
	}
	/**
	 * Checks for Filter rules in the cache based on arrived event.
	 * @param levelOne	Filter level of the arrived event (Event hasContext LEVELONE)
	 * @param entities	Contains the triples that need to be checked to be able to speak about a hit or miss.
	 * @param filter	The IRI (String) of the Filter rule
	 */
	/*public void addToCache(String levelOne, List<OWLEntity> entities, String filter) {
		if (!cache.containsKey(levelOne)) {
			// check if level one is already presented in cache
			ArrayList<LevelTwo> levelTwo = new ArrayList<LevelTwo>();
			LevelTwo levelTwoEntry = new LevelTwo(entities, filter);
			levelTwo.add(levelTwoEntry);
			cache.put(levelOne, levelTwo);
		} else {
			// Add new entry
			List<LevelTwo> levelTwos = cache.get(levelOne);
			LevelTwo levelTwoEntry = new LevelTwo(entities, filter);
			levelTwos.add(levelTwoEntry);
		}
	}*/

	public void addToCache2(String levelOne, OWLClassExpression entities, String filter) {
		if(enabled){
			if (!cache.containsKey(levelOne)) {
				// check if level one is already presented in cache
				ArrayList<LevelTwo> levelTwo = new ArrayList<LevelTwo>();
				LevelTwo levelTwoEntry = new LevelTwo(entities, filter);
				levelTwo.add(levelTwoEntry);
				cache.put(levelOne, levelTwo);
			} else {
				// Add new entry
				List<LevelTwo> levelTwos = cache.get(levelOne);
				LevelTwo levelTwoEntry = new LevelTwo(entities, filter);
				levelTwos.add(levelTwoEntry);
			}
		}
	}
	/**
	 * Checks if the arrived message is contained in the cache.
	 * 
	 * @param levelOne	Level one of the arrived message (Event hasContext LEVELONE)
	 * @param message	Set of axioms from the arrived event
	 * @return	Returns String of the Filter rule match, Null if miss
	 */
	/*public Set<String> checkCache(String levelOne, Set<OWLAxiom> message) {
		Set<String> filters = new HashSet<String>();
		if (cache.containsKey(levelOne)) {
			// found level one match
			// Loop level two, all entries need to be checked since a event can match multiple filter rules			
			for (LevelTwo levelTwoEntry : cache.get(levelOne)) {
				List<OWLEntity> processedEntities = levelTwoEntry.getEntities();
				boolean foundAll = false;
				//only start from index 2 since index 0 = Event and index 1 = hasContext
				for (int i = 2; i < processedEntities.size() ; i++) {
					boolean foundAx = false;
					for (OWLAxiom ax : message) {
						if (ax.toString().contains(processedEntities.get(i).toString())) {
							foundAx = true;
							System.out.println("Found " + processedEntities.get(i));
							break;
						}
					}
					if (!foundAx) {
						System.out.println("did not find " + processedEntities.get(i));
					}else{
						foundAll = true;
					}
				}
				if(foundAll){
					//filter rule has been found, let's add it to the result set.
					String filter = levelTwoEntry.getFilter();
					System.out.println("Found filter " + filter);
					filters.add(filter);
					//return filter;
				}
			}
		}
		return filters.isEmpty() ? null : filters;
	}*/
	
	public Set<String> checkCache(String levelOne, OWLIndividual event , OWLOntology ontology, OWLOntologyManager manager) {
		if(enabled){
			Set<String> filters = new HashSet<String>();
			if (cache.containsKey(levelOne)) {
				// found level one match
				// Loop level two, all entries need to be checked since a event can match multiple filter rules			
				for (LevelTwo levelTwoEntry : cache.get(levelOne)) {
					OWLClassExpression processedEntities = levelTwoEntry.getEntities();
					EventCheckerVisitor checkVisitor = new EventCheckerVisitor(ontology, event);
					processedEntities.accept(checkVisitor);
					if(checkVisitor.isMatch()){
						filters.add(levelTwoEntry.getFilter());
					}
				}
			}
			return filters.isEmpty() ? null : filters;
		}else{
			return null;
		}
	}
	/**
	 * Proprocesses the arrived event and looks for the links between the filter rule and the event.
	 * @param message	Set of axioms from the arrived event
	 * @param event		A pointer to the root of the event
	 * @param ontology	Ontology containing the event
	 * @param manager	OWLManger to manage the ontology
	 * @param filter	The filter rule
	 * @return	Returns a list of triples that can be checked against an arriving message. If all these triples are present in the message, it matches the filter rule.
	 */
	public List<OWLEntity> preProcessing(Set<OWLAxiom> message, OWLIndividual event , OWLOntology ontology, OWLOntologyManager manager, String filter){
		OWLDataFactory dFactory = manager.getOWLDataFactory();
		OWLClassExpression clsExp = EntitySearcher.getEquivalentClasses(dFactory.getOWLClass(filter), ontology).collect(Collectors.toSet()).iterator().next();
		List processedFilterEntities = processFilter(clsExp);
		List<OWLEntity> processedMessage = processMessage(processedFilterEntities, ontology, event,null);
		return processedMessage;
	}
	
	public OWLClassExpression preProcessing2(Set<OWLAxiom> message, OWLIndividual event , OWLOntology ontology, OWLOntologyManager manager, String filter,  OWLReasoner reasoner){
		if(enabled){
			OWLDataFactory dFactory = manager.getOWLDataFactory();
			MyOWLClassExpressionVisitor visitor = new MyOWLClassExpressionVisitor(ontology, event, reasoner);
			OWLClassExpression filterExp = EntitySearcher.getEquivalentClasses(dFactory.getOWLClass(filter), ontology).collect(Collectors.toSet()).iterator().next();
			filterExp.accept(visitor);
			return visitor.constructOWLFilter();
		}else
			return null;
	}
	/**
	 * Processes the filter rule: splits filter rule in usefull triples
	 * @param filter	The filter rule
	 * @return	List of useful triples
	 */
	private  List processFilter(OWLClassExpression filter){
		MyOWLClassExpressionVistior visitor = new MyOWLClassExpressionVistior();
		filter.accept(visitor);
		return visitor.getEntityList();
	}
	/**
	 * Maps the message to the filter rule and extract useful triples
	 * @param entities	useful triples from filter rule
	 * @param ontology	ontology containing the message
	 * @param event		pointer to root of the message
	 * @return			returns a list of useful triples that are contained in the message and that indicate a match with the filter rule.
	 */
	private List<OWLEntity> processMessage(List<OWLEntity> entities, OWLOntology ontology, OWLIndividual event, OWLReasoner reasoner){
		ArrayList<OWLEntity> processedEntities = new ArrayList<OWLEntity>();
		processedEntities.add(entities.get(0));
		for(int i = 1; i < entities.size() ; i++){
			//System.out.println(entities.get(i));
			if(! (entities.get(i) instanceof OWLClass))
				processedEntities.add(entities.get(i));
			Multimap<OWLObjectPropertyExpression, OWLIndividual> objExpressions = EntitySearcher.getObjectPropertyValues(event, ontology);
			boolean found = false;
			for(OWLObjectPropertyExpression exObj: objExpressions.keySet()){
				if(exObj.toString().equals(entities.get(i).toString())){
					event = objExpressions.get(exObj).iterator().next();
					
					Set<OWLClassExpression> classes = EntitySearcher.getTypes(event, ontology).collect(Collectors.toSet());
					System.out.println("Type " + classes);
					if(!classes.isEmpty()){
						processedEntities.add(classes.iterator().next().asOWLClass());
					}else{
						NodeSet<OWLClass> classesReasoner = reasoner.getTypes(event.asOWLNamedIndividual(), true);
						System.out.println("Type(reasoner) " + classesReasoner);
						processedEntities.add(classesReasoner.getFlattened().iterator().next());
					}
					found = true;
				}else{
				}
			}
			/*if(!found){
				
				for(OWLClassExpression cls: event.getTypes(ontology)){
					if(cls.toString().trim().equals(entities.get(i).toString().trim())){
						//System.out.println("but found it as class");
						//processedEntities.add(entities.get(i));
					}
				}
			}*/
		}
		return processedEntities;
	}
	/**
	 * Retrieves the level one class from the arrived message contained in the ontology object.
	 * Level one class is defined as: Event <objectProperty> LEVELONE.
	 * @param ontology		Ontology object containing the arrived message.
	 * @param event			Pointer to the root of the message
	 * @param objectProp	String presentation of the property defining the level one relation
	 * @return				Returns level one class from the arrvied message contained in the ontology object.
	 */
	public String getLevelOneType(OWLOntology ontology, OWLIndividual event, String objectProp){
		if(enabled){
			OWLDataFactory dFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
			Multimap<OWLObjectPropertyExpression, OWLIndividual> objectProps = EntitySearcher.getObjectPropertyValues(event, ontology);
			for(OWLObjectPropertyExpression objExp: objectProps.keySet()){
				if(objExp.toString().contains(objectProp)){
					Collection<OWLIndividual> contextSet = objectProps.get(objExp);
					for(OWLIndividual context: contextSet){					
						for(OWLClassExpression cls: EntitySearcher.getTypes(context, ontology).collect(Collectors.toSet())){
							return cls.toString().trim();
						}
					}
					
				}
			}
		}
		return null;
	}
}
