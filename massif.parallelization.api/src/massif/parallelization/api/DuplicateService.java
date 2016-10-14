package massif.parallelization.api;

import massif.parallelization.api.util.ServiceUpdateSerializeable;

public interface DuplicateService {

	public ServiceUpdateSerializeable generateServiceUpdate();
	
	public void updateService(ServiceUpdateSerializeable update);
	
	public int getQueueSize();
	
}
