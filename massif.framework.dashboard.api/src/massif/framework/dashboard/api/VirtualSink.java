package massif.framework.dashboard.api;

public interface VirtualSink {

	public boolean isVirtualSensor(String sensorId);
	
	public void send(Object object);
}
