package massif.matchingservice.util;

//import massif.api.contextadapterapi2.ContextAdapter;

public class AdapterPair {
	
	private Object adapter;
	private String type;
	
	public AdapterPair(Object adapter, String type){
		this.adapter = adapter;
		this.type = type;
	}
	public Object getAdapter(){
		return adapter;
	}
	public String getType(){
		return type;
	}

}
