package massif.exceptions;

/**
 * CorruptCacheException Used when the functionality of the cache fails
 * 
 * @author Pieter De Buysser
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CorruptCacheException extends Exception {
	public CorruptCacheException() {
	}

	public CorruptCacheException(String msg) {
		super(msg);
	}

	public CorruptCacheException(Throwable cause) {
		super(cause);
	}

	public CorruptCacheException(String msg, Throwable cause) {
		super(msg, cause);
	}
}