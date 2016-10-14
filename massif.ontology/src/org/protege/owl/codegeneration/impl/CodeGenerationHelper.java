package org.protege.owl.codegeneration.impl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.protege.owl.codegeneration.CodeGenerationRuntimeException;
import org.protege.owl.codegeneration.HandledDatatypes;
import org.protege.owl.codegeneration.WrappedIndividual;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import eu.astute.decision.engine.ContextModelListener;

public class CodeGenerationHelper {

	private OWLOntology owlOntology;
	private OWLNamedIndividual owlIndividual;
	private OWLDataFactory owlDataFactory;
	private OWLOntologyManager manager;
	private OWLReasoner reasoner;
	private CodeGenerationInference inference;
	private ContextModelListener decisionEngine;
	private ProtegeJavaMapping javaMapping;
	
	//protected final Logger LOGGER = Logger.getLogger(this.getClass());

	public CodeGenerationHelper(OWLOntology owlOntology,
			OWLNamedIndividual individual, CodeGenerationInference inference,
			ProtegeJavaMapping javaMapping,ContextModelListener decisionEngine) {
		this.owlOntology = owlOntology;
		owlIndividual = individual;

		manager = owlOntology.getOWLOntologyManager();
		owlDataFactory = manager.getOWLDataFactory();
		this.inference = inference;
		this.reasoner = inference.getOWLReasoner();
		this.decisionEngine = decisionEngine;
		this.javaMapping=javaMapping;
	}

	public OWLOntology getOwlOntology() {
		return owlOntology;
	}

	public OWLNamedIndividual getOwlIndividual() {
		return owlIndividual;
	}

	public <X> Collection<X> getPropertyValues(OWLNamedIndividual i,
			OWLObjectProperty p, Class<X> c) {
		try {
			Constructor<X> constructor = c.getConstructor(OWLOntology.class,
					IRI.class, CodeGenerationInference.class,ProtegeJavaMapping.class,
					ContextModelListener.class);
			Set<X> results = new HashSet<X>();

			if (reasoner != null) {
				for (Node<OWLNamedIndividual> j : reasoner
						.getObjectPropertyValues(i, p)) {
					X x = constructor.newInstance(owlOntology, j
							.getRepresentativeElement().getIRI(), inference,javaMapping,
							decisionEngine);

					if (decisionEngine != null
							&& !decisionEngine.contextObjectExists(x)) {
						decisionEngine.contextObjectCreated(x);
					//	LOGGER.info("Create object - "+j.getRepresentativeElement().getIRI());
					}

					results.add(x);
				}
			} else {
				for (OWLOntology imported : owlOntology.getImportsClosure()) {
					for (OWLIndividual j : i.getObjectPropertyValues(p,
							imported)) {
						if (!j.isAnonymous()) {

							X x = constructor.newInstance(owlOntology, j
									.asOWLNamedIndividual().getIRI(),
									inference, javaMapping,decisionEngine);

							if (decisionEngine != null
									&& !decisionEngine.contextObjectExists(x)) {
								decisionEngine.contextObjectCreated(x);
							//	LOGGER.info("Create object - "+j.asOWLNamedIndividual().getIRI());
							}

							results.add(x);
						}
					}
				}
			}

			if (results.isEmpty() && reasoner != null) {
				// get equivalent/superclass class restrictions which result in the
				// creation of additional relationships to new instances
				Set<OWLClassExpression> exs = i.getTypes(this.owlOntology);
				Set<OWLClassExpression> eqs=new HashSet<OWLClassExpression>();
				
				for(OWLClassExpression ex:exs){
					eqs.addAll(ex.asOWLClass().getEquivalentClasses(this.owlOntology.getImportsClosure()));
					eqs.addAll(ex.asOWLClass().getSuperClasses(this.owlOntology.getImportsClosure()));
				}
				
				for (OWLClassExpression eq : eqs) {
					for (OWLClassExpression e : eq.asConjunctSet()) {
						Set<OWLObjectProperty> props = e.getObjectPropertiesInSignature();

						for (OWLObjectProperty prop : props) {
							if (prop.equals(p)) {
								// property in signature via restriction, so a
								// new
								// instance should be created in relation to the
								// individual
								Set<OWLClass> classes = e.getClassesInSignature();
								for (OWLClass clas : classes) {
									
									OWLNamedIndividual ind = null;
									boolean inSignature = false;
									
									Set<Node<OWLNamedIndividual>> inds=reasoner.getInstances(clas, false).getNodes();
									for(Node<OWLNamedIndividual> in:inds){
										ind=in.getRepresentativeElement();
										inSignature=true;
										break;
									}						
									
									if (!inSignature) {
										String name = clas.getIRI().getStart()
												+ i.getIRI().getFragment()
												+ prop.getIRI().getFragment()
												+ clas.getIRI().getFragment()
												+ UUID.randomUUID().toString();

										ind = owlDataFactory.getOWLNamedIndividual(IRI.create(name));
										manager.addAxiom(owlOntology,owlDataFactory.getOWLClassAssertionAxiom(clas, ind));
										
									} else if (!inference.canAs(ind, clas)) {
										return null;
									}

									// add property

									OWLAxiom axiom = owlDataFactory
											.getOWLObjectPropertyAssertionAxiom(
													prop, i, ind);
									manager.addAxiom(owlOntology, axiom);

									X x = constructor.newInstance(owlOntology,
											ind.getIRI(), inference,javaMapping,
											decisionEngine);

									if (decisionEngine != null
											&& !decisionEngine
													.contextObjectExists(x)) {
										decisionEngine.contextObjectCreated(x);
										//LOGGER.info("Create object - "+ind.getIRI());
									}

									results.add(x);
								}
							}
						}
					}
				}
			}

			return results;
		} catch (Exception e) {
			throw new CodeGenerationRuntimeException(e);
		}
	}
	
