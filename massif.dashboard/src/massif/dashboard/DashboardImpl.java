package massif.dashboard;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.framework.dashboard.api.AdaptableSCB;
import massif.framework.dashboard.api.AdaptableService;
import massif.framework.dashboard.api.Dashboard;
import massif.framework.dashboard.api.VirtualSink;
import massif.mciservice.api.MCIService;
import massif.parallelization.api.LocalConfigurator;

@Path("dashboard")
@Component(immediate = true,configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DashboardImpl implements Dashboard, VirtualSink{
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String, MCIService> activeServices = Collections.synchronizedMap(new HashMap<String, MCIService>());
	private ServiceInfoHolder serviceInfos = new ServiceInfoHolder();
	private SensorInfoHolder sensorInfos = new SensorInfoHolder();
	private StateHolder stateHolder = new StateHolder(serviceInfos,sensorInfos);
	private AdaptableSCB scb;
	private LocalConfigurator localConfig;

	@Activate
	public void start(ComponentContext context) {
		logger.info("Started Dashboard");
		Dictionary<String,Object> properties = context.getProperties();
		String sensors = (String) properties.get("sensors");
		for(String sensor: sensors.split(",")){
			URL url = context.getBundleContext().getBundle().getEntry(sensor.trim()+".sensor");
			Properties prop = new Properties();
			try {
				prop.load(url.openStream());
				SensorInfo virtualSensor = new SensorInfo(prop);
				sensorInfos.addSensorInfo(virtualSensor);
			} catch (Exception e) {
				logger.error("Unable to load prop <"+sensor+".sensor>",e);
			}
		}
	}
	// TODO: class provided by template

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response list() {
		if (stateHolder != null) {
			return Response.ok(stateHolder).build();
		} else {
			return Response.status(503).build();
		}
	}

	@Path("queries")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQueries(@QueryParam("serviceID") String serviceID) {
		System.out.println("called queries for service " + serviceID);

		if (activeServices.containsKey(serviceID) && activeServices.get(serviceID) instanceof AdaptableService) {
			logger.info("service active");
			List<String> queries = ((AdaptableService) activeServices.get(serviceID)).getQueries();
			System.out.println(queries);
			return Response.ok(queries).build();
		} else {
			logger.info("Service not active");
			return Response.noContent().build();
		}

	}
	@Path("/addQuery")
	@POST
	public Response addQuery(Map query) {
		System.out.println(query);
		if (query.containsKey("serviceID") && query.containsKey("newQuery")) {
			String serviceID = (String) query.get("serviceID");
			String newQuery = (String) query.get("newQuery");
			if (activeServices.containsKey(serviceID) && activeServices.get(serviceID) instanceof AdaptableService) {
				boolean success = ((AdaptableService) activeServices.get(serviceID)).addQuery(newQuery);
			}
		}

		return Response.ok(serviceInfos).build();
	}
	@Path("/removeQuery")
	@POST
	public Response removeQuery(Map query) {
		System.out.println("removing filter" + query);
		if (query.containsKey("serviceID") && query.containsKey("removeQuery")) {
			String serviceID = query.get("serviceID") + "";
			String removeQuery = (String) query.get("removeQuery");
			if (activeServices.containsKey(serviceID) && activeServices.get(serviceID) instanceof AdaptableService) {
				AdaptableService aService = ((AdaptableService) activeServices.get(serviceID));
				int index = -1;
				for(int i = 0; i< aService.getQueries().size(); i++){
					if(aService.getQueries().get(i).equals(removeQuery)){
						index = i;
						break;
					}
				}
				boolean success = false;
				if(index != -1){
					success = aService.removeQuery(index);
				}else{
					logger.error("Filter rule <"+removeQuery+"> not found in <"+aService.getQueries()+">.");
				}
				 
			}
		}

		return Response.ok(serviceInfos).build();
	}


	@Path("/addFilter")
	@POST
	public Response addFilter(Map filter) {
		System.out.println("added new filter" + filter);
		if (filter.containsKey("serviceID") && filter.containsKey("newFilter") && filter.containsKey("filterName")) {
			String serviceID = (String) filter.get("serviceID");
			String newFilter = (String) filter.get("newFilter");
			String filterName = (String)filter.get("filterName");
			if (activeServices.containsKey(serviceID) && activeServices.get(serviceID) instanceof AdaptableService) {
				boolean success = ((AdaptableService) activeServices.get(serviceID)).addFilterRule(filterName,newFilter);
			}
		}

		return Response.ok(serviceInfos).build();
	}
	@Path("/removeFilter")
	@POST
	public Response removeFilter(Map filter) {
		System.out.println("removing filter" + filter);
		if (filter.containsKey("serviceID") && filter.containsKey("removeFilter")) {
			String serviceID = filter.get("serviceID") + "";
			String removeFilter = (String) filter.get("removeFilter");
			if (activeServices.containsKey(serviceID) && activeServices.get(serviceID) instanceof AdaptableService) {
				AdaptableService aService = ((AdaptableService) activeServices.get(serviceID));
				int index = -1;
				for(int i = 0; i< aService.getFilterRules().size(); i++){
					if(aService.getFilterRules().get(i).equals(removeFilter)){
						index = i;
						break;
					}
				}
				boolean success = false;
				if(index != -1){
					success = aService.removeFilterRule(index);
				}else{
					logger.error("Filter rule <"+removeFilter+"> not found in <"+aService.getFilterRules()+">.");
				}
				 
			}
		}

		return Response.ok(serviceInfos).build();
	}
	
	@Path("/newService")
	@POST
	public Response newService(Map newService) {
		//TODO MAKE TYPE SAFE
		System.out.println("adding new service");
		Dictionary<String, Object> dict = new Hashtable<String,Object>();
		dict.put("name", newService.get("name"));
		if(newService.containsKey("description")){
			dict.put("description", newService.get("description"));
		}
		List<Map<String,String>> inputs = (List<Map<String,String>>)newService.get("input");
		for(int i =0;i<inputs.size();i++){
			dict.put("input."+i, inputs.get(i).get("input"));
			dict.put("inputName."+i, inputs.get(i).get("name"));
		}
		dict.put("ontology", newService.get("ontology"));
		List<Map<String,String>> queries = (List<Map<String,String>>) newService.get("query");
		for(int i = 0; i<queries.size();i++){
			dict.put("query."+i, queries.get(i).get("query"));
		}
		
		localConfig.startService("massif.generic.service.GenericService", dict);

		return Response.ok(serviceInfos).build();
	}

	@Path("/scb")
	@GET
	public Response getSCBOntology() {
		System.out.println("called");

		if (scb != null) {
			return Response.ok(scb.getOntologyPrint()).build();
		} else {
			return Response.status(503).build();
		}
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unbindServices")
	public void bindServices(MCIService service, Map<String, Object> properties) {
		String serviceId = properties.get("service.id").toString();
		activeServices.put(serviceId, service);
		serviceInfos.addServiceInfo(new ServiceInfo(service, properties));
	}

	public void unbindServices(MCIService service, Map<String, Object> properties) {
		String serviceId = properties.get("service.id").toString();
		activeServices.remove(serviceId);
		serviceInfos.removeServiceInfo(serviceId);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unbindSCB")
	public void bindSCB(AdaptableSCB scb, Map<String, Object> properties) {
		this.scb = scb;
	}

	public void unbindSCB(AdaptableSCB scb, Map<String, Object> properties) {
		if (this.scb == scb) {
			this.scb = null;
		}
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unbindLocalConfig")
	public void bindLocalConfig(LocalConfigurator localConf) {
		this.localConfig = localConf;
	}

	public void unbindLocalConfig(LocalConfigurator localConf) {
		if(this.localConfig == localConf){
			this.localConfig = null;
		}
	}

	@Override
	public void sensorUpdate(Map<String, Object> event) {
		// TODO Auto-generated method stub
		if(event.containsKey("ssn:observedBy")){
			String id = (String)((Map)event.get("ssn:observedBy")).get("upper:hasID");
			sensorInfos.sensorUpdate(id, event);
		}
		
	}

	@Override
	public boolean isVirtualSensor(String sensorId) {		
		return sensorInfos.isSensorVirtual(sensorId);
	}

	@Override
	public void send(Object object) {
		// TODO Auto-generated method stub
		Map<String, String> event = (Map<String, String>)object;
		if(event.containsKey("id") && event.containsKey("type")){
			sensorInfos.actuate(event.get("id"), event.get("type"));
		}
		System.out.println("Received; " + object);
	}
}
