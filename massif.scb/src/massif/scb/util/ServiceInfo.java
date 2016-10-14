package massif.scb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.framework.dashboard.api.AdaptableService;
import massif.mciservice.api.MCIService;

/**
 * This Class stores all necessary data to identify a service and its possible duplicates.
 * @author pbonte
 *
 */
public class ServiceInfo implements Comparable<ServiceInfo>{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String className;
	private String serviceID;
	private MCIService mciService;
	private List<ServiceInfo> duplicates;
	private List<MCIService> duplicateServices;
	private Map<String, Boolean> filterDuplicateMapping;
	private boolean isGeneric;
	
	private int lastIndex;
	
	public ServiceInfo(MCIService mciScervice, String serviceID, String componentName){
		this.className = componentName;
		this.serviceID = serviceID;
		this.mciService = mciScervice;
		duplicates = new ArrayList<ServiceInfo>();
		duplicateServices =new ArrayList<MCIService>();
		filterDuplicateMapping = new HashMap<String, Boolean>();
		lastIndex=-1;
		isGeneric = false;
		if(mciService instanceof AdaptableService){
			isGeneric = true;
		}
	}

	public String getClassName() {
		return className;
	}
	
	public String getSimpleClassName() {
		if (className != null && className.length() > 0) {
			// Return the string behind the last dot
			int ld = className.lastIndexOf(".");
			if (ld != -1)
				return className.substring(ld + 1);
		}
		
		return className;
	}

	public String getServiceID() {
		return serviceID;
	}
	public MCIService getMCIService(){
		return mciService;
	}

	@Override
	public int compareTo(final ServiceInfo arg0) {
		//return 0 if equal
		return className.compareTo(arg0.getClassName()) + serviceID.compareTo(arg0.serviceID);
	}
	
	public boolean hasDuplicates(){
		return !duplicates.isEmpty();
	}
	public void addFilter(String filter,boolean needForDuplication){
		logger.info("Adding filter: " + filter + " with duplication: "+needForDuplication);
		filterDuplicateMapping.put(filter, needForDuplication);
	}
	public void removeFilter(String filter){
		logger.info("Removing filter: " + filter );
		filterDuplicateMapping.remove(filter);
	}
	public Set<String> getFilters(){
		return filterDuplicateMapping.keySet();
	}
	public void addDuplicate(ServiceInfo duplicateInfo){
		duplicates.add(duplicateInfo);
		duplicateServices.add(duplicateInfo.getMCIService());
	}
	public void removeDuplicate(MCIService removedDuplicate){
		//TODO
		throw new UnsupportedOperationException();
	}
	public List<MCIService> getAllDuplicateServices(){
		return duplicateServices;
	}
	
	public boolean isFilterDuplicated(String filter){
		return filterDuplicateMapping.get(filter);
	}
	
	public Set<MCIService> getDuplicates(String filter){
		Set<MCIService> duplicates = new HashSet<MCIService>();
		if(!hasDuplicates()){
			duplicates.add(getMCIService());
		}else{
			boolean isLoadBalanced = filterDuplicateMapping.get(filter);
			if(isLoadBalanced){
				lastIndex = (++lastIndex) % (duplicateServices.size()+1);
				if(lastIndex < duplicateServices.size()){
					duplicates.add(duplicateServices.get(lastIndex));
				}else{
					duplicates.add(mciService); //the original should take part in the load balancing
				}
			}else{
				duplicates.addAll(duplicateServices);
				duplicates.add(mciService);
			}
		}
		return duplicates;
	}
	
	public boolean isGeneric(){
		return isGeneric;
	}

}
