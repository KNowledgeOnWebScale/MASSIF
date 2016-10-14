package massif.service.notification;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.exceptions.NoBusFoundException;
import massif.framework.dashboard.api.Sink;
import massif.framework.query.QueryFactory;
import massif.framework.util.owl.OntologyCache;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;
import massif.watchdog.api.WatchdogEventService;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

@Component
public class NotificationService implements MCIService {

	private OWLSemanticCommunicationBus bus;
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private OWLOntologyManager manager;
	private ArrayList<String> queries = new ArrayList<String>();
	private QueryFactory qFactory;
	private OWLOntology ontology;

	private final String ONT_IRI = "http://IBCNServices.github.io/Accio-Ontology/demo/sensors.owl";
	private final String ONT_FILE = "iotdemo.owl";
	private Sink sink;

	@Activate
	public void start(BundleContext context) {
		this.manager = OWLManager.createOWLOntologyManager();
		URL url = context.getBundle().getEntry("test.query");
		URL fileURL = context.getBundle().getEntry(ONT_FILE);
		try {
			String template = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			queries.add(template);
			OntologyCache cache = OntologyCache.getOntologyCache();
			manager = OWLManager.createOWLOntologyManager();
			ontology = cache.getOntology(ONT_IRI, manager);
			OWLOntology abox = manager.loadOntologyFromOntologyDocument(fileURL.openStream());
			manager.addAxioms(ontology, abox.axioms());
			qFactory = QueryFactory.createRDFSReasoningQueryFactory(ontology, queries, null);
		} catch (Exception e) {
			logger.error("Could not load ontology <" + ONT_IRI + "> from cache.", e);
		}
	}

	@Override
	public void transmitIn(OWLMessage message) {
		// TODO Auto-generated method stub
		logger.info("Received message");
		logger.info("Triggered filters: " + message.getTriggeredFilters().toString());
		logger.info("Received axioms: " + message.getAxioms().toString());

		try {
			manager.addAxioms(ontology, message.getAxioms().stream());
			// 1) Perform filtering on the received message if necessary
			// your code here
			// 2) add the (filtered) axioms to the local ontology
			System.out.println(message.getAxioms());
			// 3) Execute queries
			List<Map<String, String>> results = qFactory.query();
			System.out.println("##Query results:");
			for (Map<String, String> queryResult : results) {
				System.out.println(queryResult);

			}
			if(!results.isEmpty()){
				sink.send(prepareOutput(results));	
			}
			

			manager.removeAxioms(ontology, message.getAxioms().stream());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

					newAxioms.add(
							manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objProp, subject, object));
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

	@Override
	public void transmitOut(OWLMessage message) throws NoBusFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void transmitOut(OWLMessage message, String packetID) throws NoBusFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerBus(OWLSemanticCommunicationBus bus) {
		// TODO Auto-generated method stub
		this.bus = bus;
	}

	public OWLMessageFilter createCallFilter() {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String prefix = "http://massif.example.owl#";
		OWLClass owlFilter = factory.getOWLClass(prefix + "NotificationFilter");

		

		OWLMessageFilter filter = new OWLMessageFilter(owlFilter);

		 

		OWLClass observationClass = factory
				.getOWLClass("http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#Action");
		filter.addAxiom(factory.getOWLEquivalentClassesAxiom(owlFilter, observationClass));

		return filter;
	}

	@Override
	public List<OWLMessageFilter> getFilter() {
		// TODO Auto-generated method stub
		List<OWLMessageFilter> filters = new ArrayList<OWLMessageFilter>();
		filters.add(createCallFilter());
		return filters;
	}

	@Override
	public void registerWatchdog(WatchdogEventService watchdog) {
		// TODO Auto-generated method stub

	}

	public Map<String, Object> convertBindings(Map<String, String> bindings) {
		Map<String, Object> result = new HashMap<String, Object>();
		return null;
	}

	@Reference(unbind = "unregisterSink", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void registerSink(Sink sink) {
		logger.info("Registering sink");
		this.sink = sink;
	}

	public void unregisterSink(Sink sink) {
		if (sink == this.sink) {
			logger.info("Unregistering sink");
			this.sink = null;
		}

	}

	/**
	 * Replaces IRI's to values by replacing the values of the map.
	 * 
	 * @param input
	 *            to be translated list with map of results
	 * @return translated results
	 */
	public List<Map<String, String>> prepareOutput(List<Map<String, String>> input) {
		for (Map<String, String> result : input) {
			for (String key : result.keySet()) {
				result.put(key, removeIRI(result.get(key)));
			}
		}
		return input;
	}

	public String removeIRI(String value) {
		if (value.contains("^^")) {
			value = value.substring(0,value.indexOf("^^"));
		} else if (value.contains("#")) {
			value = value.substring(value.indexOf("#")+1);

		}
		return value;
	}

}
