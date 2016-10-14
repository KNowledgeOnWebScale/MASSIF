package massif.exceptions;

public class IllegalBusBindSequence extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public IllegalBusBindSequence(){
		super();
	}
	public IllegalBusBindSequence(String message){
		super(message);
	}
	public IllegalBusBindSequence(String message, Throwable cause){
		super(message, cause);
	}
	public IllegalBusBindSequence(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
		super(message, cause, enableSuppression, writableStackTrace);
	}
	public IllegalBusBindSequence(Throwable cause){
		super(cause);
	}
	
}
