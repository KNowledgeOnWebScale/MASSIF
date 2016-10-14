package massif.scb.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import massif.scb.cache.owl.OWLIntersection;
import massif.scb.cache.owl.OWLObjHasValue;
import massif.scb.cache.owl.OWLObjectInterface;
import massif.scb.cache.owl.OWLSomeValuesFrom;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

public class MyOWLClassExpressionVisitor implements OWLClassExpressionVisitor {

	private ArrayList<OWLObject> entityList; // collects data that matched
												// filter rule
	private ArrayList<OWLObject> entityListTemp; // collects data that matched
													// filter rule

	private OWLOntology ontology;
	private OWLIndividual event;
	private OWLReasoner reasoner;
	private OWLOntology filterOntology;
	private OWLClassExpression rewriteFilter;
	private OWLDataFactory dFact;
	private OWLClassExpression tempRewriteFilter;

	private OWLObjectInterface currentObj;
	private OWLObjectInterface finalObj;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public MyOWLClassExpressionVisitor(OWLOntology ontology,
			OWLIndividual event, OWLReasoner reasoner) {
		entityList = new ArrayList<OWLObject>();
		entityListTemp = new ArrayList<OWLObject>();

		this.ontology = ontology;
		this.event = event;
		this.reasoner = reasoner;
		try {
			this.filterOntology = ontology.getOWLOntologyManager()
					.createOntology();
			this.dFact = ontology.getOWLOntologyManager().getOWLDataFactory();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List getEntityList() {
		return entityList;
	}

	public OWLObjectInterface getOWLObject() {
		currentObj.toString();
		return currentObj;
	}

	public OWLClassExpression constructOWLFilter() {
		return (OWLClassExpression) currentObj.constructOWLObject(dFact);
	}

	public OWLClass getOWLClassIndividual(OWLIndividual ind, OWLClass cls) {
		Set<OWLClassExpression> classes = EntitySearcher.getTypes(event, ontology).collect(Collectors.toSet());
		OWLClass found = null;
		OWLClass testClass = null;
		
		for (OWLClassExpression classExp : classes) {
			testClass = classExp.asOWLClass();
			if (classExp.asOWLClass().equals(cls)) {
				found = cls; // the filter class matches the individual class
				break;
			}
		}
		if (found == null) { //class must be a subclass
			boolean subClassing = testClass==null? true : false; 
			/*only calculate all super classes if the individual has a specified type
			If it does not have a specified type, we will reason to retrieve it.
			But without taking all possible classes in consideration*/	
			NodeSet<OWLClass> classesReasoner = reasoner.getTypes(event.asOWLNamedIndividual(), subClassing);
			for (OWLClassExpression classExp : classesReasoner.getFlattened()) {
				if( testClass == null){ //there is no define class, just take class from reasoner
					found = classExp.asOWLClass();
					break;
				}
				else if (classExp.asOWLClass().equals(testClass)) {
					found = testClass;
					break;
				}
			}
		}
		return found;
	}

	@Override
	public void visit(OWLClass arg0) {
		OWLClass classCurrentInd = getOWLClassIndividual(event, arg0);
		if (classCurrentInd != null) {
			entityList.add(classCurrentInd);
			currentObj.add(classCurrentInd);
		} else {
			// check if it is subclass or not
			logger.error("Class dismatch!!!! " + arg0 + " vs "+ classCurrentInd);
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf arg0) {
		// TODO Auto-generated method stub
		// System.out.println(arg0.getOperands());
		if (finalObj == null) {
			finalObj = new OWLIntersection();
			currentObj = finalObj;
		}
		OWLObjectInterface tempObj = currentObj;
		currentObj = new OWLIntersection();
		Set<OWLClassExpression> newExpressionSet = new HashSet<OWLClassExpression>();

		Set<OWLClassExpression> classExp = arg0.getOperands();
		if (classExp.size() == 2) {
			for (OWLClassExpression cEx : arg0.getOperands()) {
				cEx.accept(this);
				newExpressionSet.add(tempRewriteFilter);
			}
		} else if (classExp.size() > 2) {
			for (OWLClassExpression cEx : arg0.getOperands()) {
				OWLIndividual backupEvent = event;
				cEx.accept(this);
				event = backupEvent;
				newExpressionSet.add(tempRewriteFilter);
			}
		}
		tempObj.add(currentObj);
		currentObj = tempObj;
	}

	@Override
	public void visit(OWLObjectUnionOf arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectComplementOf arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectSomeValuesFrom arg0) {
		// TODO Auto-generated method stub
		OWLObjectInterface tempObj = currentObj;
		currentObj = new OWLSomeValuesFrom();
		Multimap<OWLObjectPropertyExpression, OWLIndividual> objExpressions = EntitySearcher.getObjectPropertyValues(event, ontology);
		OWLObjectPropertyExpression filterProp = arg0.getProperty().asOWLObjectProperty();
		if (objExpressions.containsKey(filterProp)) {
			// entityList.add(arg0);
			entityList.add(filterProp.asOWLObjectProperty());
			currentObj.add(filterProp.asOWLObjectProperty());
			// follow link to next event
			event = objExpressions.get(filterProp).iterator().next();
		} else {
			// use reasoner to see if subproperty is a better match
			NodeSet<OWLObjectPropertyExpression> reasonerProps = reasoner
					.getSubObjectProperties(filterProp, false);
			for (OWLObjectPropertyExpression objExp : reasonerProps.getFlattened()) {
				if (objExpressions.containsKey(objExp)) {
					// entityList.add(arg0);
					entityList.add(objExp.asOWLObjectProperty());
					currentObj.add(objExp.asOWLObjectProperty());
					// follow link to next event
					event = objExpressions.get(objExp).iterator().next();
					break;
				}
			}
		}
		arg0.getFiller().accept(this);
		tempObj.add(currentObj);
		currentObj = tempObj;
	}

	@Override
	public void visit(OWLObjectAllValuesFrom arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectHasValue arg0) {
		OWLObjectInterface tempObj = currentObj;
		currentObj = new OWLObjHasValue();
		Multimap<OWLObjectPropertyExpression, OWLIndividual> objExpressions = EntitySearcher.getObjectPropertyValues(event, ontology);
		OWLObjectPropertyExpression filterProp = arg0.getProperty().asOWLObjectProperty();
		if (objExpressions.containsKey(filterProp)) {
			// entityList.add(arg0);
			entityList.add(filterProp.asOWLObjectProperty());
			currentObj.add(filterProp.asOWLObjectProperty());
			// follow link to next event
			OWLIndividual ind = objExpressions.get(filterProp).iterator().next();
			currentObj.add(ind);
			//event = objExpressions.get(filterProp).iterator().next();
		} else {
			// use reasoner to see if subproperty is a better match
			NodeSet<OWLObjectPropertyExpression> reasonerProps = reasoner.getSubObjectProperties(filterProp, false);
			for (OWLObjectPropertyExpression objExp : reasonerProps.getFlattened()) {
				if (objExpressions.containsKey(objExp)) {
					// entityList.add(arg0);
					entityList.add(objExp.asOWLObjectProperty());
					currentObj.add(objExp.asOWLObjectProperty());
					// follow link to next event
					OWLIndividual ind = objExpressions.get(filterProp).iterator().next();
					currentObj.add(ind);
					//event = objExpressions.get(objExp).iterator().next();
					break;
				}
			}
		}
		//arg0.getFiller().accept(this);
		tempObj.add(currentObj);
		currentObj = tempObj;
	}

	@Override
	public void visit(OWLObjectMinCardinality arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectExactCardinality arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectMaxCardinality arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectHasSelf arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLObjectOneOf arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLDataSomeValuesFrom arg0) {
		// TODO Auto-generated method stub
		entityList.add(arg0.getProperty().asOWLDataProperty());

	}

	@Override
	public void visit(OWLDataAllValuesFrom arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLDataHasValue arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLDataMinCardinality arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLDataExactCardinality arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

	@Override
	public void visit(OWLDataMaxCardinality arg0) {
		// TODO Auto-generated method stub
		logger.warn("Unsupported Axiom: " + arg0);

	}

}