package massif.exceptions;

@SuppressWarnings("serial")
public class NoBusFoundException extends Exception {
	public NoBusFoundException() {

	}

	public NoBusFoundException(String msg) {
		super(msg);
	}

	public NoBusFoundException(Throwable cause) {
		super(cause);
	}

	public NoBusFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
