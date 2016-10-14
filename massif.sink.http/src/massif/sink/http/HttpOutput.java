package massif.sink.http;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.framework.dashboard.api.Dashboard;
import massif.framework.dashboard.api.Sink;
import massif.framework.dashboard.api.VirtualSink;
import util.httpclient.HttpClient;

@Component
public class HttpOutput implements Sink {

	private final String DYAMAND = "http://localhost:8080/react";
	private VirtualSink virtualSink;
	final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	public boolean send(Object obj) {
		try {
			if(obj instanceof List){
				Map resultMap = (Map)((List)obj).get(0);
				if(resultMap.containsKey("id") && virtualSink!= null && virtualSink.isVirtualSensor((String)resultMap.get("id"))){
					virtualSink.send(resultMap);
				}else{
					Object json = JSONObject.toJSONString((Map)((List)obj).get(0));
					HttpClient.post(DYAMAND, json.toString());
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	@Reference(unbind = "unbindVirtualSink", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void bindVirtualSink(VirtualSink virtualSink){
		this.virtualSink = virtualSink;
		logger.info("Binding dashboard");
	}
	public void unbindVirtualSink(VirtualSink virtualSink){
		if(virtualSink == this.virtualSink){
			this.virtualSink = null;
		}
	}
	// TODO: class provided by template

}
