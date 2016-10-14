package massif.matchingservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import massif.journal.api.JournalService;
import massif.matchingservice.api.MatchingService;
import massif.watchdog.api.WatchdogEventService;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMatchingService implements MatchingService {

	// Bundle context
	protected static BundleContext owncontext;
	
	static BundleContext getContext() {
		return owncontext;
	}
	
	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// GETTER logger
	protected Logger getLogger() {
		return logger;
	}
	
	/** MASSIF Services */
	private WatchdogEventService watchdogService;
	
	// GETTER watchdogService
	protected WatchdogEventService getWatchdogEventService() {
		return watchdogService;
	}
	
	// SETTER watchdogService
	protected void setWatchdogEventService(WatchdogEventService watchdogService) {
		this.watchdogService = watchdogService;
	}
	
	// Bind and unbind methods Watchdog
	public abstract void bindWatchdog(WatchdogEventService watchdog);
	public abstract void unbindWatchdog(WatchdogEventService watchdog);
	
	protected JournalService jsservice;
	
	// GETTER journalService
	protected JournalService getJournalService() {
		return jsservice;
	}
	
	// SETTER journalService
	protected void setJournalService(JournalService service) {
		this.jsservice = service; 
	}
	
	// Bind and unbind methods JournalService
	public abstract void bindJournalService(JournalService js);
	public abstract void unbindJournalService(JournalService js);
	
	/** Worker pool to handle the message */
	private ExecutorService workerpool = Executors.newFixedThreadPool(4);
	
	/**
	 * Prepares the matching service
	 * @param context
	 */
	protected void start(BundleContext context) {
		owncontext = context;
	}
	
	@Override
	public void transmitIn(final Map<String, Object> metaFragment) {			
		logger.debug("metaFragment received in the transmitIn.");
		
		try {
			workerpool.execute(new Runnable() {
				
				@Override
				public void run() {
					List<Map<String, Object>> results = analyzeMetaFragment(metaFragment);
										
					for (final Map<String, Object> m : results) {
						try {
							workerpool.execute(new Runnable() {
								@Override
								public void run() {
									String tag = null;
									Object tagObject = "";
									//check if json-ld
									if(m.containsKey("@context")){
										tagObject="jsonld";
									}else{
										tagObject = ((Map<String, Object>) ((Map<String, Object>) m.get("data")).get("e")).get("tag");
									}
									
									if (tagObject instanceof String) {
										tag = (String) tagObject;
									} else if (tagObject instanceof List) {
										ArrayList<String> tagList = (ArrayList<String>) tagObject;
										tag = tagList.get(0);
									}
									
									// Strip the tag
									int k = tag.indexOf(":");
									if (!(k == -1 && k != tag.length()))
										tag = tag.substring(k + 1).trim();
									
									transmitOut(tag, m);
								}
							});
						} catch (RejectedExecutionException e) {
							logger.error("RejectionException", e);
						}
					}

				}
			});
		} catch (RejectedExecutionException err) {
			logger.error("RejectionException", err);
		}		
	}

	@Override
	public List<Map<String,Object>> analyzeMetaFragment(Map<String, Object> metaFragment) {		
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		
		// Get the data from the incoming message
		Map<String,Object> data = (Map<String,Object>) metaFragment.get("data");
		if(data == null){
			logger.warn("No data element present in received message!");
			result.add(metaFragment);
			
		}		
		else if (data.size() != 0 && data.containsKey("e")) {
			// The raw data is contained in the e key
			List<Map<String,Object>> e = (ArrayList<Map<String,Object>>) data.get("e");
			
			// Handler if e key is present
			if (!e.isEmpty()) {
				int counter = 1;
				
				String packetID = (String) metaFragment.get("packetID");
				String baseName = (String) data.get("bn");
				
				for (Map<String,Object> m : e) {
					Map<String,Object> d = new HashMap<String,Object>();
					d.put("e", m);
					
					Map<String,Object> part = new HashMap<String, Object>(metaFragment);
					part.put("data", d);
					part.put("packetID", packetID + "-" + counter);
					part.put("bn", baseName);
					
					// Add to the main map
					result.add(part);
					
					// Increase the number of message
					counter++;
				}
			}
		} else {
			String packetID = (String) metaFragment.get("packetID");
			String baseName = data.containsKey("bn") ? (String) data.get("bn") : "not set";
			
			Map<String,Object> d = new HashMap<String, Object>();
			d.put("e", data);
			
			Map<String,Object> part = new HashMap<String, Object>(metaFragment);
			part.put("data", d);
			part.put("packetID", packetID);
			part.put("bn", baseName);
			
			// Add to the main map
			result.add(part);
		}
		
		// Prepare event on the watchdog service
		if (watchdogService != null && metaFragment.containsKey("clientid")) {
			// The client that initiated the event
			String client = metaFragment.get("clientid").toString();
			
			if (result.size() > 0) {
				String pid = result.get(0).get("packetID").toString();
				watchdogService.incomingEvent(client, pid);
			}
		}
		
		logger.info("analyzeMetaFragment complete: " + result.toString());
		
		return result;
	}
	
}
