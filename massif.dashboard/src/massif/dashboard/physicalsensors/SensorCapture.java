package massif.dashboard.physicalsensors;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.framework.dashboard.api.Dashboard;
import massif.framework.dashboard.api.VirtualSink;
import massif.matchingservice.api.MatchingService;

@Component(immediate=true)
public class SensorCapture implements MatchingService{
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Dashboard dashboard;
	@Activate
	public void start(){
		logger.info("Starting capturing");
	}
	@Override
	public void transmitIn(Map<String, Object> metaFragment) {
		// TODO Auto-generated method stub
		logger.info("Captured data!!!!");
		if(dashboard!=null){
			dashboard.sensorUpdate(metaFragment);
		}
	}

	@Override
	public List<Map<String, Object>> analyzeMetaFragment(Map<String, Object> metaFragment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void transmitOut(String tag, Map<String, Object> metaFragment) {
		// TODO Auto-generated method stub
		
	}
	@Reference(unbind = "unbindDashBoard", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void bindDashBoard(Dashboard dashboard){
		this.dashboard = dashboard;
		logger.info("Binding dashboard");
	}
	public void unbindDashBoard(Dashboard dashboard){
		if(dashboard == this.dashboard){
			this.dashboard = null;
		}
	}


}
