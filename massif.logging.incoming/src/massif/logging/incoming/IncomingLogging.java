package massif.logging.incoming;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.matchingservice.api.MatchingService;
@Component(immediate=true)
public class IncomingLogging implements MatchingService {
	
	static BundleContext owncontext;
	
	final Logger logger = LoggerFactory.getLogger(IncomingLogging.class);

	@Override
	public void transmitIn(final Map metaFragment) {
		//log as json format so it can be used for testing
		JSONObject json = new JSONObject(metaFragment);
		logger.info(json.toString());
	}

	@Override
	public void transmitOut(String tag, Map metaFragment) {
		
	}

	@Override
	public List<Map<String, Object>> analyzeMetaFragment(Map metaFragment) {
		return null;
	}

	public void start(BundleContext context){
		logger.debug("Bundle is starting...");
		owncontext = context;
		
	}

}
