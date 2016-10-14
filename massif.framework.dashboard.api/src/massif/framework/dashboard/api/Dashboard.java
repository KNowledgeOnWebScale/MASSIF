package massif.framework.dashboard.api;

import java.util.Map;

public interface Dashboard {
	
	public void sensorUpdate(Map<String,Object> event);

}
