package massif.adapter.jsonld;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormatFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFXMLParserFactory;
import org.semanticweb.owlapi.rio.RioJsonLDParserFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import massif.contextadapter.api.ContextAdapter;
import massif.framework.dashboard.api.util.Converter;
import massif.framework.util.owl.OntologyCache;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLSemanticCommunicationBus;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/*
 * Converts JsonLD to OWL API axioms
 */
@Component(immediate = true, property = { "tag=jsonld" })
public class JsonLdAdapter implements ContextAdapter {

	private OWLSemanticCommunicationBus bus;
	// Util variables
	protected final ExecutorService workerpool = Executors.newFixedThreadPool(1);
	protected AtomicLong id = new AtomicLong();
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private final String EVENT_IRI = "";
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	private static final JsonFactory JSON_FACTORY = new JsonFactory(JSON_MAPPER);

	final Logger logger = LoggerFactory.getLogger(JsonLdAdapter.class);
	private OWLOntology ontology;

	@Activate
	public void activate(BundleContext context) {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		logger.info("Started");
		try {
			ontology = manager.loadOntologyFromOntologyDocument(IRI.create("http://IBCNServices.github.io/Accio-Ontology/SSNiot.owl"));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void transmitIn(Map<String, Object> in) {
		// TODO Auto-generated method stub
		workerpool.execute(new Runnable() {

			@Override
			public void run() {
				long counter = id.getAndIncrement();
				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

				IRI vcardIri = IRI.create("https://www.w3.org/2006/vcard/ns.jsonld");
				try {
					Set parsers = new HashSet();

					parsers.add(new RioJsonLDParserFactory());
					parsers.add(new RDFXMLParserFactory());
					ontologyManager.setOntologyParsers(parsers);
					String location = "/tmp/massif/";
					//add import
					Map<String,Object> importStatements = new HashMap<String,Object>();
					Map<String,String> importStatement = new HashMap<String,String>();
					importStatement.put("@id", "http://IBCNServices.github.io/Accio-Ontology/SSNiot.owl");
					Map<String,String> importStatement2 = new HashMap<String,String>();
					importStatement2.put("@id", "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn.owl");
					Map<String,String> importStatement3 = new HashMap<String,String>();
					importStatement3.put("@id", "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl");
					Map<String,String> importStatement4 = new HashMap<String,String>();
					importStatement4.put("@id", "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl");
					List<Map> importList = new ArrayList<Map>();
					importList.add(importStatement);
					importList.add(importStatement2);
					importList.add(importStatement3);
					importList.add(importStatement4);

					importStatements.put("@id", "http://www.semanticweb.org/pbonte/ontologies/2016/6/untitled-ontology-25");
					List<String> types = new ArrayList<String>();
					types.add("http://www.w3.org/2002/07/owl#Ontology");
					importStatements.put("@type", types);
					importStatements.put("http://www.w3.org/2002/07/owl#imports",importList);
					List<Map> fullJSONLD = new ArrayList<Map>();
					fullJSONLD.add(in);
					fullJSONLD.add(importStatements);
					OWLOntology ont = ontologyManager
							.loadOntologyFromOntologyDocument(new ByteArrayInputStream(toPrettyString(fullJSONLD).getBytes()));
					//manager.saveOntology(ont, new N3DocumentFormat(), new FileOutputStream(new File(location + "savedontology" + "jsonld" + ".owl")));
					OWLNamedIndividual eventInd = null;
					Set<OWLAxiom> axioms = ont.axioms().collect(Collectors.toSet());
					for (OWLAxiom ax : axioms) {
						if (ax.toString().endsWith("Observation>)") && ax instanceof OWLClassAssertionAxiom) {
							eventInd = ax.getIndividualsInSignature().iterator().next();
						}
						
					}

					OWLMessage message = new OWLMessage(eventInd, counter + "");
					axioms = Converter.checkForIncorrectAnnotations(axioms,ontology);
					for(OWLAxiom ax: axioms){
						if(ax instanceof OWLClassAssertionAxiom){
							OWLClassAssertionAxiom classAss = (OWLClassAssertionAxiom)ax;
							OWLDeclarationAxiom decAx = manager.getOWLDataFactory().getOWLDeclarationAxiom(classAss.getIndividual().asOWLNamedIndividual());
							message.addAxiom(decAx);
						}
					}
					ont.axioms().forEach(a -> message.addAxiom(a));
					logger.info(message.getAxioms() + "");
					transmitOut(message, counter + "");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

	}

	public static String toPrettyString(Object jsonObject) throws JsonGenerationException, IOException {
		final StringWriter sw = new StringWriter();
		writePrettyPrint(sw, jsonObject);
		return sw.toString();
	}

	public static void writePrettyPrint(Writer writer, Object jsonObject) throws JsonGenerationException, IOException {
		final JsonGenerator jw = JSON_FACTORY.createGenerator(writer);
		jw.useDefaultPrettyPrinter();
		jw.writeObject(jsonObject);
	}

	@Override
	public void transmitOut(OWLMessage message, String packetID) {
		// TODO Auto-generated method stub
		bus.publish(message, packetID);
	}

	@Reference(unbind = "unbindOWLSemanticCommunicationBus")
	@Override
	public void bindOWLSemanticCommunicationBus(OWLSemanticCommunicationBus bus) {
		// TODO Auto-generated method stub
		this.bus = bus;
	}

	@Override
	public void unbindOWLSemanticCommunicationBus(OWLSemanticCommunicationBus bus) {
		// TODO Auto-generated method stub

	}


}