	private <X> void createWrappedIndividual(WrappedIndividual ind){	
		OWLNamedIndividual i=ind.getOwlIndividual();		
		
		        // check if different type				
				Collection<OWLClass> types=inference.getTypes(i); // THIS IS THE PERFORMANCE KILLER!!!!
				types.removeAll(i.getTypes(this.owlOntology));
		
				for(OWLClass cls:types){
						Class<X> x= (Class<X>) javaMapping.getJavaImplementationFromOwlClass(cls);
						try {
							Constructor<X> constructor = x.getConstructor(OWLOntology.class,
									IRI.class, CodeGenerationInference.class,ProtegeJavaMapping.class,
									ContextModelListener.class);
							X obj = constructor.newInstance(this.owlOntology, i.getIRI(),
									inference, javaMapping,decisionEngine);
							
							if (decisionEngine != null && !decisionEngine.contextObjectExists(x)){				
								decisionEngine.contextObjectCreated(x);
								//LOGGER.info("Create object - "+i.getIRI());
							}
							
						} catch (Exception e) {
							throw new CodeGenerationRuntimeException(e);
						}
				}		
	}
	

	public void addPropertyValue(WrappedIndividual i, OWLObjectProperty p, WrappedIndividual j) {
		OWLAxiom axiom = owlDataFactory.getOWLObjectPropertyAssertionAxiom(p, i.getOwlIndividual(), j.getOwlIndividual());
		manager.addAxiom(owlOntology, axiom);
		//createWrappedIndividual(i); //performance killer, but necessary due to possible subclassing
		
		if (decisionEngine != null) {
			decisionEngine.contextObjectUpdated(i);
			decisionEngine.contextObjectUpdated(j);
			//LOGGER.info("New object attribute - \n"+i.getOwlIndividual().getIRI()+"\n"+ p.getIRI()+"\n"+  j.getOwlIndividual().getIRI());
		}
	}
	
	
	private <X> void removeWrappedIndividual(WrappedIndividual ind, Collection<OWLClass> oldTypes){	
		OWLNamedIndividual i=ind.getOwlIndividual();
		
		// check if disappeared types
				//Collection<OWLClass> newTypes=inference.getTypes(i); 
				//oldTypes.removeAll(newTypes);
				
				for(OWLClass cls:oldTypes){
						Class<X> x= (Class<X>) javaMapping.getJavaImplementationFromOwlClass(cls);
						try {
							Constructor<X> constructor = x.getConstructor(OWLOntology.class,
									IRI.class, CodeGenerationInference.class,ProtegeJavaMapping.class,
									ContextModelListener.class);
							X obj = constructor.newInstance(this.owlOntology, i.getIRI(),
									inference, javaMapping,decisionEngine);
							
							if (decisionEngine != null){				
								decisionEngine.contextObjectRemoved(x);
							//	LOGGER.info("Remove object - "+i.getIRI());
							}
							
						} catch (Exception e) {
							throw new CodeGenerationRuntimeException(e);
						}
				}		
	}

