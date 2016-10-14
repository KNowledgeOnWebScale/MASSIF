package massif.gateway.gatewayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.core.MediaType;








import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import massif.api.gatewayapi.GatewayOut;

//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
/**
 * @author pbonte
 *
 * Sep 26, 2013
 */
public class GatewayOutImpl {//implements GatewayOut {
	
	/*private WebResource resource;
	private String response="";
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean debug;
	public void start(BundleContext context){
		Properties properties = null;
		InputStream in;

		try {
			in = context.getBundle().getEntry("/resources/properties.properties").openStream();
			properties = new Properties();
			properties.load(in);
			in.close();
			debug = properties.getProperty("debug").equals("true")?true:false;
			if(!debug){
				Client client = Client.create();
				resource = client.resource(properties.getProperty("destination"));
			}
		} catch (IOException e) {
			logger.error("Could not read property for <" + context.getBundle().getEntry("/resources/properties.properties") + ">.", e);
		} catch (Exception e) {
			logger.error(e.toString());
		}
		
		
	}
	@Override
	public void transmitOut(String metaFragment) {
		if(!debug){
			ClientResponse response = resource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, metaFragment);
		}else{
			response=metaFragment;
			System.out.println("DummyGateWayOut has sent message: "+metaFragment);
		}
	}
	@Override
	public String getResponse() {
		return response;
	}*/

}


