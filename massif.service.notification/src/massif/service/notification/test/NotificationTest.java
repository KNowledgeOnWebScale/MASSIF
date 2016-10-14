package massif.service.notification.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import massif.scb.api.OWLMessage;
import massif.service.notification.NotificationService;


public class NotificationTest {

	BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private NotificationService service;
	private TestSink sink;
	@Before
	public void startService(){
		service = new NotificationService();
		service.start(context);
		sink = new TestSink("http://localhost:8080/react");
		sink.setTrasmitting(true);
		service.registerSink(sink);
	}
	
	public void testTurnOnLight() {	
		String lightId = "1";
		OWLMessage msg = createLightAction("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#TurnOnLightAction",lightId);
		service.transmitIn(msg);
		int counter = 0;
		int timeOut = 100;
		while(!sink.isResultIn() && counter < 20){
			try {
				Thread.sleep(timeOut);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
		}
		assertTrue(sink.isResultIn());
		List<Map<String, String>> results = (List<Map<String, String>>) sink.getResponse();
		//check size array
		assertEquals(results.size(), 1, 0);
		Map<String,String> result = results.get(0);
		assertEquals(result.get("type"), "TurnOnLightAction");
		assertTrue(result.get("id").contains(lightId));	
	}
	@Test
	public void testTurnOffLight() {	
		String lightId = "3";
		OWLMessage msg = createLightAction("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#TurnOffLightAction",lightId);
		service.transmitIn(msg);
		int counter = 0;
		int timeOut = 100;
		while(!sink.isResultIn() && counter < 20){
			try {
				Thread.sleep(timeOut);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
		}
		assertTrue(sink.isResultIn());
		List<Map<String, String>> results = (List<Map<String, String>>) sink.getResponse();
		//check size array
		assertEquals(results.size(), 1, 0);
		Map<String,String> result = results.get(0);
		assertEquals(result.get("type"), "TurnOffLightAction");
		assertTrue(result.get("id").contains(lightId));	
	}

	private OWLMessage createLightAction(String actionString, String lightID){
		String prefix = "http://IBCNServices.github.io/Accio-Ontology/SSNiot#";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		OWLClass eventClass = factory.getOWLClass(prefix+"Event");

		OWLNamedIndividual event = factory.getOWLNamedIndividual(prefix+"event");
		OWLObjectProperty hasContextProp = factory.getOWLObjectProperty(prefix+"hasContext");
		OWLClass actionCls = factory.getOWLClass(actionString);
		OWLNamedIndividual action = factory.getOWLNamedIndividual("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#lightOn");
		
		OWLClass lightCls = factory.getOWLClass("http://IBCNServices.github.io/Accio-Ontology/SSNiot#IndicatorLight");
		OWLNamedIndividual lightInd = factory.getOWLNamedIndividual("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#light");
		axioms.add(factory.getOWLClassAssertionAxiom(lightCls, lightInd));
		OWLDataProperty hasIdProp = factory.getOWLDataProperty("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#hasID");
		axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasIdProp, lightInd, lightID));
		
		OWLObjectProperty isActionOfProp = factory.getOWLObjectProperty(prefix + "isActionOf");
		axioms.add(factory.getOWLObjectPropertyAssertionAxiom(isActionOfProp, action, lightInd));
		
		OWLClass hallCls = factory.getOWLClass("http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#Place");
		OWLNamedIndividual hallInd = factory.getOWLNamedIndividual("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#hallway");
		axioms.add(factory.getOWLClassAssertionAxiom(hallCls, hallInd));
		
		OWLObjectProperty hasLocProp = factory.getOWLObjectProperty("http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#hasLocation");
		axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasLocProp, lightInd, hallInd));
		
		axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasContextProp, event, action));
		axioms.add(factory.getOWLClassAssertionAxiom(eventClass, event));
		axioms.add(factory.getOWLClassAssertionAxiom(actionCls, action));
		OWLMessage message = new OWLMessage(event, 0+"");
		message.addAxioms(axioms);
		
		return message;
	}
}
