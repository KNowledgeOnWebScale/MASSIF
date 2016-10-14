package massif.scb.cache;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

public  class MyOWLClassExpressionVistior implements OWLClassExpressionVisitor{
	
		private ArrayList<OWLEntity> entityList;
		
		public MyOWLClassExpressionVistior(){
			entityList = new ArrayList<OWLEntity>();
		}
		public  List getEntityList(){
			return entityList;
		}
		@Override
		public void visit(OWLClass arg0) {
			entityList.add(arg0);
		}

		@Override
		public void visit(OWLObjectIntersectionOf arg0) {
			// TODO Auto-generated method stub
			//System.out.println(arg0.getOperands());
			for(OWLClassExpression cEx: arg0.getOperands()){
				cEx.accept(this);
			}
		}

		@Override
		public void visit(OWLObjectUnionOf arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectComplementOf arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectSomeValuesFrom arg0) {
			// TODO Auto-generated method stub
			entityList.add(arg0.getProperty().asOWLObjectProperty());			
			arg0.getFiller().accept(this);
		}

		@Override
		public void visit(OWLObjectAllValuesFrom arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectHasValue arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectMinCardinality arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectExactCardinality arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectMaxCardinality arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectHasSelf arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLObjectOneOf arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLDataSomeValuesFrom arg0) {
			// TODO Auto-generated method stub
			entityList.add(arg0.getProperty().asOWLDataProperty());
			
		}

		@Override
		public void visit(OWLDataAllValuesFrom arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLDataHasValue arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLDataMinCardinality arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLDataExactCardinality arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(OWLDataMaxCardinality arg0) {
			// TODO Auto-generated method stub
			
		}

		
	}