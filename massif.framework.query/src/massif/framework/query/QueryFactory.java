package massif.framework.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import massif.journal.api.JournalService;

import org.osgi.framework.BundleContext;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public abstract class QueryFactory {
	
	protected OWLOntology ontology;
	protected OWLOntologyManager manager;
	protected OWLReasoner reasoner;
	
	protected Collection<String> selectQueries;
	protected Collection<String> constructQueries;
	
	// Variable used to indicate if all queries need to be executed or stop at the first rule trigger (default true)
	protected boolean executeAll;
	
	// Variable for debugging only. See how long each query takes to execute
	protected boolean verboseQueryExecution;
	
	// Variables we can use for logging
	protected String logId;
	protected JournalService jsservice;	
	
	final static Logger logger = LoggerFactory.getLogger(QueryFactory.class);

	protected QueryFactory(OWLOntology ontology, Collection<String> selectQueries, Collection<String> constructQueries) {
		this.ontology = ontology;
		this.selectQueries = selectQueries;
		this.constructQueries = constructQueries;
		this.manager = ontology.getOWLOntologyManager();
		this.executeAll = true;
		this.verboseQueryExecution = false;
	}
	
	/***
	 * Static Factory Methods for easy creation of the QueryFactory.
	 */
	
	/**
	 * Creates an QueryFactory with an ontology that already has been materialized. This means that all inferred concepts has been calculated.
	 * It also only uses a subset of data to compute the inferences for the dynamic data.
	 * This method utilizes the HermiT reasoner.
	 * @param materializedOntology		The ontology containing the static data an all the inferred data.
	 * @param queries					The queries that should be excuted.
	 * @return							A queryfactory following the above description.
	 */
	public static QueryFactory createPrecomputedSubSetQueryFactoryWithMaterializedOntology(OWLOntology materializedOntology, Collection<String> selectQueries, Collection<String> constructQueries){
		return new PrecomputedSubSetQueryFactory(materializedOntology, selectQueries, constructQueries);
	}
	
	public static QueryFactory createPrecomputedSubSetQueryFactoryWithMaterializedOntology(OWLOntology materializedOntology, Collection<String> selectQueries, Collection<String> constructQueries, JournalService service){
		return new PrecomputedSubSetQueryFactory(materializedOntology, selectQueries, constructQueries, service);
	}
	
	/**
	 * Creates an QueryFactory with an ontology that already has been materialized. This means that all inferred concepts has been calculated.
	 * It also only uses a subset of data to compute the inferences for the dynamic data.
	 * This method utilizes the Pellet reasoner.
	 * @param materializedOntology		The ontology containing the static data an all the inferred data.
	 * @param queries					The queries that should be excuted.
	 * @return							A queryfactory following the above description.
	 */

	public static QueryFactory createPrecomputedPelletQueryFactoryWithMaterilizedOntology(OWLOntology materializedOntology, Collection<String> selectQueries, Collection<String> constructQueries){
		return new PrecomputedPelletQueryFactory(materializedOntology, selectQueries, constructQueries);
	}
	
	/**
	 * Creates an QueryFactory with an ontology that already has ben materialized. This means that all inferred concepts has been calculated.
	 * @param materializedOntology		The ontology containing the static data an all the inferred data.
	 * @param queries					The queries that should be excuted.
	 * @return							A queryfactory following the above description.
	 */
	public static QueryFactory createPrecomputedQueryFactoryWithMaterializedOntology(OWLOntology materializedOntology, Collection<String> selectQueries, Collection<String> constructQueries){
		return new PrecomputedQueryFactory(materializedOntology, selectQueries, constructQueries);
	}
	
	/**
	 * Creates an QueryFactory that will fully materialize the whole ontology an each update.
	 * Standard ontology should not be fully materialized since it will be materialized in each update.
	 * @param materializedOntology		The ontology containing the static data an all the inferred data.
	 * @param queries					The queries that should be excuted.
	 * @return							A queryfactory following the above description.
	 */
	public static QueryFactory createPrecomputedQueryFactoryWithFullMaterializionOntology(OWLOntology ontology, Collection<String> selectQueries, Collection<String> constructQueries){
		return new PrecomputedFullMaterializationQueryFactory(ontology, selectQueries, constructQueries);
	}
	
	
	/**
	 * Creates an QueryFactory with an ontology that still needs to be precomputed. Not all inferrences have been created yet. Upon creating this will be done.
	 * @param standardOntology			The ontology that still needs to be materialized.
	 * @param queries					The queries that should be executed.
	 * @return							A queryfactory following the above description.
	 */
	public static QueryFactory createPrecomputedQueryFactoryWithStandardOntology(OWLOntology standardOntology, Collection<String> selectQueries ,Collection<String> constructQueries){
		Reasoner reasoner = new Reasoner(standardOntology);
		OWLOntologyManager manager = standardOntology.getOWLOntologyManager();
		
		// generate all inferred concepts
		List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		
		// generators.add(new InferredSubClassAxiomGenerator());
		generators.add(new InferredClassAssertionAxiomGenerator());
		InferredOntologyGenerator infGen = new InferredOntologyGenerator(reasoner, generators);
		
		// add them to the current ontology
		infGen.fillOntology(manager.getOWLDataFactory(), standardOntology);
		
		// create new factory
		QueryFactory factory = new PrecomputedSubSetQueryFactory(standardOntology, selectQueries, constructQueries);
		factory.logger.info("Factory ready");
		
		// we already add the reasoner to save memory consumption.
		factory.reasoner = reasoner;
		reasoner.flush();
		
		return factory;
	}
	
	/**
	 * Creates an QueryFactory that does not precompute anything and uses the reasoner at runtime to retrieve and compute the needed data.
	 * @param standardOntology		The used ontology
	 * @param queries				The queries that should be executed.
	 * @return						A QueryFactory following the above description.
	 */
	public static QueryFactory createNaiveQueryFactory(OWLOntology standardOntology, Collection<String> queries, Collection<String> constructQueries){
		return new NaiveQueryFactory(standardOntology,queries,constructQueries);
	}
	
	/**
	 * Creates an QueryFactory that does not perform reasoning when executing the queries.
	 * @param standardOntology		The used ontology
	 * @param queries				The queries that should be executed.
	 * @return						A QueryFactory following the above description.
	 */
	public static QueryFactory createNoReasoningQueryFactory(OWLOntology standardOntology, Collection<String> selectQueries, Collection<String> constructQueries){
		return new NoReasoningQueryFactory(standardOntology,selectQueries, constructQueries);
	}
	
	public static QueryFactory createRDFSReasoningQueryFactory(OWLOntology standardOntology, Collection<String> selectQueries, Collection<String> constructQueries){
		return new RDFSReasoningQueryFactory(standardOntology,selectQueries, constructQueries);
	}
	
	
	/***
	 * Interaction Methods
	 */
	abstract public void update();
	
	/**
	 * Adds new axioms to the ontology and computes its inferences, adds them to the root ontology so its stays fully materialized.
	 * It should be noted that possible changes the new axioms have on the original ontology will not be captured. 
	 * Whereas changes from the orginal ontology onto the new axioms will be taken into account.
	 * @param axioms		The to-be-added axioms.
	 */
	abstract public void addAxioms(Set<OWLAxiom> axioms);
	
	/**
	 * Add axioms and fully rematerializes the whole ontology. This is useful when the new axioms have an influence on the original ontology.
	 */
	abstract public void updateFullMaterialization();
	
	public List<Map<String,String>> query() {
		return query(selectQueries);
	}
	
	public List<Map<String,String>> query(Map<String, String[]> filter) {
		return query(selectQueries, filter);
	}
	
	abstract public List<Map<String,String>> query(Collection<String> queries);
	abstract public List<Map<String,String>> query(Collection<String> queries, Map<String, String[]> filter);
	abstract public List<Set<OWLAxiom>> constructQuery(Collection<String> queries);
	
	public List<Set<OWLAxiom>> constructQuery(){
		return constructQuery(constructQueries);
	}
	
	protected List<Map<String,String>> exec(Model model, String query, Map<String, String[]> filter){
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		
		ResultSet result = sparqlQuery(model, query);
		while (result != null && result.hasNext()) {			
			Map<String, String> tempMap = new HashMap<String, String>();
			
			QuerySolution solution = result.next();
			Iterator<String> it = solution.varNames();
			
			// Iterate over all results
			while (it.hasNext()) {
				String varName = it.next();
				String varValue = solution.get(varName).toString();
				
				boolean found = false;
				// Check if a filter was set
				if (filter != null) {
					// Check if value exists in array
					found = containsValue(filter, varName, varValue);
				}
				
				// Pass the filter
				if (!found) {
					tempMap.put(varName, varValue);
				}
			}
			
			// Only add if we have some objects in temp map
			if (tempMap.size() > 0) {
				results.add(tempMap);
			}
		}
		
		return results;
	}
	
	protected Set<OWLAxiom> execConstruct(Model model, String query){
		Set<OWLAxiom> constructResult = new HashSet<OWLAxiom>();
		Model result = sparqlConstructQuery(model, query);
		OWLOntology resultOnt = getOWLOntology(result);
		resultOnt.axioms().forEach(a -> constructResult.add(a));
		
		return constructResult;
	}
	
	/**
	 * Check if the given key and value exist in given array
	 * @param array				The array you want to check 
	 * @param key				The key that has to exist in the array
	 * @param value				The value that has to exist in the array
	 * @return					True if found
	 */
	private boolean containsValue(Map<String, String[]> array, String key, String value) {
		if (array.containsKey(key)) {
			String[] values = array.get(key);
			// Find value
			for (String v : values) {
				if (v.length() == value.length() && v.equals(value))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Add a filter to the map. If the map is null, it will be a HashMap
	 * @param map				The map you want to add the filter
	 * @param key				The key of the filter
	 * @param value				The value of the filter
	 */
	public Map<String, String[]> addFilterToMap(Map<String, String[]> map, String key, String value) {
		// Make sure map is instantiated
		if (map == null) {
			map = new HashMap<String, String[]>();
		}
		
		// Check if key exists
		if (map.containsKey(key)) {
			// Add value to list
			String[] filter = map.get(key);
			String[] newFilter = new String[filter.length + 1];
			
			for (int i = 0; i < filter.length; i++)
				newFilter[i] = filter[i];
			newFilter[filter.length] = value;
			
			// Replace value in map
			map.put(key, newFilter);
			
			return map;
		}
		
		// A new key needs to be created
		map.put(key, new String[] { value });
		
		return map;
	}
	
	private ResultSet sparqlQuery(Model model, String rule) {
		Query query = com.hp.hpl.jena.query.QueryFactory.create(rule);
		QueryExecution qExec = QueryExecutionFactory.create(query, model);
		return qExec.execSelect();
	}
	public static boolean isCorrectQuery(String query){
		try{
		Query parsedQuery = com.hp.hpl.jena.query.QueryFactory.create(query);
		}catch(Exception e){
			logger.error("Unable to parse query",e);
			return false;
		}
		return true;
	}
	private Model sparqlConstructQuery(Model model, String rule) {
		Query query = com.hp.hpl.jena.query.QueryFactory.create(rule);
		QueryExecution qExec = QueryExecutionFactory.create(query, model);
		return qExec.execConstruct();
	}
	protected static OWLOntology getOWLOntology(final Model model) {
		OWLOntology ontology;
		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is)) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.write(os, "TURTLE", null);
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			ontology = man.loadOntologyFromOntologyDocument(is);
			return ontology;
		} catch (Exception e) {
			throw new RuntimeException("Could not convert JENA API model to OWL API ontology.", e);
		}
	}
	protected OntModel getOntologyModel(OWLOntologyManager manager, OWLOntology ontology){
		OntModel noReasoningModel = null;
		
		noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		noReasoningModel.getDocumentManager().setProcessImports(false);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			manager.saveOntology(ontology, out);
		} catch (OWLOntologyStorageException e) {
			logger.error("Unable to write ontology to stream");
		}
		
		try {
			noReasoningModel.read(new ByteArrayInputStream(out.toByteArray()), "RDF/XML");
		} catch (Exception e) {
			logger.error("Problems reading stream. Might be ignored");
		}
		
		return noReasoningModel;
	}
		
	/***
	 * Getters and setters
	 */
	public OWLReasoner getReasoner(){
		return reasoner;
	}
	
	public void setExecuteAll(boolean value) {
		this.executeAll = value;
	}
	
	public void setVerboseQueryExecution(boolean value) {
		this.verboseQueryExecution = value;
	}
	
	public void setLogId(String id) {
		this.logId = id;
	}
	
	/**
	 * Util
	 */
	protected void saveOntology(OWLOntology ontology, OWLOntologyManager manager, String suffix) {
		String location = "/tmp/orca/";
		try {
			File file = new File(location + "savedontology" + suffix + ".owl");
			file.createNewFile();
			manager.saveOntology(ontology, new N3DocumentFormat(),new FileOutputStream(file));
			//manager.saveOntology(ontology, new RDFXMLOntologyFormat(), IRI.create(file.toURI()));
		} catch (OWLOntologyStorageException | IOException e) {
			System.err.println("could not load ontology from location <" + location + ">");
		}
	}
	/**
	 * This will fetch the rules in the service in a specified path and returns them
	 * @param context			The context of the service
	 */
	public static List<String> readServiceRules(BundleContext context, String path) {
		Enumeration<String> rules = context.getBundle().getEntryPaths(path);
		ArrayList<String> readRules = new ArrayList<String>();
		while (rules != null && rules.hasMoreElements()) {
			String url = rules.nextElement();
			
			// Read data from rule file
			String text = readFile(context, url);
			
			readRules.add(text);
			
		}
		logger.debug("Loaded < " + readRules.size() +"> rules!");
		return readRules;
	}
		
	/**
	 * Read the requested file and return a string
	 * @param context			The context of the bundle
	 * @param path				The path of the file
	 * @return					The read text from the file
	 */
	private static String readFile(BundleContext context, String path) {
		URL url = context.getBundle().getEntry(path);
		StringBuilder text = new StringBuilder();
		
		try {
			Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
			
			// Start reading the file
			int data = reader.read();
			while (data != -1) {
				text.append((char) data);
				data = reader.read();
			}
			
			reader.close();
		} catch (IOException e) {
			logger.error("Could not read the file from location <" + url.toString() + ">. Returning empty file", e);
		}
		
		return text.toString();
	}
}
