package massif.scb.util;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
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
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * This class can check if two OWLClasses are equal based on their syntax.
 * Note that no reasoning takes place.
 * 
 * However the order of the items in a union are an intersection can vary and the classes 
 * are still considered equal.
 * 
 * Note that the classes should be handed over as OWLSubClassOfAxioms.
 * 
 * Use this class only in optimized setting where performance is of impartances.
 * Otherwise use Reasoner.getEquivalentClasses(..)
 * 
 * @author pbonte
 *
 */
public class EqualOWLClassChecker implements OWLClassExpressionVisitor {

	private boolean equal = true;
	private OWLClassExpression clazz1;
	private OWLClassExpression clazz2;
	private OWLClassExpression currentAxiomClazz2;

	public EqualOWLClassChecker(OWLClassExpression clazz1, OWLClassExpression clazz2) {
		this.clazz1 = clazz1;
		this.clazz2 = clazz2;
		currentAxiomClazz2 = clazz2;
		clazz1.accept(this);
	}
	public EqualOWLClassChecker(OWLSubClassOfAxiom clazz1, OWLSubClassOfAxiom clazz2) {	
		this.clazz1 = clazz1.getSuperClass();
		this.clazz2 = clazz2.getSuperClass();
		currentAxiomClazz2 = this.clazz2;
		this.clazz1.accept(this);
	}


	public boolean isEqual() {
		return equal;
	}

