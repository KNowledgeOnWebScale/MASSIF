package massif.matchingservice;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import massif.contextadapter.api.ContextAdapter;
import massif.journal.api.JournalService;
import massif.matchingservice.api.MatchingService;
import massif.watchdog.api.WatchdogEventService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The Matching Service will analyze incoming JSON messages and decide which context adapter 
 * can enrich this message. The decision will be based on the received "tag" in the message.
 */
@Component(immediate=true)
public class MatchingServiceImpl extends AbstractMatchingService implements MatchingService {

	/** 
	 * Contains a mapping of a ontology concept to the adapter who is capable of enriching that concept
	 * eg. Person -> PersonAdapter(a  reference to a specific ContextAdapter)
	 */
	private Hashtable<String, ContextAdapter> contextadapters;
	private Map<ContextAdapter, String> typeMapper;
	
	// Constructor
	public MatchingServiceImpl() {
		contextadapters = new Hashtable<String, ContextAdapter>();
		typeMapper = new HashMap<ContextAdapter, String>();
	}
		
	@Activate
	public void start(BundleContext context) {
		this.owncontext = context;
		getLogger().info("MatchingService started");
	}
	
	/**
	 * Binds the contextadapters, properties map contains properties of the binded services.
	 * This allows us to retrieve the type of the service
	 * @param ca		The contextadapter	
	 * @param properties	Map containing all properties of the contextadapter service
	 */
	@Reference(unbind="unbindContextAdapter", policy=ReferencePolicy.DYNAMIC, cardinality=ReferenceCardinality.AT_LEAST_ONE)
	public void bindContextAdapter(ContextAdapter ca, Map<String, Object> properties){
		getLogger().info(properties.toString());
		
		// Check the contextadapter what kind of data it can enrich 
		if (properties.containsKey("tag")) {
			getLogger().info("Binding Context adapter who enriches <" + properties.get("tag").toString() + ">");
			contextadapters.put(properties.get("tag").toString(), ca);
		}
	}
		
	public void unbindContextAdapter(ContextAdapter ca, Map<String, Object> properties){
		getLogger().info(properties.toString());
		
		String key = null;
		if (properties.containsKey("tag")) {
			key = properties.get("tag").toString();
			
			if (key != null && contextadapters.get(key).equals(ca)) {
				getLogger().debug("Unbinding context adapter with type <" + key + ">");
				contextadapters.remove(key);
				typeMapper.remove(ca);
			} else if (key == null) {
				getLogger().error("Could not unbind: No key found");
			}
		} else {
			getLogger().error("Could not unbind: No enrich key found");
		}
	}
	
	@Override
	@Reference(unbind="unbindWatchdog", cardinality=ReferenceCardinality.OPTIONAL)
	public void bindWatchdog(WatchdogEventService watchdog) {
		setWatchdogEventService(watchdog);
	}

	@Override
	public void unbindWatchdog(WatchdogEventService watchdog) {
		setWatchdogEventService(null);
	}

	@Override
	@Reference(unbind="unbindJournalService", cardinality=ReferenceCardinality.OPTIONAL)
	public void bindJournalService(JournalService js) {
		setJournalService(js);
	}

	@Override
	public void unbindJournalService(JournalService js) {
		setJournalService(null);
	}
	
	@Override
	public void transmitOut(String tag, Map<String, Object> metaFragment) {		
		if (contextadapters.containsKey(tag)) {
			getLogger().debug("Send message to " + tag);		
			ContextAdapter ca = contextadapters.get(tag);
			String caType = typeMapper.get(ca);
			if(jsservice!=null){
				jsservice.log(1, this.getClass().getSimpleName(), ""+metaFragment.get("packetID"), owncontext, ContextAdapter.class, "(type="+caType +")", "transmitOut", metaFragment);
			}
			ca.transmitIn(metaFragment);
		} else {
			getLogger().error("ContextAdapter for concept <" + tag + "> not active");
		}
	}
	
}
