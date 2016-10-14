package util.httpclient;

/**
 * This class represents a field for a http header
 * @author JSchabal
 *
 */
public class HttpHeaderField {
	
	private String key;
	private String data;
	
	// Constructor
	public HttpHeaderField(String key, String data) {
		this.key = key;
		this.data = data;
	}
	
	/** Getters */
	public String getKey() { return key; }
	public String getData() { return data; }
	
}
