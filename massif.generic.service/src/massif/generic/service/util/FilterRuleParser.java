package massif.generic.service.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxOntologyParser;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxOntologyParserFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFXMLParserFactory;
import org.semanticweb.owlapi.rio.RioJsonLDParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.scb.api.OWLMessageFilter;

public class FilterRuleParser {
	final static Logger logger = LoggerFactory.getLogger(FilterRuleParser.class);

	/**
	 * Parses a textual input filter (in OWL Manchester Syntax) to a
	 * OWLMessageFilter.
	 * 
	 * @param template
	 *            Represents the template where the ontology details will be
	 *            parsed in.
	 * @param ontologyIRI
	 *            The iri of the ontology that describes the concepts in the
	 *            defined input filter.
	 * @param filterName
	 *            The class name of the new input filter.
	 * @param filterRule
	 *            The definition of the input filter.
	 * @return An OWLMessageFilter conform to the textual provided input filter.
	 */
	public static OWLMessageFilter parseFilterRules(String template, String ontologyIRI, String filterName, String filterRule) {
		OWLMessageFilter filter = null;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// extract all OWL statements
		Set<String> entities = new HashSet<String>();
		Matcher m = Pattern.compile("<([^>]+)>").matcher(filterRule);
		while (m.find()) {
			entities.add(m.group(1));
		}
		// add corresponding Class: ObjectProperty or DataProperty
		// load ontology
		String input = template;
		try {
			OWLOntology ont = manager.loadOntology(IRI.create(ontologyIRI));
			OWLDataFactory dfact = manager.getOWLDataFactory();
			String importStr = ontologyIRI;
			// for(OWLOntology importOnt :
			// ont.imports().collect(Collectors.toSet())){
			// importStr+= "\nImport:
			// <"+importOnt.getOntologyID().getOntologyIRI().get()+">";
			// }
			input = input.replace("@IRI@", importStr) + "\n";

			// add the used entities in the class expression as classes or
			// properties (otherwise the parsing fails...)
			Set<OWLOntology> onts = ont.imports().collect(Collectors.toSet());
			onts.add(ont);
			for (OWLOntology importOnt : onts) {
				for (String ent : entities) {
					if (importOnt.containsClassInSignature(IRI.create(ent))) {
						input += "Class: <" + ent + ">\n\n";
					} else if (importOnt.containsObjectPropertyInSignature(IRI.create(ent))) {
						input += "ObjectProperty: <" + ent + ">\n\n";
					} else if (importOnt.containsDataPropertyInSignature(IRI.create(ent))) {
						input += "DataProperty: <" + ent + ">\n\n";
					} else {
						logger.error("Found unknown entity <" + ent + ">!");
					}
				}
			}
			// add the defined class and expression
			input += "\nClass: <" + filterName + ">\n    " + "EquivalentTo:\n        " + filterRule;

			// parsing stuff
			InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

			filter = new OWLMessageFilter(dfact.getOWLClass(filterName));
			Set parsers = new HashSet();
			parsers.add(new ManchesterOWLSyntaxOntologyParserFactory());
			parsers.add(new RDFXMLParserFactory());
			manager.setOntologyParsers(parsers);
			OWLOntology parsedFilter = manager.loadOntologyFromOntologyDocument(stream);
			for (OWLAxiom ax : parsedFilter.axioms().collect(Collectors.toSet())) {
				if (ax instanceof OWLEquivalentClassesAxiom) {
					filter.addAxiom(ax);
				}
			}
			manager.removeOntology(parsedFilter);
		} catch (Exception e) {
			logger.error("Unable to parse input <" + input + ">", e);
		}

		return filter;
	}

}
