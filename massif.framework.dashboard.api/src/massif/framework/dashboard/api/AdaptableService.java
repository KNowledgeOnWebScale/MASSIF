package massif.framework.dashboard.api;

import java.util.List;
/**
 * Allows to adapt certain Service properties.
 * Used to adapt services in the DashBoard.
 * 
 * @author pbonte
 *
 */
public interface AdaptableService {
	
	/**
	 * Returns the human readable name, if defined.
	 * @return	human readable name.
	 */
	
	public String getName();
	
	/**
	 * List all active Queries.
	 * @return List of queries.
	 */
	public List<String> getQueries();
	/**
	 * Removes query at the defined index. 
	 * @param index	index of deletion.
	 * @return	true if deletion was successful, false otherwise.
	 */
	public boolean removeQuery(int index);
	/**
	 * Adds query to the list of executed queries.
	 * @param query
	 * @return	true if addition was successful.
	 */
	public boolean addQuery(String query);
	/**
	 * List all active filter rules.
	 * @return	list of filter rules.
	 */
	public List<String> getFilterRules();
	/**
	 * Removes filter rule at the defined index. 
	 * @param index	index of deletion.
	 * @return	true if deletion was succesful, false otherwise.
	 */
	public boolean removeFilterRule(int index);
	/**
	 * Adds filter rule to the list of executed queries.
	 * @param query
	 * @return	true if addition was successful.
	 */
	public boolean addFilterRule(String filterName, String filterRule);

}
