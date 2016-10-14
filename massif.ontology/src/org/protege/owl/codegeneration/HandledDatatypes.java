package org.protege.owl.codegeneration;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

//import com.hp.hpl.jena.vocabulary.RDF;

public enum HandledDatatypes {	
	STRING(XSDVocabulary.STRING.getIRI(), "String", "String") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			return literal.getLiteral();
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof String) {
				return factory.getOWLLiteral((String) o);
			}
			else {
				return null;
			}
		}
	},
	FLOAT(XSDVocabulary.FLOAT.getIRI(), "float", "Float") {
	
		@Override
		public Object getObject(OWLLiteral literal) {
			return Float.parseFloat(literal.getLiteral());
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Float) {
				return factory.getOWLLiteral((Float) o);
			}
			else {
				return null;
			}
		}
	},
	LONG(XSDVocabulary.LONG.getIRI(), "long", "Long") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			return Long.parseLong(literal.getLiteral());
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Long) {
				return factory.getOWLLiteral((Long) o);
			}
			else {
				return null;
			}
		}
	},
	DOUBLE(XSDVocabulary.DOUBLE.getIRI(), "double", "Double") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			return Double.parseDouble(literal.getLiteral());
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Double) {
				return factory.getOWLLiteral((Double) o);
			}
			else {
				return null;
			}
		}
	},
	BOOLEAN(XSDVocabulary.BOOLEAN.getIRI(), "boolean", "Boolean") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			return Boolean.parseBoolean(literal.getLiteral());
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Boolean) {
				return factory.getOWLLiteral((Boolean) o);
			}
			else {
				return null;
			}
		}
	},
	INTEGER(XSDVocabulary.INTEGER.getIRI(), "int", "Integer") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			return Integer.parseInt(literal.getLiteral());
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Integer) {
				return factory.getOWLLiteral((Integer) o);
			}
			else {
				return null;
			}
		}
		
		@Override
		public boolean isMatch(OWLDatatype dt) {
			return dt.getIRI().equals(XSDVocabulary.INT.getIRI()) || dt.getIRI().equals(XSDVocabulary.INTEGER.getIRI());
		}
	},
	ANYURI(XSDVocabulary.ANY_URI.getIRI(), "anyURI", "URI") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			return URI.create(literal.getLiteral());
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof URI) {
				return factory.getOWLLiteral(((URI)o).toString(),factory.getOWLDatatype(this.getIri()));
			}
			else {
				return null;
			}
		}		
	},	
	DATETIME(XSDVocabulary.DATE_TIME.getIRI(), "Calendar", "Calendar") {
		
		@Override
		public Object getObject(OWLLiteral literal) {
			String lit=literal.getLiteral().replace("T", " ");
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(lit);
		    try {
				cal.setTime(sdf.parse("2001-10-26 21:32:52"));
			} catch (ParseException e) {}
			return cal;
		}
		
		@Override
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Calendar) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				return factory.getOWLLiteral(sdf.format(((Calendar)o).getTime()).replace(" ", "T"),factory.getOWLDatatype(this.getIri()));
			}
			else {
				return null;
			}
		}		
	}
//	,
//	XMLLITERAL(IRI.create(RDF.getURI()+"XMLLiteral"), "XMLLiteral", "XMLLiteral") {
//		
//		public Object getObject(OWLLiteral literal) {
//			return new XMLLiteral(literal.getLiteral());
//		}
//		
//		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
//			if (o instanceof XMLLiteral) {
//				return factory.getOWLLiteral(((XMLLiteral) o).getXML(),factory.getOWLDatatype(this.getIri()));
//			}
//			else {
//				return null;
//			}
//		}
//	}
	;
	
	private IRI iri;
	private String javaType;
	private String javaClass;
	
	private HandledDatatypes(IRI iri, String javaType, String javaClass) {
		this.iri = iri;
		this.javaType = javaType;
		this.javaClass = javaClass;
	}
	
	public IRI getIri() {
		return iri;
	}
	
	public String getJavaType() {
		return javaType;
	}
	
	public String getJavaClass() {
		return javaClass;
	}

	public boolean isMatch(OWLDatatype dt) {
		return dt.getIRI().equals(iri);
	}
	
	public abstract Object getObject(OWLLiteral literal);
	
	public abstract OWLLiteral getLiteral(OWLDataFactory factory, Object o);
}
