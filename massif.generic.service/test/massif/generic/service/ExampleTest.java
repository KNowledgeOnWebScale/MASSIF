package massif.generic.service;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class ExampleTest {

	@Test
	public void test() {
		String input = "  <http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#Observation>"
         +"and (<http://IBCNServices.github.io/Accio-Ontology/SSNiot#hasSymptom> some <http://IBCNServices.github.io/Accio-Ontology/SSNiot#Symptom>)";
		//String out = input.replaceAll("<[^ ]*>", "");
		String out = input.replaceAll(" (?!<)*", "");
		Set<String> entities = new HashSet<String>();
		Matcher m = Pattern.compile("<([^>]+)>").matcher(input);
	     while(m.find()) {
	       entities.add(m.group(1));    
	     }
		System.out.println(out);
		fail("Not yet implemented");
	}

}
