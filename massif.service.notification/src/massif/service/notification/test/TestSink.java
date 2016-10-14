package massif.service.notification.test;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import massif.framework.dashboard.api.Sink;
import util.httpclient.HttpClient;

public class TestSink implements Sink{

	private Object respons;
	private boolean transmit;
	private String url;
	public TestSink(){
		transmit = false;
	}
	public TestSink(String url){
		super();
		this.url = url;
	}
	@Override
	public boolean send(Object obj) {
		this.respons = obj;
		if(transmit){
			try {
				if(obj instanceof List){
					Object json = JSONObject.toJSONString((Map)((List)obj).get(0));
					HttpClient.post(url, json.toString());
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}
		return true;
	}
	
	public Object getResponse(){
		return respons;
	}

	public void reset(){
		respons = null;
	}
	public boolean isResultIn(){
		return respons!=null;
	}
	public void setTrasmitting(boolean transmit){
		this.transmit = transmit;
	}
}
