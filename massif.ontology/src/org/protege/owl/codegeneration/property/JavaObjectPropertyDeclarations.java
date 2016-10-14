package org.protege.owl.codegeneration.property;

import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY_RANGE;
import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY_RANGE_IMPLEMENTATION;

import java.util.Map;

import org.protege.owl.codegeneration.Constants;
import org.protege.owl.codegeneration.SubstitutionVariable;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * This class represents the following java methods that are associated with an OWL object property:
 * <pre>
 *     private ${propertyRange} ${owlPropertySmall};
 * 
 *     Collection<? extends ${propertyRange}> getAll${OwlProperty}();
 *     ${propertyRange} get${OwlProperty}();
 *     boolean has${OwlProperty}();
 *     void add${OwlProperty}(${propertyRange} new${OwlProperty});
 *     void remove${OwlProperty}(${propertyRange} old${OwlProperty});
 *     void set${OwlProperty}(${propertyRange} new${OwlProperty});
 * </pre>
 * Note that these methods do not get specialized as we move to subclasses.
 * <p/>
 * @author tredmond
 */
public class JavaObjectPropertyDeclarations implements JavaPropertyDeclarations {
	private CodeGenerationInference inference;
	private CodeGenerationNames names;
	private OWLObjectProperty property;
	
	public JavaObjectPropertyDeclarations(CodeGenerationInference inference, CodeGenerationNames names, 
			                              OWLObjectProperty property) {
		this.inference = inference;
		this.names     = names;
		this.property  = property;
	}
	
	@Override
	public OWLObjectProperty getOwlProperty() {
		return property;
	}

	@Override
	public JavaPropertyDeclarations specializeTo(OWLClass subclass) {
		return this; // no specialization is done...
	}

	@Override
	public void configureSubstitutions(Map<SubstitutionVariable, String> substitutions) {
        substitutions.put(PROPERTY_RANGE_IMPLEMENTATION, getObjectPropertyRange(false));
        substitutions.put(PROPERTY_RANGE, getObjectPropertyRange(true));
	}

	private String getObjectPropertyRange(boolean isInterface) {
		OWLClass range = inference.getRange(property);
		if (range == null || !inference.getOwlClasses().contains(range)) {
			return isInterface ? Constants.UKNOWN_CODE_GENERATED_INTERFACE : Constants.ABSTRACT_CODE_GENERATOR_INDIVIDUAL_CLASS;
		}
		return isInterface ? names.getInterfaceName(range) : names.getImplementationName(range);
	}
}
