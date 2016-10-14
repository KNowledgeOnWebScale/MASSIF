package massif.parallelization.localconfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.parallelization.api.DuplicateService;
import massif.parallelization.api.LocalConfigurator;
import massif.parallelization.api.util.ServiceUpdate;
import massif.parallelization.api.util.ServiceUpdateSerializeable;

/**
 * Configures the number of duplicate services in a local osgi container. A
 * number of active services can be indicated through the configuration manager.
 * *param <instances> indicates the number of active instances. *param
 * <factoryPID> indicates the servicec.pid of the to be duplicated service
 *
 * @author pbonte
 *
 */
@Component(immediate = true, property = { "service.pid=massif.distr.localconfig.LocalConfiguratorImpl",
		"massif.parallel.node=local", "aiolos.unique=true" })
public class LocalConfiguratorImpl implements LocalConfigurator, ManagedService {

	private ConfigurationAdmin confAdmin;
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	private BundleContext context;

	// service properties
	private final String IDNAME = "massif.parallel.serviceid";
	private final String UNIQUENAME = "massif.parallel.unique";
	private final String ORIGINAL_IDENTIFIER = "aiolos.service.id";

	public LocalConfiguratorImpl() {
		System.out.println("Constuctor called");
	}

	@Activate
	public void start(BundleContext context) {
		this.context = context;
	}

	@Reference(unbind = "unbindConfAdmin", policy = ReferencePolicy.DYNAMIC)
	public void bindConfAdmin(ConfigurationAdmin confAdmin) {
		logger.info("Binded configAdmin");
		this.confAdmin = confAdmin;
	}

	public void unbindConfAdmin(ConfigurationAdmin confAdmin) {
		if (this.confAdmin == confAdmin) {
			this.confAdmin = null;
		}
	}

