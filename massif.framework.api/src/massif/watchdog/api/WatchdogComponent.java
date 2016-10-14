package massif.watchdog.api;

/**
 * Interface to communicate with a listener in the watchdog service
 */
public interface WatchdogComponent {
		
	/**
	 * Registers the watchdog service to a component
	 * @param watchdog	The MASSIF watchdog service
	 */
	void registerWatchdog(WatchdogEventService watchdog);
	
}