	public void removePropertyValue(WrappedIndividual i, OWLObjectProperty p,
			WrappedIndividual j) {
		OWLAxiom axiom = owlDataFactory.getOWLObjectPropertyAssertionAxiom(p,
				i.getOwlIndividual(), j.getOwlIndividual());
				
		Collection<OWLClass> types=inference.getTypes(i.getOwlIndividual());
				
		for (OWLOntology imported : owlOntology.getImportsClosure()) {
			manager.removeAxiom(imported, axiom);
		}
		
		removeWrappedIndividual(i, types);

		if (decisionEngine != null) {
			decisionEngine.contextObjectUpdated(i);
			decisionEngine.contextObjectUpdated(j);
			//LOGGER.info("Remove object attribute - \n"+i.getOwlIndividual().getIRI()
				//	+"\n"+ p.getIRI()+"\n"+  j.getOwlIndividual().getIRI());
			
		}
	}

	public <X> Collection<X> getPropertyValues(OWLNamedIndividual i,
			OWLDataProperty p, Class<X> c) {
		Set<X> results = new HashSet<X>();

		if (reasoner != null) {
			for (OWLLiteral l : reasoner.getDataPropertyValues(i, p)) {
				results.add(c.cast(getObjectFromLiteral(l)));
			}
		} else {
			for (OWLOntology imported : owlOntology.getImportsClosure()) {
				for (OWLLiteral l : i.getDataPropertyValues(p, imported)) {
					results.add(c.cast(getObjectFromLiteral(l)));
				}
			}
		}
		return results;
	}

	public void addPropertyValue(WrappedIndividual i, OWLDataProperty p,
			Object o) {
		OWLLiteral literal = getLiteralFromObject(owlDataFactory, o);
		if (literal != null) {
			OWLAxiom axiom = owlDataFactory.getOWLDataPropertyAssertionAxiom(p,
					i.getOwlIndividual(), literal);
			manager.addAxiom(owlOntology, axiom);
		} else {
			throw new CodeGenerationRuntimeException(
					"Invalid type for property value object " + o);
		}

		if (decisionEngine != null) {
			decisionEngine.contextObjectUpdated(i);
			//LOGGER.info("New object attribute - \n"
			//+i.getOwlIndividual().getIRI()+"\n"+ p.getIRI()+"\n"+  o);
			
		}
	}

	public void removePropertyValue(WrappedIndividual i, OWLDataProperty p,
			Object o) {
		OWLLiteral literal = getLiteralFromObject(owlDataFactory, o);
		if (literal != null) {
			OWLAxiom axiom = owlDataFactory.getOWLDataPropertyAssertionAxiom(p,
					i.getOwlIndividual(), literal);
			manager.removeAxiom(owlOntology, axiom);
		} else {
			throw new CodeGenerationRuntimeException(
					"Invalid type for property value object " + o);
		}

		if (decisionEngine != null) {
			decisionEngine.contextObjectUpdated(i);
		//	LOGGER.info("Remove object attribute - \n"
			//+i.getOwlIndividual().getIRI()+"\n"+ p.getIRI()+"\n"+  o);
			
		}
	}

	public static Object getObjectFromLiteral(OWLLiteral literal) {
		Object o = null;
		for (HandledDatatypes handled : HandledDatatypes.values()) {
			if (handled.isMatch(literal.getDatatype())) {
				o = handled.getObject(literal);
				break;
			}
		}
		if (o == null) {
			o = literal;
		}
		return o;
	}

	public static OWLLiteral getLiteralFromObject(
			OWLDataFactory owlDataFactory, Object o) {
		OWLLiteral literal = null;
		if (o instanceof OWLLiteral) {
			literal = (OWLLiteral) o;
		} else {
			for (HandledDatatypes handled : HandledDatatypes.values()) {
				literal = handled.getLiteral(owlDataFactory, o);
				if (literal != null) {
					break;
				}
			}
		}
		return literal;
	}
}
