package massif.scb.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import massif.scb.cache.owl.OWLIntersection;
import massif.scb.cache.owl.OWLObjectInterface;
import massif.scb.cache.owl.OWLSomeValuesFrom;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
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

public  class EventCheckerVisitor implements OWLClassExpressionVisitor{
	
		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private OWLOntology ontology;
		private OWLIndividual event;

		private OWLDataFactory dFact;
		
		private boolean match;

		public EventCheckerVisitor( OWLOntology ontology, OWLIndividual event){
			this.ontology = ontology;
			this.event = event;
			this.match = true;
		}
		public boolean isMatch(){
			return match;
		}
		public OWLClass getOWLClassIndividual(OWLIndividual ind){
			Stream<OWLClassExpression> classes = EntitySearcher.getTypes(event, ontology);
			if(classes.count()>0){
				return classes.iterator().next().asOWLClass();
			}else{
				return getTypes(ind,ontology).iterator().next().asOWLClass();
			}
		}
		public Set<OWLClassExpression> getTypes(OWLIndividual ind, OWLOntology ontology) {
			Set<OWLClassExpression> result = new TreeSet<OWLClassExpression>();
			for (OWLOntology importOntology : ontology.getImports()) {
				for (OWLClassAssertionAxiom axiom : importOntology
						.getClassAssertionAxioms(ind)) {
					result.add(axiom.getClassExpression());
				}
			}
			return result;
		}
		@Override
		public void visit(OWLClass arg0) {
			OWLClass classCurrentInd = getOWLClassIndividual(event);
			if(!arg0.equals(classCurrentInd)){
				if(classCurrentInd!=null)
					match = false;
			}
		}

		@Override
		public void visit(OWLObjectIntersectionOf arg0) {

			Set<OWLClassExpression> classExp = arg0.getOperands();
			if(classExp.size() <= 2){
				for(OWLClassExpression cEx: arg0.getOperands()){
					cEx.accept(this);
				}
			}else{
				for(OWLClassExpression cEx: arg0.getOperands()){
					OWLIndividual backupEvent = event;
					cEx.accept(this);
					if(!match){
						return;
					}
					event = backupEvent;
				}
			}

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
			Multimap<OWLObjectPropertyExpression, OWLIndividual> objExpressions = EntitySearcher.getObjectPropertyValues(event, ontology);
			OWLObjectPropertyExpression filterProp = arg0.getProperty().asOWLObjectProperty();
			if(objExpressions.containsKey(filterProp)){
				
				event = objExpressions.get(filterProp).iterator().next();
			}else{
				match = false;
				return;
			}
			arg0.getFiller().accept(this);
		}

		@Override
		public void visit(OWLObjectAllValuesFrom arg0) {
			// TODO Auto-generated method stub
			logger.warn("Unsupported Axiom: " + arg0);
		}

		@Override
		public void visit(OWLObjectHasValue arg0) {
			Multimap<OWLObjectPropertyExpression, OWLIndividual> objExpressions = EntitySearcher.getObjectPropertyValues(event, ontology);
			OWLObjectPropertyExpression filterProp = arg0.getProperty().asOWLObjectProperty();
			if(objExpressions.containsKey(filterProp)){
				
				OWLIndividual ind = objExpressions.get(filterProp).iterator().next();
				if(!ind.equals(arg0.getValue())){
					match = false;
					return;
				}
			}else{
				match = false;
				return;
			}
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
			logger.warn("Unsupported Axiom: " + arg0);
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