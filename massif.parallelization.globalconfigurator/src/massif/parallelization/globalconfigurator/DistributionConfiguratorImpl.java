package massif.parallelization.globalconfigurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.parallelization.api.DistributionConfigurator;
import massif.parallelization.api.DuplicateService;
import massif.parallelization.api.LocalConfigurator;

@Component(immediate = true)
public class DistributionConfiguratorImpl implements DistributionConfigurator {

	// Logger
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, LocalConfigurator> configs;

	ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

	private List<DuplicateService> services;

	private Map<DuplicateService, Map> servicePropMapping; // maps all service
															// properties

	private Map<String, List<DuplicateService>> locationMapping;

	private final int ALLOWEDQUEUESIZE = 10;
	private final int ALLOWEDNUMBEROFSERVICES = 2;
	private final int NUMBER_OF_SCALING_UP = 1;
	private final int NUMBER_OF_SCALING_DOWN = 1;
	private final boolean FORCE_LOCAL_SCALING = true;
	
	private final String CLAZZNAME = "massif.parallelization.api.DuplicateService";
	private final String FACTORYPID = "massif.testing.duplicateService.TestServiceWithQueue";
	private final String COMPONENTNAME = "massif.testing.duplicateService";
	private final String UNIQUEUESERVICE = "massif.parallel.unique";
	
	private final String ORIGINAL_IDENTIFIER = "aiolos.service.id";
	private String idOriginal = "";
	
	private boolean activePolling = false;

	int teller = 1;

	private Map<String, List<DuplicateService>> factoryMapping; // maps all
																// services with
																// same pid

	private Map<String, DuplicateService> originalMapping; // maps original
															// services
	
	private DuplicateService originalService;

	public DistributionConfiguratorImpl() {
		configs = new HashMap<String, LocalConfigurator>();
		services = new ArrayList<DuplicateService>();
		servicePropMapping = new HashMap<DuplicateService, Map>();
		factoryMapping = new HashMap<String, List<DuplicateService>>();
		originalMapping = new HashMap<String, DuplicateService>();
		locationMapping = new HashMap<String, List<DuplicateService>>();
	}

	private int calcAvg(List<Integer> values) {
		int avg = 0;
		for (Integer value : values) {
			avg += value;
		}
		return avg / values.size();
	}

