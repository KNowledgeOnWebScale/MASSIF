package massif.watchdog.api;

public class WatchdogDummyComponent implements WatchdogComponent {

	/**
	 * The name of the dummy
	 */
	private String name;
	
	// Setter
	public void setName(String name) {
		this.name = name;
	}
	
	// Getter
	public String getName() {
		return this.name;
	}
	
	// Constructor
	public WatchdogDummyComponent(String name) {
		setName(name);
	}
	
	@Override
	public void registerWatchdog(WatchdogEventService watchdog) {
		// Do nothing
	}
	
}
