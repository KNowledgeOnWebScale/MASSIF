package massif.exceptions;

/**
 * IllegalPacketException Used when an incoming packet is not readable
 * 
 * @author Pieter De Buysser
 * @version 1.0
 */
@SuppressWarnings("serial")
public class IllegalPacketException extends Exception {
	public IllegalPacketException() {

	}

	public IllegalPacketException(String msg) {
		super(msg);
	}

	public IllegalPacketException(Throwable cause) {
		super(cause);
	}

	public IllegalPacketException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
