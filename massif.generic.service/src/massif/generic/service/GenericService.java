package massif.generic.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.exceptions.NoBusFoundException;
import massif.framework.dashboard.api.AdaptableService;
import massif.framework.query.QueryFactory;
import massif.framework.util.owl.OntologyCache;
import massif.generic.service.util.FilterRuleParser;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLMessageFilter.Validity;
import massif.scb.api.OWLSemanticCommunicationBus;
import massif.watchdog.api.WatchdogEventService;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

@Component(immediate = true, property = { "factory.pid=massif.generic.service.GenericService" }, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class GenericService implements MCIService, AdaptableService {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<OWLMessageFilter> filters = new ArrayList<OWLMessageFilter>();
	private List<String> queries = new ArrayList<String>();
	private final ExecutorService workerpool = Executors.newFixedThreadPool(1);
	private OWLOntologyManager manager;
	private Dictionary<String, Object> properties;
	private OWLOntology ontology;
	private QueryFactory qFactory;
	private OWLSemanticCommunicationBus scb;
	private String template;

	@Activate
	public void activate(ComponentContext context) {
		properties = context.getProperties();
		// load ontology
		OntologyCache cache = OntologyCache.getOntologyCache();
		manager = OWLManager.createOWLOntologyManager();
		try {
			ontology = cache.getOntology(properties.get("ontology").toString(), manager);
			// Construct input filters
			URL url = context.getBundleContext().getBundle().getEntry("FilterRule.owlTemplate");
			this.template = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			List<String> inputList = extractListFromProperty(properties, "input");
			List<String> inputNameList = extractListFromProperty(properties, "inputName");
			if (inputList.size() != inputNameList.size()) {
				logger.error("Unequal amount of input and input names!");
				return;
			}
			for (int i = 0; i < inputList.size(); i++) {
				filters.add(FilterRuleParser.parseFilterRules(template, properties.get("ontology").toString(), inputNameList.get(i), inputList.get(i)));
			}
		} catch (IOException e) {
			logger.error("Could not read file", e);
		} catch (OWLOntologyCreationException e) {
			logger.error("Could not load ontology <" + properties.get("ontology").toString() + "> from cache.", e);
		}
		// load SPARQL queries
		for (String query : extractListFromProperty(properties, "query")) {
			if (QueryFactory.isCorrectQuery(query)) {
				queries.add(query);
			}
		}
		try {
			qFactory = QueryFactory.createNoReasoningQueryFactory(ontology, null, queries);
			logger.info("Activated " + context.getProperties());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void registerWatchdog(WatchdogEventService watchdog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transmitIn(OWLMessage message) {
		logger.info(getName() + ": Received event");
		workerpool.execute(new Runnable() {

			@Override
			public void run() {
				// 0) check validity of the incomming message
				Validity validity = getValidity(message);
				// 1) Perform filtering on the received message if necessary
				// your code here
				// check which axioms already belong to the ontology so these
				// will not be removed
				Set<OWLAxiom> dontRemove = null;
				Set<OWLAxiom> doRemove = null;
				if (validity == Validity.VOLATILE) {
					dontRemove = new HashSet<OWLAxiom>();
					for (OWLAxiom ax : message.getAxioms()) {
						if (ontology.containsAxiom(ax)) {
							dontRemove.add(ax);
						}
					}
				}
				if (validity == Validity.TEMPORAL) {
					doRemove = new HashSet<OWLAxiom>();
					for (OWLAxiom ax : message.getAxioms()) {
						if (ax instanceof OWLObjectPropertyAssertionAxiom) {
							OWLObjectPropertyAssertionAxiom objAx = (OWLObjectPropertyAssertionAxiom) ax;
							// remove the object if equal subject and property

							for (OWLIndividual object : EntitySearcher.getObjectPropertyValues(objAx.getSubject(), objAx.getProperty(), ontology).collect(Collectors.toSet())) {
								OWLObjectPropertyAssertionAxiom rmAx = manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objAx.getProperty(), objAx.getSubject(), object);
								doRemove.add(rmAx);

							}
						}
						if (ax instanceof OWLDataPropertyAssertionAxiom) {
							OWLDataPropertyAssertionAxiom dataAx = (OWLDataPropertyAssertionAxiom) ax;
							for (OWLLiteral lit : EntitySearcher.getDataPropertyValues(dataAx.getSubject(), dataAx.getProperty(), ontology).collect(Collectors.toSet())) {
								OWLDataPropertyAssertionAxiom rmAx = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dataAx.getProperty(), dataAx.getSubject(), lit);
								doRemove.add(rmAx);

							}
						}
					}
					//remove overlapping props
					manager.removeAxioms(ontology, doRemove.stream());
				}
				// 2) add the (filtered) axioms to the local ontology
				System.out.println(message.getAxioms());
				manager.addAxioms(ontology, message.getAxioms().stream());
				// 3) Execute queries
				Set<String> queris = new HashSet<String>();
				queris.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX ssn: <http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#>\nPREFIX dul: <http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX iot: <http://IBCNServices.github.io/Accio-Ontology/SSNiot#>\nPREFIX upper: <http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\nPREFIX demo: <http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#>\nPREFIX ical: <http://www.w3.org/2002/12/cal/ical#>\n"


+"Select * \n"
+"WHERE {	"
+ "?observation rdf:type iot:RFIDTagObservation. "
+ "	?observation ssn:observationResult ?obresult. "
+ "	?obresult ssn:hasValue ?obvalue. "
+ "	?obvalue dul:hasParameterDataValue \"false\"^^xsd:boolean. "
+ "	?observationdoor rdf:type iot:DoorWindowContactObservation. "
+ "	?observationdoor ssn:observationResult ?obresultdoor. "
+ "	?obresultdoor ssn:hasValue ?obvaluedoor. "
+ "	?obvaluedoor dul:hasDataValue \"false\"^^xsd:boolean. "
+ "	?observationdoor ssn:observedBy ?doorSensor2."
+ "	?doorSensor2 dul:hasLocation ?location."
+ "	?light rdf:type iot:LightSensor. "
+ "	?light dul:hasLocation ?location2. "
+ "	?light upper:hasID ?lightID. "
+ "	FILTER NOT EXISTS{ "
+ "		?observation2 rdf:type iot:RFIDTagObservation. " 
+ "		?observation2 ssn:observationResult ?obresult2. "
+ "		?obresult2 ssn:hasValue ?obvalue2. "
+ "		?obvalue2 dul:hasParameterDataValue \"true\"^^xsd:boolean. " 
	+"}"		
+"}"
						);
				System.out.println("Query results: "+qFactory.query(queris));
				//System.out.println(queris);
				List<Set<OWLAxiom>> results = qFactory.constructQuery();
				saveOntology(ontology, manager, "genericService");
				System.out.println("##Query results:");
				for (Set<OWLAxiom> queryResult : results) {
					Set<OWLAxiom> cleanedResult = checkForIncorrectAnnotations(queryResult);
					System.out.println(cleanedResult);
					// construct message
					OWLNamedIndividual event = extractEvent(cleanedResult);
					OWLMessage msg = new OWLMessage(event, message.getPacketID());
					msg.addAxioms(cleanedResult);
					// transmit it to the SCB
					
					try {
						transmitOut(msg);
					} catch (NoBusFoundException e) {
						logger.error("Bus not active", e);
					}
				}
				if (validity == Validity.VOLATILE) {
					message.getAxioms().removeAll(dontRemove);
					manager.removeAxioms(ontology, message.getAxioms().stream());
				}

			}
		});
	}

	private Validity getValidity(OWLMessage message) {
		Validity validity = null;
		for (String triggeredFilters : message.getTriggeredFilters()) {
			for (OWLMessageFilter filter : filters) {
				if (filter.getOWLFilter().toStringID().equals(triggeredFilters)) {
					validity = filter.getValidity();
					break;
				}
			}
		}
		return validity;
	}

	private OWLNamedIndividual extractEvent(Set<OWLAxiom> axioms) {
		OWLNamedIndividual result = null;
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom clsAx = (OWLClassAssertionAxiom) ax;
				if (clsAx.getClassExpression().toString().contains("#Event")) {
					result = clsAx.getIndividual().asOWLNamedIndividual();
					break;
				}
			}
		}
		return result;
	}

	private Set<OWLAxiom> checkForIncorrectAnnotations(Set<OWLAxiom> axioms) {
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLAnnotationAssertionAxiom) {
				OWLAnnotationAssertionAxiom anno = (OWLAnnotationAssertionAxiom) ax;
				OWLIndividual subject = new OWLNamedIndividualImpl(anno.getSubject().asIRI().get());
				if (ontology.containsObjectPropertyInSignature(anno.getProperty().getIRI())) {
					OWLIndividual object = new OWLNamedIndividualImpl(anno.getValue().asIRI().get());
					OWLObjectProperty objProp = new OWLObjectPropertyImpl(anno.getProperty().getIRI());

					newAxioms.add(manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objProp, subject, object));
				} else if (ontology.containsDataPropertyInSignature(anno.getProperty().getIRI())) {
					OWLDataProperty dataProp = new OWLDataPropertyImpl(anno.getProperty().getIRI());
					OWLLiteral lit = anno.getValue().asLiteral().get();

					newAxioms.add(manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dataProp, subject, lit));
				} else {
					newAxioms.add(ax);
				}
			} else {
				newAxioms.add(ax);
			}
		}

		return newAxioms;
	}

	@Override
	public void transmitOut(OWLMessage message) throws NoBusFoundException {
		// TODO Auto-generated method stub
		if (scb != null) {
			scb.publish(message, message.getPacketID());
		} else {
			throw new NoBusFoundException();
		}
	}

	@Override
	public void transmitOut(OWLMessage message, String packetID) throws NoBusFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerBus(OWLSemanticCommunicationBus bus) {
		this.scb = bus;

	}

	@Override
	public List<OWLMessageFilter> getFilter() {
		// TODO Auto-generated method stub
		return filters;
	}

	@Override
	public List<String> getQueries() {
		// TODO Auto-generated method stub
		return queries;
	}

	@Override
	public boolean removeQuery(int index) {
		if (index < queries.size()) {
			queries.remove(index);
			return true;
		}
		return false;
	}

	@Override
	public boolean addQuery(String query) {
		// check if query can be parsed
		if (QueryFactory.isCorrectQuery(query)) {
			queries.add(query);
			return true;
		}
		return false;

	}

	@Override
	public List<String> getFilterRules() {
		// TODO Auto-generated method stub
		List<String> filtersString = new ArrayList<String>();
		for (OWLMessageFilter msgFilter : filters) {
			filtersString.add(msgFilter.getAxioms().toString());
		}
		return filtersString;
	}

	@Override
	public boolean removeFilterRule(int index) {
		System.out.println("removing filter");
		if (index < filters.size()) {
			OWLMessageFilter rmFilter = filters.remove(index);
			scb.unsubscribe(this, rmFilter);
			return true;
		} else {
			return false;
		}

	}

	@Override
	public boolean addFilterRule(String filterName, String filterRule) {
		System.out.println("adding filter");
		OWLMessageFilter addFilter = FilterRuleParser.parseFilterRules(template, properties.get("ontology").toString(), filterName, filterRule);
		filters.add(addFilter);
		scb.subscribe(this, addFilter);

		return false;
	}

	/*
	 * @Override public void updated(Dictionary<String, ?> properties) throws
	 * ConfigurationException { logger.info("Received updated :"+properties);
	 * this.properties = (Dictionary<String, Object>) properties; }
	 */
	@Override
	public String getName() {
		if (properties != null) {
			return (String) properties.get("name");
		}
		return null;
	}

	private void saveOntology(OWLOntology ontology, OWLOntologyManager manager, String suffix) {
		String location = "/tmp/massif/";
		try {
			File file = new File(location + "savedontology" + suffix + ".owl");
			if (!file.canExecute()) {
				File mkdir = new File(location);
				mkdir.mkdirs();
			}
			file.createNewFile();
			manager.saveOntology(ontology, new N3DocumentFormat(), new FileOutputStream(file));
		} catch (OWLOntologyStorageException | IOException e) {
			logger.error("could not write ontology to location <" + location + ">", e);
		}

	}

	private List<String> extractListFromProperty(Dictionary properties, String property) {
		ArrayList<String> extractedList = new ArrayList<String>();
		Enumeration<String> enumeration = properties.keys();
		while (enumeration.hasMoreElements()) {
			String next = enumeration.nextElement();
			if (next.matches(property) || next.matches(property + "\\.[0-9]+")) {
				extractedList.add(properties.get(next).toString());
			}
		}

		return extractedList;
	}

}