	private boolean isServiceActive(String pid, String clazz) {
		try {
			ServiceReference[] refs = context.getAllServiceReferences(clazz, "(service.pid=" + pid + ")");
			if (refs == null) {
				refs = context.getAllServiceReferences(clazz, "(component.name=" + pid + ")");
			}
			if (refs != null) {
				return refs.length > 0;
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties == null) {
			return;
		}
		logger.info("Updating");
		// retrieve service id that should be managed
		String factoryPid = (String) properties.get("factoryPID");
		// retrieve number of instances
		String updatedInstances = (String) properties.get("instances");
		String clazz = (String) properties.get("clazz");
		String originalID = (String) properties.get("originalID");
		boolean update = "true".equals((String) properties.get("update")) ? true : false;
		try {
			// Retrieve current instances registered as service factory
			Configuration[] currentConfigs = confAdmin.listConfigurations("(service.factoryPID=" + factoryPid + ")");
			// retrieve the original configuration
			Configuration[] originalConfig = confAdmin.listConfigurations("(service.PID=" + factoryPid + ")");
			boolean isActive = isServiceActive(factoryPid, clazz);
			if (originalConfig == null && !isActive) {
				logger.error("No config found for <" + factoryPid + ">.");
			} else if (originalConfig == null && isActive) {
				// create a new configuration
				Configuration newConf = confAdmin.getConfiguration(factoryPid, null);
				Dictionary<String, String> dict = new Hashtable();
				dict.put(IDNAME, UUID.randomUUID().toString());
				dict.put(UNIQUENAME, "true");
				newConf.update(dict);
				Thread.sleep(2000);
				originalConfig = confAdmin.listConfigurations("(service.PID=" + factoryPid + ")");
			}
			// if null, there are no configs active and we should not create a
			// factory for the pid
			if (originalConfig != null && updatedInstances != null) {
				int instances = 0; // if no configs exists, no factory services
									// are registered
				if (currentConfigs != null) {
					instances = currentConfigs.length;
				}
				int updatedInstancesInt = Integer.parseInt(updatedInstances);
				if (updatedInstancesInt > instances) {
					// start #(updatedInstancesInt - instances) new services
					int numberOfnewInstances = updatedInstancesInt - instances;
					logger.info("Starting new " + numberOfnewInstances + " instances");
					for (int i = 0; i < numberOfnewInstances; i++) {
						// create new service
						String configID = confAdmin.createFactoryConfiguration(factoryPid, null).getPid();
						// retrieve the bundle location of the original bundle
						String locationBundle = confAdmin.getConfiguration(factoryPid).getBundleLocation();
						// create the new configuration
						Configuration createdConfig = confAdmin.getConfiguration(configID);
						// adapt bundle location
						createdConfig.setBundleLocation(locationBundle);
						// create id
						Dictionary<String, String> dict = new Hashtable();
						String uuid = UUID.randomUUID().toString();
						dict.put(IDNAME, uuid);
						dict.put(UNIQUENAME, "false");
						// update services
						createdConfig.update(dict);
						if (update) {
							updateService(clazz, originalID, uuid);
						}
					}
				} else if (updatedInstancesInt < instances) {
					// stop #(instances - updatedInstancesInt) services
					logger.info("Stopping " + (instances - updatedInstancesInt) + " instances");
					for (int i = instances; i > updatedInstancesInt && i > 0; i--) {
						// retrieve last configs from the registered
						// configurations
						Configuration stopConfig = currentConfigs[i - 1];
						// by calling delete on a managed factory service, the
						// deactivate method is invoked
						stopConfig.delete();
					}
				}

			}
		} catch (Exception e) {
			logger.error("Could not modify number of services", e);
		}

	}

	public void updateService(String clazz, String original, String duplicate) {
		try {
			@SuppressWarnings("rawtypes")
			ServiceReference[] originalRef = context.getAllServiceReferences(clazz,
					"(" + ORIGINAL_IDENTIFIER + "=" + original + ")");
			logger.info(originalRef.length + " : number of originals");
			for (ServiceReference ref : originalRef) {
				DuplicateService originalServiceTemp = (DuplicateService) context.getService(ref);
				originalServiceTemp.generateServiceUpdate();
			}
			DuplicateService originalService = (DuplicateService) context.getService(originalRef[0]);
			DuplicateTracker dTracker = new DuplicateTracker(originalService);
			ServiceTracker tracker = new ServiceTracker<DuplicateService, DuplicateService>(context,
					context.createFilter("(" + IDNAME + "=" + duplicate + ")"), dTracker);
			dTracker.setTracker(tracker);
			tracker.open();
			// create serviceTracker to
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Modified
	public void modified() {

	}

	@Override
	public void startServices(int number, String pid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopServices(int number, String pid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(int number, String clazz, String pid, String componentName, String originalID) {
		// TODO Auto-generated method stub
		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict.put("factoryPID", pid);
		dict.put("instances", number + "");
		dict.put("clazz", clazz);
		dict.put("componentName", componentName);
		dict.put("originalID", originalID);

		try {
			updated(dict);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class DuplicateTracker implements ServiceTrackerCustomizer<DuplicateService, DuplicateService> {

		private DuplicateService original;
		private ServiceTracker tracker;

		public DuplicateTracker(DuplicateService original) {
			this.original = original;
		}

		@Override
		public DuplicateService addingService(ServiceReference<DuplicateService> reference) {
			// TODO Auto-generated method stub
			DuplicateService duplicate = context.getService(reference);
			// update the service
			ServiceUpdateSerializeable update = original.generateServiceUpdate();
			duplicate.updateService(update);
			// stop the tracker
			tracker.close();
			return duplicate;
		}

		@Override
		public void modifiedService(ServiceReference<DuplicateService> reference, DuplicateService service) {
			// TODO Auto-generated method stub

		}

		@Override
		public void removedService(ServiceReference<DuplicateService> reference, DuplicateService service) {
			// TODO Auto-generated method stub

		}

		public void setTracker(ServiceTracker tracker) {
			this.tracker = tracker;
		}

	}

	@Override
	public boolean startService(String factoryPid, Dictionary<String, Object> newServiceProperties) {
		logger.info("Updating " + factoryPid);

		try {
			// Retrieve current instances registered as service factory
			Configuration[] currentConfigs = confAdmin.listConfigurations("(service.factoryPID=" + factoryPid + ")");
			// retrieve the original configuration
			Configuration[] originalConfig = confAdmin.listConfigurations("(service.PID=" + factoryPid + ")");
			if (originalConfig == null) {
				// create a new configuration
				Configuration newConf = confAdmin.getConfiguration(factoryPid, null);
				Dictionary<String, String> dict = new Hashtable();
				dict.put(IDNAME, UUID.randomUUID().toString());
				dict.put(UNIQUENAME, "true");
				newConf.update(dict);
				Thread.sleep(2000);
				originalConfig = confAdmin.listConfigurations("(service.PID=" + factoryPid + ")");
			}
			// if null, there are no configs active and we should not create a
			// factory for the pid
			if (originalConfig != null) {

				logger.info("Starting new 1 instances");
				// create new service
				String configID = confAdmin.createFactoryConfiguration(factoryPid, null).getPid();
				// retrieve the bundle location of the original bundle
				String locationBundle = confAdmin.getConfiguration(factoryPid).getBundleLocation();
				locationBundle = "?";
				// create the new configuration
				Configuration createdConfig = confAdmin.getConfiguration(configID);
				// adapt bundle location
				createdConfig.setBundleLocation(locationBundle);
				// create id
				Dictionary<String, String> dict = new Hashtable();
				String uuid = UUID.randomUUID().toString();
				newServiceProperties.put(IDNAME, uuid);
				newServiceProperties.put(UNIQUENAME, "false");
				// update services
				createdConfig.update(newServiceProperties);

			}
		} catch (Exception e) {
			logger.error("Could not modify number of services", e);
			return false;
		}
		return true;
	}

}
