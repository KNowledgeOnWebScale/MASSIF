package massif.matchingservice.api;

import java.util.List;
import java.util.Map;

/**
 * Interface for the Matching Service capabilities.
 * The Matching Service will analyse the incoming message and decide which Context Adapter is able to enrich 
 * the incoming raw data.
 * 
 * @author Pieter Bonte
 * @version 1.0
 */
public interface MatchingService {
	
	/**
	 * This method will be called to provide the Matching Service with input data
	 * @param metaFragment raw data
	 * @return void 
	 */
	void transmitIn(Map<String,Object> metaFragment);

	/**
	 * This method analyzes a metaFragment and splits it up into several new fragments if needs. 
	 * For example, when the sensor board consists of more than 1 sensor it shall be broken down. 
	 * @param metaFragment The metaFragment as sent by the gateway
	 * @return A List of Maps, where the packetID is already changed.
	 */

	List<Map<String,Object>> analyzeMetaFragment(Map<String,Object> metaFragment);

	/**
	 * Method that will decide to which adapter the metaFragment should be sent. 
	 * 
	 * @param tag The tag on which the decision will be made
	 * @param metaFragment The metaFragment that has to be sent to the adapter
	 */

	void transmitOut(String tag, Map<String,Object> metaFragment);
	
}