	private int checkAVGLoad(List<DuplicateService> services){
		int avg = 0;
		for (DuplicateService service : services) {
			avg += service.getQueueSize();
		}
		return avg / services.size();
	}
	@Activate
	public void start(BundleContext context) {
		// start polling thread
		logger.info("Started");
		ex.scheduleAtFixedRate(new Runnable() {
			//TODO: check if the algorithm can keep running after scale up/down on one instance, maybe it should stop
			@Override
			public void run() {
				if(activePolling){
					try{
					logger.info("Scheduler activated");
					// iterate over all locations, first make a deep copy to avoid
					// concurrency issues
					Map<String, List<DuplicateService>> mapCpy = new HashMap<String, List<DuplicateService>>(locationMapping);
					int currentActiveNodes = mapCpy.size();
					int possibleNodes = configs.size();
					//check load in active nodes, only launch on new nodes if necessary
					for (String location : mapCpy.keySet()) {
						//check load for location x
						List<DuplicateService> servicesAtLocationX = new ArrayList<DuplicateService>(mapCpy.get(location));
						int activeServices = servicesAtLocationX.size();
						int avgLoad = checkAVGLoad(servicesAtLocationX);
						if(avgLoad>=ALLOWEDQUEUESIZE){
							//scale up
							if(activeServices < ALLOWEDNUMBEROFSERVICES){
								//no problem to start an extra service on the same node
								configs.get(location).update((activeServices -1 ) + NUMBER_OF_SCALING_UP, CLAZZNAME, FACTORYPID, COMPONENTNAME,idOriginal);
							}else{
								//find a node where the load is not as high
								String bestLocation = null;
								int minLoad = Integer.MAX_VALUE;
								for(String location2: mapCpy.keySet()){
									if(location != location2){
										int load = checkAVGLoad(new ArrayList<DuplicateService>(mapCpy.get(location)));
										if(load < minLoad){
											minLoad = load;
											bestLocation = location2;
										}
									}
								}
								if(bestLocation != null){
									//there is a better location to launch a new services
									configs.get(bestLocation).update((activeServices -1 ) + NUMBER_OF_SCALING_UP, CLAZZNAME, FACTORYPID, COMPONENTNAME,idOriginal);
								}else{
									//there is no better location currently active
									if(currentActiveNodes < possibleNodes){
										//launch a service on a new node
										String newNode = null;
										//find a node that has no services yet
										for(String node: configs.keySet()){
											if(!locationMapping.containsKey(node)){
												newNode = node;
												break;
											}
										}
										configs.get(newNode).update(-1 + NUMBER_OF_SCALING_UP, CLAZZNAME, FACTORYPID, COMPONENTNAME,idOriginal);
									}else{
									//will have to launch an extra service here
										if(FORCE_LOCAL_SCALING){//only for local scaling if allowed, else do nothing
											configs.get(location).update((activeServices -1 ) + NUMBER_OF_SCALING_UP, CLAZZNAME, FACTORYPID, COMPONENTNAME,idOriginal);		
										}else{
											logger.info("Unable to start extra services. The maximum number of allowed duplicates has been reached");
										}
									}
								}
							}
							
						}
						else if (avgLoad < ALLOWEDQUEUESIZE /2){
							//scale down
							int newNumberOfServices = activeServices - NUMBER_OF_SCALING_DOWN -1; 
							if(newNumberOfServices < -1){
								newNumberOfServices = -1;
							}
							if(location == "local" && newNumberOfServices < 0){ 
								//there should always keep 1 service running on the local node
								newNumberOfServices = 0;
							}
							configs.get(location).update(newNumberOfServices , CLAZZNAME, FACTORYPID, COMPONENTNAME,idOriginal);
							
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				}
			}

		}, 10, 10, TimeUnit.SECONDS);
	}

	@Reference(unbind = "unbindLocalConfigurator", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void bindLocalConfigurator(LocalConfigurator lconf, Map<String, Object> properties) {
		logger.info("binded local configurator:" + properties.toString()); 
		String location = (String) properties.get("aiolos.framework.uuid");
		if (location == null) {
			location = "local";
		}

		if (!configs.containsKey(location)) {
			configs.put(location, lconf);
		}

	}

	public void unbindLocalConfigurator(LocalConfigurator lconf, Map<String, Object> properties) {
		String node = (String) properties.get("massif.parallel.node");
		if (configs.containsKey(node)) {
			configs.remove(node);
		}
	}

	@Override
	public void update(String node, int number, String clazz, String pid, String componentName) {
		// TODO Auto-generated method stub
		logger.info("Updating");
		List<LocalConfigurator> foundConfigs = new ArrayList<LocalConfigurator>();
		LocalConfigurator conf = configs.get(node);

		if (conf == null) {
			if(node == "all"){
				foundConfigs.addAll(configs.values());
			}
			if(node == "allOthers"){
				foundConfigs.addAll(configs.values());
				LocalConfigurator localConf = configs.get("local");
				if(localConf != null){
					foundConfigs.remove(localConf);
				}
			}else{
				logger.error("No configurator for node <" + node + ">! Active nodes: " + configs.keySet());
			}
		} else {
			foundConfigs.add(conf);
		}
		for(LocalConfigurator config: foundConfigs){
			config.update(number, clazz, pid, componentName, idOriginal);
		}
	}

	@Reference(unbind = "unbindDistrServices", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void bindDistrServices(DuplicateService service, Map<String, String> props) {
		logger.info("Binding Duplicate Service: " + props.toString());
		if (!services.contains(service)) {
			services.add(service);
			servicePropMapping.put(service, props);
			String factoryPID = null;
			if (props.containsKey("service.factoryPid")) {
				factoryPID = props.get("service.factoryPid");
			} else {
				factoryPID = props.get("component.name");
				originalMapping.put(factoryPID, service);
			}
			if (!factoryMapping.containsKey(factoryPID)) {
				factoryMapping.put(factoryPID, new ArrayList<DuplicateService>());
			}
			factoryMapping.get(factoryPID).add(service);
			// Retrieve location
			String location = props.get("aiolos.framework.uuid");
			if (location == null) {
				location = "local";
			}
			if (!locationMapping.containsKey(location)) {
				locationMapping.put(location, new ArrayList<DuplicateService>());
			}
			locationMapping.get(location).add(service);
			
			//set original services as the first service that is activated
			if(originalService == null){
				originalService = service;
				idOriginal = props.get(ORIGINAL_IDENTIFIER);
				logger.info("Original on node <"+location+">");
			}
		}
	}

	public void unbindDistrServices(DuplicateService service, Map<String, String> props) {
		System.out.println(props);
		if (services.contains(service)) {
			services.remove(service);
			props = servicePropMapping.get(service);
			servicePropMapping.remove(service);
			String factoryPID = null;
			if (props.containsKey("service.factoryPid")) {
				factoryPID = props.get("service.factoryPid");
			} else {
				factoryPID = props.get("component.name");
				originalMapping.remove(factoryPID);
			}
			if (factoryMapping.containsKey(factoryPID)) {
				factoryMapping.get(factoryPID).remove(service);
			}
			// rertrieve location
			String location = props.get("aiolos.framework.uuid");
			if (location == null) {
				location = "local";
			}
			if (locationMapping.containsKey(location)) {
				locationMapping.get(location).remove(service);
			}
			if(service == originalService){
				logger.error("The original services has been deactived. Selected random services as orignal(if present)...");
				if(services.iterator().hasNext()){
					originalService = services.iterator().next();
					idOriginal = (String) servicePropMapping.get(originalService).get(ORIGINAL_IDENTIFIER);
				}else{
					originalService = null;
				}
			}
		}
	}
	
	@Modified
	public void updateConfig(Map<String,String> properties){
		logger.info("Updating: " + properties.toString());
		String node = (String) properties.get("node");
		String factoryPid = (String) properties.get("factoryPID");
		// retrieve number of instances
		String updatedInstances = (String) properties.get("instances");	
		String clazz = (String) properties.get("class");
		String componentName = (String) properties.get("component");
		activePolling = Boolean.parseBoolean(properties.get("activePolling").toString());
		update(node, Integer.parseInt(updatedInstances), clazz, factoryPid, componentName);
		
	}

}
