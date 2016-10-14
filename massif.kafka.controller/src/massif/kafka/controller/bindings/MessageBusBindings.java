package massif.kafka.controller.bindings;

public class MessageBusBindings {
		
	/**
	 * Communication name to send and receive message to be handled by the bus
	 */
	public static final String MESSAGE_BUS_MESSAGES = "massif.bus.messages";
	
	/**
	 * Communication name to send and receive filters
	 */
	public static final String MESSAGE_BUS_FILTERS = "massif.bus.filters";
	
	/**
	 * Communication name to send and receive watchdog events
	 */
	public static final String MESSAGE_BUS_WATCHDOG = "massif.bus.watchdog";
	
	/**
	 * The name of the watchog services in the kafka cluster
	 */
	public static final String GROUP_NAME_MESSAGE_BUS_WATCHDOG = "massif.watchdog";
	
	/**
	 * The name of the matching services in the kafka cluster
	 */
	public static final String GROUP_NAME_MESSAGE_BUS_MATCHINGSERVICES = "massif.matchingservices";
	
	/**
	 * The name of the adapters in the kafka cluster
	 */
	public static final String GROUP_NAME_MESSAGE_BUS_ADAPTERS = "massif.adapters";
	
	/**
	 * The name of the semantic bus(ses) in the kafka cluster
	 */
	public static final String GROUP_NAME_MESSAGE_BUS_SEMANTIC_BUS = "massif.bus";
	
	/**
	 * The name of the services in the kafka cluster
	 */
	public static final String GROUP_NAME_MESSAGE_BUS_SERVICES = "massif.services";

}
