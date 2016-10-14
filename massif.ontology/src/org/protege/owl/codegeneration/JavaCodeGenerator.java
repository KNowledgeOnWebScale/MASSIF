package org.protege.owl.codegeneration;

import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_CLASS_VOCABULARY;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_CONTEXT_HEADER;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_CONTEXT_INTERFACE;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_CONTEXT_TAIL;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_DATA_PROPERTY_IMPLEMENTATION;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_DATA_PROPERTY_INTERFACE;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_DATA_PROPERTY_VOCABULARY;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_FACTORY_CLASS;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_FACTORY_HEADER;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_FACTORY_TAIL;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_IMPLEMENTATION_HEADER;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_IMPLEMENTATION_TAIL;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_INTERFACE_HEADER;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_INTERFACE_TAIL;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_OBJECT_PROPERTY_IMPLEMENTATION;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_OBJECT_PROPERTY_INTERFACE;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_OBJECT_PROPERTY_VOCABULARY;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_VOCABULARY_HEADER;
import static org.protege.owl.codegeneration.CodeGenerationPhase.CREATE_VOCABULARY_TAIL;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * A class that can create Java interfaces in the Protege-OWL format
 * 
 * @author z.khan
 * 
 */
public class JavaCodeGenerator {
	

	private Worker worker;

	/**
	 * Constructor
	 * 
	 * @param owlOntology
	 * @param options
	 */
	public JavaCodeGenerator(Worker worker) {
		this.worker = worker;
		worker.initialize();
	}

	/**
	 * Initiates the code generation
	 * 
	 * @param reasoner
	 * @throws IOException
	 */
	public void createAll() throws IOException {
		Collection<OWLClass> owlClassList = worker.getOwlClasses();
		printVocabularyCode(owlClassList);
		printFactoryClassCode(owlClassList);
		printContextEngineInterfaceCode(owlClassList);
		for (OWLClass owlClass : owlClassList) {
			createInterface(owlClass);
			createImplementation(owlClass);
		}
	}

	/**
	 * Generates interface code for the provided OWlClass
	 * 
	 * @param owlClass
	 *            The class whose interface code is to generated
	 * @throws IOException
	 */
	private void createInterface(OWLClass owlClass) throws IOException {
		File baseFile = worker.getInterfaceFile(owlClass);
		FileWriter fileWriter = new FileWriter(baseFile);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printInterfaceCode(owlClass, printWriter);
		printWriter.close();
	}

	/**
	 * Writes the interface code for the provided OWlClass to the PrintStream
	 * 
	 * @param interfaceName
	 * @param owlClass
	 * @param printWriter
	 */
	private void printInterfaceCode(OWLClass owlClass, PrintWriter printWriter) {
		Collection<OWLObjectProperty> owlObjectProperties = worker
				.getObjectPropertiesForClass(owlClass);
		this.removeSuperObjectProperties(owlClass, owlObjectProperties,true);
		Collection<OWLDataProperty> owlDataProperties = worker
				.getDataPropertiesForClass(owlClass);
		this.removeSuperDataProperties(owlClass, owlDataProperties,true);

		Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(
				SubstitutionVariable.class);

		fillAndWriteTemplate(printWriter, CREATE_INTERFACE_HEADER,
				substitutions, owlClass, null);

		for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
			fillAndWriteTemplate(printWriter, CREATE_OBJECT_PROPERTY_INTERFACE,
					substitutions, owlClass, owlObjectProperty);
		}

		for (OWLDataProperty owlDataProperty : owlDataProperties) {
			fillAndWriteTemplate(printWriter, CREATE_DATA_PROPERTY_INTERFACE,
					substitutions, owlClass, owlDataProperty);
		}

