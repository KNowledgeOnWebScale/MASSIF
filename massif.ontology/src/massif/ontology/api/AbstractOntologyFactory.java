package massif.ontology.api;

import org.protege.owl.codegeneration.impl.FactoryHelper;
import org.protege.owl.codegeneration.impl.ProtegeJavaMapping;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.semanticweb.owlapi.model.OWLOntology;

import eu.astute.decision.engine.ContextModelListener;

public abstract class AbstractOntologyFactory {
	
	/**
	 * Url of the ontology
	 */
    protected String url;
    
    /**
     * The ontology
     */
    protected OWLOntology ontology;
    
    /**
     * The mapping needed from protege
     */
    protected ProtegeJavaMapping javaMapping;
    
    /**
     * Helper to handle ontology objects
     */
    protected FactoryHelper delegate;
    
    /**
     * Helper to handle inference of the ontology
     */
    protected CodeGenerationInference inference;
    
    /**
     * Listener to handle ontology updates
     */
    protected ContextModelListener decisionEngine;
        
    // Constructor
    public AbstractOntologyFactory() { 
    	// Prepare a javamapping for all objects in the ontology
    	javaMapping = new ProtegeJavaMapping();
    }
	
    /**
     * Prepare the factory
     */
    public void createFactory(OWLOntology ontology, String url, ContextModelListener decisionEngine) {
    	createFactory(ontology, new SimpleInference(ontology), url, decisionEngine);
    }
    
    /**
     * Prepare the factory
     */
    public void createFactory(OWLOntology ontology, CodeGenerationInference inference, String url, ContextModelListener decisionEngine) {
    	this.url = url;
        this.ontology = ontology;
        this.inference = inference;
        this.decisionEngine = decisionEngine;
        
        
        javaMapping.initialize(ontology, inference, decisionEngine);
        
        // Create a factory helper
        delegate = new FactoryHelper(ontology, inference, javaMapping, decisionEngine);
    }
        
}
