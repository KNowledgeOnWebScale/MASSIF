package util.httpclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * This class will handle the Http requests
 * @author JSchabal
 *
 */
public class HttpClient {

	public interface Messages {
		
		// Used when a message is received on the http client
		void receivedMessage(String message);
	}
	
	/**
	 * This will do a GET request to the given url
	 * @param url		The url you want to query
	 * @param fields	The extra fields that you want to add
	 * @return			The response string
	 */
	public static String request(String url, HttpHeaderField ... fields) throws Exception {				
		// Variable that will handle the http communication
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setInstanceFollowRedirects(false);

		// Set the needed headers
		con.setRequestProperty("User-Agent", "RAMP Agent");
		
		if (fields != null && fields.length > 0)
			for (HttpHeaderField field : fields)
				con.addRequestProperty(field.getKey(), field.getData());
				
		// Start the request and check if success
		if (con.getResponseCode() == 200) {
			StringBuffer response = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
				response.append(line);
			
			reader.close();
			con.disconnect();
			
			// Return the response
			return response.toString();
		}
		
		throw new Exception(con.getResponseCode() + ": " + con.getResponseMessage());
	}
	
	/**
	 * This will do a PUT request to the given url
	 * @param url		The url you want to query
	 * @param content	The content you want to add to this put command
	 * @param fields	The extra fields that you want to add
	 * @return			The response string
	 */
	public static String put(String url, String content, HttpHeaderField ... fields) throws Exception {
		// Variable that will handle the http communication
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setInstanceFollowRedirects(false);
		con.setDoOutput(true);
		con.setDoInput(true);
		
		if (fields != null && fields.length > 0)
			for (HttpHeaderField field : fields)
				con.addRequestProperty(field.getKey(), field.getData());
		
		// Set the needed headers
		con.setRequestMethod("PUT");
		con.setRequestProperty("User-Agent", "RAMP Agent");		
		
		// Write content
		OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
		writer.write(content);
		writer.flush();
		writer.close();
			
		// Start the request and check if success
		if (con.getResponseCode() == 200 || con.getResponseCode() == 201 || con.getResponseCode() == 202 || con.getResponseCode() == 204) {
			StringBuffer response = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
				response.append(line);
			
			reader.close();
			con.disconnect();
			
			// Return the response
			return response.toString();
		}
		
		throw new Exception("Could not connect or server did not accept.");
	}
	
	/**
	 * This will do a POST request to the given url
	 * @param url		The url you want to query
	 * @param content	The content you want to add to this put command
	 * @param fields	The extra fields that you want to add
	 * @return			The response string
	 */
	public static String post(String url, String content, HttpHeaderField ... fields) throws Exception {
		// Variable that will handle the http communication
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setInstanceFollowRedirects(false);
		con.setDoOutput(true);
		con.setDoInput(true);
		
		if (fields != null && fields.length > 0)
			for (HttpHeaderField field : fields)
				con.addRequestProperty(field.getKey(), field.getData());
		
		// Set the needed headers
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "RAMP Agent");		
		
		// Write content
		OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
		writer.write(content);
		writer.flush();
		writer.close();
			
		// Start the request and check if success
		if (con.getResponseCode() == 200 || con.getResponseCode() == 201 || con.getResponseCode() == 202) {
			StringBuffer response = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
				response.append(line);
			
			reader.close();
			con.disconnect();
			
			// Return the response
			return response.toString();
		}
		
		throw new Exception("Could not connect or server did not accept.");
	}
	
	/**
	 * This will encode the value so it can be safely used in a query 
	 * @param value				The value that you want to encode
	 * @return					The encoded value
	 */
	public static String encodeValue(String value) throws Exception {
		return URLEncoder.encode(value, "ISO-8859-1");
	}
	
}
