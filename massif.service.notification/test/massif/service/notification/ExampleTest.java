package massif.service.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.BundleContext;

import junit.framework.TestCase;

public class ExampleTest extends TestCase{

	private final BundleContext context = new BundleContextDummy("resources/");
	@Test
	public void test() {
		NotificationService not = new NotificationService();
		String removed = not.removeIRI("105^^http://www.w3.org/2001/XMLSchema#string");
		assertEquals(removed, "105");
		removed = not.removeIRI("http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#TurnOnLightAction");
		assertEquals(removed, "TurnOnLightAction");
		List results = new ArrayList<Map<String,String>>();
		Map<String,String> input = new HashMap<String,String>();
		input.put("id", "http://IBCNServices.github.io/Accio-Ontology/iotdemo.owl#TurnOnLightAction");
		input.put("key", "105^^http://www.w3.org/2001/XMLSchema#string");
		results.add(input);
		
		List expected = new ArrayList<Map<String,String>>();
		Map<String,String> expectedMap = new HashMap<String,String>();
		expectedMap.put("id", "TurnOnLightAction");
		expectedMap.put("key", "105");
		expected.add(expectedMap);
		
		assertEquals(expected, not.prepareOutput(expected));
		
	}

}
