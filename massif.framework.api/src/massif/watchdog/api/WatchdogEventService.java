package massif.watchdog.api;

/**
 * Interface that can trigger a watchdog events
 */
public interface WatchdogEventService {
	
	/**
	 * When a new event was triggered, use this method to notify the our watchdog service
	 * @param id				The incoming id received from this service that can be used to link the data 
	 * @param context			The data that you want to link to the given id
	 * @return					True if succes
	 */
	boolean incomingEvent(String id, String context);
	
	/**
	 * Starts a new event with the given id
	 * @param id			The id of the event you want to start
	 * @return				True if success
	 */
	boolean eventStart(String id);
		
	/**
	 * Indicates that the listener is running event with the given id
	 * @param listener		The listener that is running the given id
	 * @param id			The id of the event that is running
	 * @return
	 */
	boolean eventRunning(WatchdogComponent listener, String id);
	
	/**
	 * Updates the given id to ready
	 * @param id			The id you want to update
	 * @return				True if success
	 */
	boolean eventReady(WatchdogComponent listener, String id);

}
