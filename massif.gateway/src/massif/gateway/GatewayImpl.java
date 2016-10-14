package massif.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import massif.journal.api.JournalService;
import massif.matchingservice.api.MatchingService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Gateway serves as an entry point for the MASSIF platform.
 * It receives data in the JSON-format and forwards it to the Matching Services for further analysis.
 * 
 * The class is provided as an REST-services.
 * 
 * @author Pieter Bonte
 *
 */
@Path("gateway")
@Component(immediate=true,service=Object.class)
public class GatewayImpl {

	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(GatewayImpl.class);

	// An id that can be traced throughout the system
	private volatile AtomicLong packetID = new AtomicLong();
	
	/** Journaling service */
	private JournalService journalService;
	
	/** Worker pool to handle incoming message */
	private ExecutorService workerpool = Executors.newFixedThreadPool(4);

	/** List of matching service accepting messages */
	private List<MatchingService> matchingServices = new ArrayList<MatchingService>();;
		
	private static BundleContext owncontext;
	
	static BundleContext getContext() {
		return owncontext;
	}

	/**
	 * Called when the service is activated.
	 * @param context
	 */
	@Activate
	public void start(BundleContext context) {
		logger.info("Bundle is starting...");
		owncontext = context;
	}	
	
	@Deactivate
	public void stop(BundleContext bundleContext) throws Exception {
		logger.info("Stopping GatewayImpl bundle");
		owncontext = null;
	}
		
	/**
	 * Allows to bind the active MatchingServices
	 * @param service
	 */
	@Reference(unbind="unbindMatchingService",policy = ReferencePolicy.DYNAMIC,cardinality=ReferenceCardinality.MULTIPLE)
	public void bindMatchingService(MatchingService service) {
		logger.info("Binding MatchingService...");
		matchingServices.add(service);
	}

	public void unbindMatchingService(MatchingService service) {
		if (matchingServices.contains(service)) {
			matchingServices.remove(service);
			logger.info("Unbinding MatchingService...");
		} else {
			logger.warn("MatchingService <" + service + "> tried to unbind, but was not found in binding list.");
		}
	}
	
	/**
	 * Allows to bind the active JournalServices
	 * @param service
	 */
	@Reference(unbind="unbindJournalService",policy=ReferencePolicy.DYNAMIC,cardinality=ReferenceCardinality.OPTIONAL)
	public void bindJournalService(JournalService service) {
		logger.info("Binding JournalService...");
		journalService = service;
	}

	public void unbindJournalService(JournalService service) {
		logger.info("Unbinding JournalService...");
		journalService = null;
	}
	
	/**
	 * Receives raw data from an external source.
	 * This method is provided as a REST service method.
	 * @param metaFragment	raw data
	 * @return
	 */
	@PUT
	public Response transmitIn(final Map<String,Object> metaFragment) {
		logger.info("Receiving JSON string");
		logger.debug("String received " + metaFragment.toString());
		
		try {
			workerpool.execute(new Runnable() {
				@Override
				public void run() {
					transmitOut(metaFragment);
				}
			});
			return Response.status(Status.ACCEPTED).build();
		} catch (RejectedExecutionException e) {
			logger.warn("Execution of the metafragment was rejected", e);
			return Response.status(Status.GONE).build();
		}
	}

	/**
	 * Transmits the received message to the matching service
	 * @param metaFragment	received message
	 */
	public void transmitOut(Map<String,Object> metaFragment) {
		logger.info("Entering transmitOut");
		
		metaFragment.put("packetID", "" + packetID.incrementAndGet());
		
		if (journalService!=null){
			journalService.log(1, "message from gateway", packetID + "", owncontext, MatchingService.class, null, "transmitIn", metaFragment);
		}
				
		//iterate over all matching service(normally there should only be one)
		for (MatchingService matchingService : matchingServices) {
			matchingService.transmitIn(metaFragment);
		}
	}
	
	@GET
	public String get() {
		System.out.println("The get method is being entered, only for testing purposes");
		return "MASSIF is online...";
	}
	
	@POST
	public Response transmitInPost(final Map<String,Object> metaFragment) {
		return transmitIn(metaFragment);
	}

}
