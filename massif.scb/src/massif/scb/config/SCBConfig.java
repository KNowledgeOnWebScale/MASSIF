package massif.scb.config;

public class SCBConfig {
	
	public enum FilterCheckingStrategy{
		SYNTAX,
		REASONING,
		NONE
	}
	
	public static String PROP_ONTOLOGY_IRI = "massif.scb.ontologyiri";
	public static String PROP_ONTOLOGY_PREFIX = "massif.scb.ontologyprefix";
	public static String PROP_ENABLE_CACHE = "massif.scb.caching";
	public static String PROP_FILTER_CHECKING = "massif.scb.filterchecking";
	public static String PROP_EVENT_IRI = "massif.scb.eventiri";
	public static String PROP_CHECK_CONSISTENCY = "massif.scb.checkconsistency";
	
	public static String ONTOLOGY_IRI = null;	
	public static String EVENT_IRI = "http://massif.scb.owl#Event";
	public static String FALLBACK_ONTOLOGY_IRI = "http://massif.scb.owl";
	public static boolean ENABLE_CACHE = false;
	public static FilterCheckingStrategy FILTER_CHECKING = FilterCheckingStrategy.NONE;
	public static boolean CHECK_CONSISTENCY = true;
	public static String WRITE_LOCATION = "/tmp/massif/scb";

}
