package massif.parallelization.api;

public interface DistributionConfigurator {
	
	public void update(String node, int number, String clazz, String pid, String componentName);

}
