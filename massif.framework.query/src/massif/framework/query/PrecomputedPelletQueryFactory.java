package massif.framework.query;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLOntology;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

 class PrecomputedPelletQueryFactory extends PrecomputedQueryFactory {
	

	protected PrecomputedPelletQueryFactory(OWLOntology ontology, Collection<String> selectQueries, Collection<String> constructQueries){
		super(ontology,selectQueries,constructQueries);
		PelletReasonerFactory reasonerFactory = new PelletReasonerFactory();
		reasoner = reasonerFactory.createReasoner(ontology);
		
	}
	
	
	
}