	@Override
	public void visit(OWLClass arg0) {
		if (!(currentAxiomClazz2 instanceof OWLClass)
				|| !(arg0.getIRI().toString().equals(((OWLClass) currentAxiomClazz2).getIRI().toString()))) {
			equal = false;
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectIntersectionOf) {
			OWLObjectIntersectionOf currentCls2 = (OWLObjectIntersectionOf) currentAxiomClazz2;
			if (currentCls2.getOperands().size() == arg0.getOperands().size()) {
				// first check if all operands match
				for (OWLClassExpression cEx : arg0.getOperands()) {
					if (!currentCls2.getOperands().contains(cEx)) {
						equal = false;
						return;
					}
				}
				for (OWLClassExpression cEx : arg0.getOperands()) {
					for (OWLClassExpression c2Ex : currentCls2.getOperands())
						if (c2Ex.equals(cEx)) {
							currentAxiomClazz2 = c2Ex;
							cEx.accept(this);
						}
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLObjectUnionOf arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectUnionOf) {
			OWLObjectUnionOf currentCls2 = (OWLObjectUnionOf) currentAxiomClazz2;
			if (currentCls2.getOperands().size() == arg0.getOperands().size()) {
				// first check if all operands match
				for (OWLClassExpression cEx : arg0.getOperands()) {
					if (!currentCls2.getOperands().contains(cEx)) {
						equal = false;
						return;
					}
				}
				for (OWLClassExpression cEx : arg0.getOperands()) {
					for (OWLClassExpression c2Ex : currentCls2.getOperands())
						if (c2Ex.equals(cEx)) {
							currentAxiomClazz2 = c2Ex;
							cEx.accept(this);
						}
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLObjectComplementOf arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectComplementOf) {
			OWLObjectComplementOf currentCls2 = (OWLObjectComplementOf) currentAxiomClazz2;
			if (currentCls2.getOperand().equals(arg0.getOperand())) {
				currentAxiomClazz2 = currentCls2.getOperand();
				arg0.getOperand().accept(this);
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLObjectSomeValuesFrom arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom currentCls2 = (OWLObjectSomeValuesFrom) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLObjectProperty().equals(arg0.getProperty().asOWLObjectProperty())) {
				currentAxiomClazz2 = currentCls2.getFiller();
				arg0.getFiller().accept(this);
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLObjectAllValuesFrom arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectAllValuesFrom) {
			OWLObjectAllValuesFrom currentCls2 = (OWLObjectAllValuesFrom) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLObjectProperty().equals(arg0.getProperty().asOWLObjectProperty())) {
				currentAxiomClazz2 = currentCls2.getFiller();
				arg0.getFiller().accept(this);
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}


	}

	@Override
	public void visit(OWLObjectHasValue arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectHasValue) {
			OWLObjectHasValue currentCls2 = (OWLObjectHasValue) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLObjectProperty().equals(arg0.getProperty().asOWLObjectProperty())) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}
		
	}

	@Override
	public void visit(OWLObjectMinCardinality arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectMinCardinality) {
			OWLObjectMinCardinality currentCls2 = (OWLObjectMinCardinality) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLObjectProperty().equals(arg0.getProperty().asOWLObjectProperty())
					&& currentCls2.getCardinality() == arg0.getCardinality()) {
				currentAxiomClazz2 = currentCls2.getFiller();
				arg0.getFiller().accept(this);
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLObjectExactCardinality arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectExactCardinality) {
			OWLObjectExactCardinality currentCls2 = (OWLObjectExactCardinality) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLObjectProperty().equals(arg0.getProperty().asOWLObjectProperty())
					&& currentCls2.getCardinality() == arg0.getCardinality()) {
				currentAxiomClazz2 = currentCls2.getFiller();
				arg0.getFiller().accept(this);
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLObjectMaxCardinality arg0) {
		if (currentAxiomClazz2 instanceof OWLObjectMaxCardinality) {
			OWLObjectMaxCardinality currentCls2 = (OWLObjectMaxCardinality) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLObjectProperty().equals(arg0.getProperty().asOWLObjectProperty())
					&& currentCls2.getCardinality() == arg0.getCardinality()) {
				currentAxiomClazz2 = currentCls2.getFiller();
				arg0.getFiller().accept(this);
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

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
		if (currentAxiomClazz2 instanceof OWLDataSomeValuesFrom) {
			OWLDataSomeValuesFrom currentCls2 = (OWLDataSomeValuesFrom) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLDataProperty().equals(arg0.getProperty().asOWLDataProperty())) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLDataAllValuesFrom arg0) {
		if (currentAxiomClazz2 instanceof OWLDataAllValuesFrom) {
			OWLDataAllValuesFrom currentCls2 = (OWLDataAllValuesFrom) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLDataProperty().equals(arg0.getProperty().asOWLDataProperty())) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLDataHasValue arg0) {
		if (currentAxiomClazz2 instanceof OWLDataHasValue) {
			OWLDataHasValue currentCls2 = (OWLDataHasValue) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLDataProperty().equals(arg0.getProperty().asOWLDataProperty())) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLDataMinCardinality arg0) {
		if (currentAxiomClazz2 instanceof OWLDataMinCardinality) {
			OWLDataMinCardinality currentCls2 = (OWLDataMinCardinality) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLDataProperty().equals(arg0.getProperty().asOWLDataProperty())
					&& arg0.getCardinality() == currentCls2.getCardinality()) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLDataExactCardinality arg0) {
		if (currentAxiomClazz2 instanceof OWLDataExactCardinality) {
			OWLDataExactCardinality currentCls2 = (OWLDataExactCardinality) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLDataProperty().equals(arg0.getProperty().asOWLDataProperty())
					&& arg0.getCardinality() == currentCls2.getCardinality()) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

	@Override
	public void visit(OWLDataMaxCardinality arg0) {
		if (currentAxiomClazz2 instanceof OWLDataMaxCardinality) {
			OWLDataMaxCardinality currentCls2 = (OWLDataMaxCardinality) currentAxiomClazz2;
			if (currentCls2.getProperty().asOWLDataProperty().equals(arg0.getProperty().asOWLDataProperty())
					&& arg0.getCardinality() == currentCls2.getCardinality()) {
				if(!currentCls2.getFiller().equals(arg0.getFiller())){
					equal = false;
				}
			} else {
				equal = false;
			}
		} else {
			equal = false;
		}

	}

}
