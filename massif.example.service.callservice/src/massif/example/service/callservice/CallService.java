package massif.example.service.callservice;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rio.RioJsonLDParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.exceptions.NoBusFoundException;
import massif.framework.dashboard.api.AdaptableService;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;
import massif.watchdog.api.WatchdogEventService;

@Component
public class CallService implements MCIService, AdaptableService{

	private OWLSemanticCommunicationBus bus;
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private OWLOntologyManager manager;
	private ArrayList<String> queries = new ArrayList<String>();
	@Activate
	public void start(){
		this.manager = OWLManager.createOWLOntologyManager();
		queries.add("TestQuery");
	}

	@Override
	public void transmitIn(OWLMessage message) {
		// TODO Auto-generated method stub
		logger.info("Received message");
		logger.info("Triggered filters: " + message.getTriggeredFilters().toString());
		logger.info("Received axioms: " + message.getAxioms().toString());
		
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

	public OWLMessageFilter createCallFilter(){
		OWLDataFactory factory = manager.getOWLDataFactory();
		String prefix = "http://massif.example.owl#";
		OWLClass owlFilter = factory.getOWLClass(prefix+"CallFilter");
		
		OWLObjectProperty hasContextProp = factory.getOWLObjectProperty(prefix+"hasContext");
		OWLClass callClass = factory.getOWLClass(prefix+"Call");	

		OWLMessageFilter filter = new OWLMessageFilter(owlFilter);	
		
		filter.addAxiom(
				factory.getOWLSubClassOfAxiom(
						owlFilter,
						factory.getOWLObjectSomeValuesFrom(
								hasContextProp,
								callClass
						)
				)
		);

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

	@Override
	public List<String> getQueries() {
		// TODO Auto-generated method stub
		return queries;
	}

	@Override
	public boolean removeQuery(int index) {
		// TODO Auto-generated method stub
		if(index<queries.size()){
			queries.remove(index);
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean addQuery(String query) {
		queries.add(query);
		return true;
	}

	@Override
	public List<String> getFilterRules() {
		// TODO Auto-generated method stub
		ArrayList<String> filterRules = new ArrayList<String>();
		for(OWLMessageFilter filter: getFilter()){
			filterRules.add(filter.getAxioms().toString());
		}
		return filterRules;
	}

	@Override
	public boolean removeFilterRule(int index) {
		// TODO Auto-generated method stub
		return false;
	}

	

	@Override
	public boolean addFilterRule(String filterName,String filterRule) {
		
		//logger.info("Adding new filter "+filterRule);
		 OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

	        try {
				OWLOntology ont = ontologyManager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(filterRule.getBytes(StandardCharsets.UTF_8)));
				ont.axioms().forEach(System.out::println);;
	        } catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Presence Service";
	}

	// TODO: class provided by template

}
