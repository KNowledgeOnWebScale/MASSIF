package org.protege.owl.codegeneration.impl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;



import org.protege.owl.codegeneration.CodeGenerationRuntimeException;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import eu.astute.decision.engine.ContextModelListener;
import massif.framework.util.owl.CascadingEntityChecker;

public class FactoryHelper {
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private CodeGenerationInference inference;
	private ContextModelListener decisionEngine;
	private ProtegeJavaMapping javaMapping;
	
	// Constructor
	public FactoryHelper(OWLOntology ontology, CodeGenerationInference inference, ProtegeJavaMapping javaMapping, ContextModelListener decisionEngine) {
		this.ontology = ontology;
		this.inference = inference;
		this.manager = ontology.getOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.decisionEngine = decisionEngine;
		this.javaMapping=javaMapping;
		
	}

	public void flushOwlReasoner() {
		inference.flush();
	}

	public <X extends WrappedIndividualImpl> X createWrappedIndividual(String name, OWLClass type, Class<X> c) {
		OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(name));
		boolean inSignature = false;
		// remove this as we create individuals in the main ontology - performance
		/*for (OWLOntology inImportClosure : ontology.getImportsClosure()) {
			if (inImportClosure.containsEntityInSignature(i)) {
				inSignature = true;
				break;
			}
		}*/
		
		if(ontology.containsEntityInSignature(i))
			inSignature = true;
		
		if (!inSignature) {
			manager.addAxiom(ontology,
					factory.getOWLClassAssertionAxiom(type, i));
		} else if (!inference.canAs(i, type)) {
			return null;
		}
		
		return getAllWrappedIndividual(i, name,  type,  c);
	}

	public <X extends WrappedIndividualImpl> X getWrappedIndividual(OWLIndividual ind, OWLClass type,Class<X> c){
		return getAllWrappedIndividual(ind.asOWLNamedIndividual(), ind.asOWLNamedIndividual().getIRI().toString(),  type,  c);
	}
	
	public <X extends WrappedIndividualImpl> X getWrappedIndividual(String name, OWLClass type, Class<X> c) {
		IRI iri = IRI.create(name);
		
		/*OWLNamedIndividual i = factory.getOWLNamedIndividual(iri);
		if (!inference.canAs(i, type)) {
			// could be a different ontology
			Collection<OWLNamedIndividual> inds=inference.getIndividuals(type);
			for(OWLNamedIndividual ind:inds){
				if(ind.getIRI().getFragment().equals(iri.getFragment()))
					return getAllWrappedIndividual(ind, ind.getIRI().toString(),  type,  c);
			}
			return null;
		}
		return getAllWrappedIndividual(i, name,  type,  c);*/		
	
		/*Collection<OWLNamedIndividual> inds=inference.getIndividuals(type);
		for(OWLNamedIndividual ind:inds){
			if(ind.getIRI().getFragment().equals(iri.getFragment()))
				return getAllWrappedIndividual(ind, ind.getIRI().toString(),  type,  c);
		}*/
		CascadingEntityChecker checker=new CascadingEntityChecker(manager.getOWLDataFactory(), ontology);
		 String stripedName=name.substring(name.indexOf("#")+1);
		OWLNamedIndividual ind=checker.getOWLIndividual(stripedName);
		if(ind!=null){
				return getAllWrappedIndividual(ind.asOWLNamedIndividual(), ind.asOWLNamedIndividual().getIRI().toString(),  type,  c);
		}
		return null;
	}
	
	private <X extends WrappedIndividualImpl> X getAllWrappedIndividual(OWLNamedIndividual i,String name, OWLClass type, Class<X> c){
		// check if different type - lets not do that for performance reasons
				boolean exists=false;
				X ind = null;
				/*Collection<OWLClass> types=inference.getTypes(i);
				for(OWLClass cls:types){
					if(!cls.equals(type)){
						Class<X> x=(Class<X>) javaMapping.getJavaImplementationFromOwlClass(cls);
						X temp=getWrappedIndividual(name, x);
						
						if(c.isAssignableFrom(x)){
							ind=temp;
							exists=true;
						}
					}
				}
				
				if(c.isAssignableFrom(x)){
							ind=temp;
							exists=true;
						}
						
				*/				
					
						Class<X> x=(Class<X>) javaMapping.getJavaImplementationFromOwlClass(type);
						X temp=getWrappedIndividual(name, x);
						
						return temp;	
				
				/*if(exists)
					return ind;
				else
					return getWrappedIndividual(name, c);*/
	}

	private <X extends WrappedIndividualImpl> X getWrappedIndividual(
			String name, Class<X> c) {
		try {
			Constructor<X> constructor = c.getConstructor(OWLOntology.class,
					IRI.class, CodeGenerationInference.class, ProtegeJavaMapping.class,
					ContextModelListener.class);
			X x = constructor.newInstance(ontology, IRI.create(name),
					inference, javaMapping,decisionEngine);
			
			if (decisionEngine != null && !decisionEngine.contextObjectExists(x)){				
				decisionEngine.contextObjectCreated(x);
				//LOGGER.info("Create object - "+x.getOwlIndividual().getIRI());
			}
			
			return x;
		} catch (Exception e) {
			throw new CodeGenerationRuntimeException(e);
		}
	}

	public <X extends WrappedIndividualImpl> Collection<X> getWrappedIndividuals(
			OWLClass owlClass, Class<X> c) {
		Set<X> wrappers = new HashSet<X>();
		for (OWLNamedIndividual i : inference.getIndividuals(owlClass)) {
			wrappers.add(getWrappedIndividual(i.getIRI().toString(), c));
		}
		return wrappers;
	}

}
