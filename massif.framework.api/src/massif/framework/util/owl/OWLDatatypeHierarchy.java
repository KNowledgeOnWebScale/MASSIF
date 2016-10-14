package massif.framework.util.owl;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OWLDatatypeHierarchy {

	
	private static final Map<OWL2Datatype, OWL2Datatype> HIERARCHY = 
			new HashMap<OWL2Datatype, OWL2Datatype>();
	
	
	static {
		// create the hierarchy
		HIERARCHY.put(OWL2Datatype.XSD_INTEGER, OWL2Datatype.XSD_DECIMAL);

		HIERARCHY.put(OWL2Datatype.XSD_NON_POSITIVE_INTEGER, OWL2Datatype.XSD_INTEGER);
		HIERARCHY.put(OWL2Datatype.XSD_NEGATIVE_INTEGER, OWL2Datatype.XSD_NON_POSITIVE_INTEGER);
		
		HIERARCHY.put(OWL2Datatype.XSD_NON_NEGATIVE_INTEGER, OWL2Datatype.XSD_INTEGER);
		HIERARCHY.put(OWL2Datatype.XSD_POSITIVE_INTEGER, OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
		HIERARCHY.put(OWL2Datatype.XSD_UNSIGNED_LONG, OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
		HIERARCHY.put(OWL2Datatype.XSD_UNSIGNED_INT, OWL2Datatype.XSD_UNSIGNED_LONG);
		HIERARCHY.put(OWL2Datatype.XSD_UNSIGNED_SHORT, OWL2Datatype.XSD_UNSIGNED_INT);
		HIERARCHY.put(OWL2Datatype.XSD_UNSIGNED_BYTE, OWL2Datatype.XSD_UNSIGNED_SHORT);
		
		HIERARCHY.put(OWL2Datatype.XSD_LONG, OWL2Datatype.XSD_INTEGER);
		HIERARCHY.put(OWL2Datatype.XSD_INT, OWL2Datatype.XSD_LONG);
		HIERARCHY.put(OWL2Datatype.XSD_SHORT, OWL2Datatype.XSD_INT);
		HIERARCHY.put(OWL2Datatype.XSD_BYTE, OWL2Datatype.XSD_SHORT);

		HIERARCHY.put(OWL2Datatype.XSD_NORMALIZED_STRING, OWL2Datatype.XSD_STRING);
		HIERARCHY.put(OWL2Datatype.XSD_TOKEN, OWL2Datatype.XSD_NORMALIZED_STRING);

		HIERARCHY.put(OWL2Datatype.XSD_LANGUAGE, OWL2Datatype.XSD_TOKEN);

		HIERARCHY.put(OWL2Datatype.XSD_NAME, OWL2Datatype.XSD_TOKEN);
		HIERARCHY.put(OWL2Datatype.XSD_NCNAME, OWL2Datatype.XSD_NAME);
		
		HIERARCHY.put(OWL2Datatype.XSD_NMTOKEN, OWL2Datatype.XSD_TOKEN);
	}
	
	
	public static boolean isSubType(OWL2Datatype type, OWL2Datatype subType) {
		if (type.equals(OWL2Datatype.XSD_ANY_URI))
			return true;
		else {
			OWL2Datatype cur = subType;
			while (cur != null) {
				if (cur.equals(type))
					return true;
				cur = HIERARCHY.get(cur);
			}
			return false;
		}
	}
	
	
	public static boolean isSubType(OWLDatatype type, OWLDatatype subType) {
		if (type.isBuiltIn() && subType.isBuiltIn()) {
			return isSubType(type.getBuiltInDatatype(), subType.getBuiltInDatatype());
		} else { // if it's not a built in datatype, we can only check if they are equal
			return type.equals(subType);
		}
	}
}
