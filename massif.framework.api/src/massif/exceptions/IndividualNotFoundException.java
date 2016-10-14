package massif.exceptions;

/**
 * IndividualNotFound Thrown when an indivual is not found in the ontology Example cause: individual not loaded from database
 * 
 * @author pbonte
 * @version 1.0
 */
@SuppressWarnings("serial")
public class IndividualNotFoundException extends Exception {
	public IndividualNotFoundException() {

	}

	public IndividualNotFoundException(String msg) {
		super(msg);
	}

	public IndividualNotFoundException(Throwable cause) {
		super(cause);
	}

	public IndividualNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}
}