		fillAndWriteTemplate(printWriter, CREATE_INTERFACE_TAIL, substitutions,
				owlClass, null);
	}

	private void createImplementation(OWLClass owlClass) throws IOException {
		File baseFile = worker.getImplementationFile(owlClass);
		FileWriter fileWriter = new FileWriter(baseFile);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printImplementationCode(owlClass, printWriter);
		printWriter.close();
	}

	private void printImplementationCode(OWLClass owlClass,
			PrintWriter printWriter) {
		Collection<OWLObjectProperty> owlObjectProperties = worker
				.getObjectPropertiesForClass(owlClass);
		this.removeSuperObjectProperties(owlClass, owlObjectProperties,false);
		Collection<OWLDataProperty> owlDataProperties = worker
				.getDataPropertiesForClass(owlClass);
		this.removeSuperDataProperties(owlClass, owlDataProperties,false);

		Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(
				SubstitutionVariable.class);

		fillAndWriteTemplate(printWriter, CREATE_IMPLEMENTATION_HEADER,
				substitutions, owlClass, null);

		for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
			fillAndWriteTemplate(printWriter,
					CREATE_OBJECT_PROPERTY_IMPLEMENTATION, substitutions,
					owlClass, owlObjectProperty);
		}

		for (OWLDataProperty owlDataProperty : owlDataProperties) {
			fillAndWriteTemplate(printWriter,
					CREATE_DATA_PROPERTY_IMPLEMENTATION, substitutions,
					owlClass, owlDataProperty);
		}

		fillAndWriteTemplate(printWriter, CREATE_IMPLEMENTATION_TAIL,
				substitutions, owlClass, null);
	}

	public void removeSuperObjectProperties(OWLClass owlClass,
			Collection<OWLObjectProperty> owlObjectProperties, boolean inface) {
		OWLClass supClass = this.getBaseImplementation(owlClass);

		if (inface)
			for (OWLClass superClass : worker.getCodeGenerationInference()
					.getSuperClasses(owlClass)) {
				if (worker.getCodeGenerationInference().getOwlClasses()
						.contains(superClass)) {
					
						owlObjectProperties.removeAll(worker.getObjectPropertiesForClass(superClass));
				}
			}

		owlObjectProperties.removeAll(worker
				.getObjectPropertiesForClass(supClass));
	}

	public void removeSuperDataProperties(OWLClass owlClass,
			Collection<OWLDataProperty> owlDataProperties, boolean inface) {

		OWLClass supClass = this.getBaseImplementation(owlClass);

		if (inface)
			for (OWLClass superClass : worker.getCodeGenerationInference()
					.getSuperClasses(owlClass)) {
				if (worker.getCodeGenerationInference().getOwlClasses()
						.contains(superClass)) {
					
						owlDataProperties.removeAll(worker
								.getDataPropertiesForClass(superClass));
				}
			}

		owlDataProperties.removeAll(worker.getDataPropertiesForClass(supClass));
	}

	/**
	 * Returns base interface of the provided OWLClass
	 * 
	 * @param owlClass
	 *            The OWLClass whose base interface is to be returned
	 * @return
	 */
	private OWLClass getBaseImplementation(OWLClass owlClass) {
		int properties = -1;
		int temp;
		OWLClass supClass = null;
		for (OWLClass superClass : worker.getCodeGenerationInference()
				.getSuperClasses(owlClass)) {
			if (worker.getCodeGenerationInference().getOwlClasses()
					.contains(superClass)) {
				temp = worker.getObjectPropertiesForClass(superClass).size()
						+ worker.getDataPropertiesForClass(superClass).size();
				if (properties < temp) {
					properties = temp;
					supClass = superClass;
				}
			}
		}
		return supClass;
	}

	/**
	 * Initilizes the vocabulary code generation
	 * 
	 * @param owlClassList
	 * @throws IOException
	 */
	private void printVocabularyCode(Collection<OWLClass> owlClassList)
			throws IOException {
		File vocabularyFile = worker.getVocabularyFile();
		FileWriter vocabularyfileWriter = new FileWriter(vocabularyFile);
		PrintWriter vocabularyPrintWriter = new PrintWriter(
				vocabularyfileWriter);
		Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(
				SubstitutionVariable.class);
		fillAndWriteTemplate(vocabularyPrintWriter, CREATE_VOCABULARY_HEADER,
				substitutions, null, null);

		for (OWLClass owlClass : owlClassList) {
			fillAndWriteTemplate(vocabularyPrintWriter,
					CREATE_CLASS_VOCABULARY, substitutions, owlClass, null);
		}

		for (OWLObjectProperty owlObjectProperty : worker
				.getOwlObjectProperties()) {
			fillAndWriteTemplate(vocabularyPrintWriter,
					CREATE_OBJECT_PROPERTY_VOCABULARY, substitutions, null,
					owlObjectProperty);
		}

		for (OWLDataProperty owlDataProperty : worker.getOwlDataProperties()) {
			fillAndWriteTemplate(vocabularyPrintWriter,
					CREATE_DATA_PROPERTY_VOCABULARY, substitutions, null,
					owlDataProperty);
		}

		fillAndWriteTemplate(vocabularyPrintWriter, CREATE_VOCABULARY_TAIL,
				substitutions, null, null);

		vocabularyPrintWriter.close();
	}

	/**
	 * Initializes the code generation for factory classes
	 * 
	 * @param owlClassList
	 * @throws IOException
	 */
	private void printFactoryClassCode(Collection<OWLClass> owlClassList)
			throws IOException {
		FileWriter factoryFileWriter = null;
		PrintWriter factoryPrintWriter = null;
		File factoryFile = worker.getFactoryFile();
		factoryFileWriter = new FileWriter(factoryFile);
		factoryPrintWriter = new PrintWriter(factoryFileWriter);

		Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(
				SubstitutionVariable.class);

		fillAndWriteTemplate(factoryPrintWriter, CREATE_FACTORY_HEADER,
				substitutions, null, null);

		for (OWLClass owlClass : owlClassList) {
			fillAndWriteTemplate(factoryPrintWriter, CREATE_FACTORY_CLASS,
					substitutions, owlClass, null);
		}

		fillAndWriteTemplate(factoryPrintWriter, CREATE_FACTORY_TAIL,
				substitutions, null, null);

		factoryPrintWriter.close();
	}

	private void printContextEngineInterfaceCode(
			Collection<OWLClass> owlClassList) throws IOException {
		FileWriter factoryFileWriter = null;
		PrintWriter factoryPrintWriter = null;
		File factoryFile = worker.getContextEngineFile();
		if (factoryFile == null)
			return;
		factoryFileWriter = new FileWriter(factoryFile);
		factoryPrintWriter = new PrintWriter(factoryFileWriter);

		Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(
				SubstitutionVariable.class);

		fillAndWriteTemplate(factoryPrintWriter, CREATE_CONTEXT_HEADER,
				substitutions, null, null);

		for (OWLClass owlClass : owlClassList) {
			fillAndWriteTemplate(factoryPrintWriter, CREATE_CONTEXT_INTERFACE,
					substitutions, owlClass, null);
		}

		fillAndWriteTemplate(factoryPrintWriter, CREATE_CONTEXT_TAIL,
				substitutions, null, null);

		factoryPrintWriter.close();
	}

	private void fillAndWriteTemplate(PrintWriter writer,
			CodeGenerationPhase phase,
			Map<SubstitutionVariable, String> substitutions, OWLClass owlClass,
			OWLEntity owlProperty) {
		worker.configureSubstitutions(phase, substitutions, owlClass,
				owlProperty);
		String template = worker.getTemplate(phase, owlClass, owlProperty);
		fillTemplate(writer, template, substitutions);
	}

	public static void fillTemplate(PrintWriter writer, String template,
			Map<SubstitutionVariable, String> substitutions) {
		for (Entry<SubstitutionVariable, String> entry : substitutions
				.entrySet()) {
			SubstitutionVariable var = entry.getKey();
			String replacement = entry.getValue();
			template = template.replaceAll("\\$\\{" + var.getName() + "\\}",
					replacement);
		}
		writer.append(template);
	}

}
