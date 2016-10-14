package massif.journal.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

//import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;



/**
 * The interface JournalService extends the interface LogService; a log-method was added to give a journaling functionality. This function
 * should ensure the possibility of restoring the system after failure. methods to register and unregister a persistence module were added.
 * a method to retrieve a logging-entry for a persistence module was added.
 * 
 * @author Don Giot
 * 
 **/
public interface JournalService {

	final static int LOG_DEBUG = 4;
	final static int LOG_INFO = 3;
	final static int LOG_WARNING = 2;
	final static int LOG_ERROR = 1;
	final static int LOG_OFF = 0;

	@SuppressWarnings("serial")
	public static class NoQueueAcquiredException extends Exception {
	};

	@SuppressWarnings("serial")
	public static class QueueAlreadyAcquiredException extends Exception {
	}

	/**
	 * this log-method should enable a restoring service, it logs all the data needed for a journaling service.
	 * 
	 * @param level
	 *            : severity level of the log. 0: LOG_OFF, 1: LOG_ERROR, 2: LOG_WARNING, 3: LOG_INFO, 4: LOG_DEBUG.
	 * @param message
	 *            : short message added to log, specified by user.
	 * @param uri_id
	 *            : uri or id of the datapacket.
	 * @param ofUsingService
	 *            : BundleContext, enabling the service to extract metadata from it.
	 * @param ofUsedService
	 *            : ServiceReference, enabling the service to extract metadata from it
	 * @param functionName
	 *            : Name of the called method, of the used service.
	 * @param args
	 *            : arguments of the called function.
	 */
	@SuppressWarnings("rawtypes")
	public void log(int level, String message, String uriId, BundleContext ofUsingService, ServiceReference ofUsedService, String functionName, Map args);

	/**
	 * This method does the same as the log method which asks for a ServiceReference Object, but asks for a Class Object and a String
	 * filter. This might be a cheaper version of the method to call, since the ServiceReference Object can be hard to obtain.
	 * 
	 * @param level
	 *            : severity level of log-message: 0: LOG_OFF, 1: LOG_ERROR, 2: LOG_WARNING, 3: LOG_INFO, 4: LOG_DEBUG.
	 * @param message
	 *            : short message added to log, specified by user.
	 * @param uri_id
	 *            : uri or id of the datapacket.
	 * @param ofUsingService
	 *            : BundleContext, enabling the service to extract metadata
	 * @param usedServiceObjectClass
	 *            Class Object of the used ServiceObject of the Service used by the logging bundle.
	 * @param filter
	 *            Used when there are multiple instances of a specific ServiceObject.
	 * @param functionName
	 *            : Name of the called method, of the used service.
	 * @param args
	 *            : arguments of the called function.
	 */
	@SuppressWarnings("rawtypes")
	public void log(int level, String message, String uriId, BundleContext ofUsingService, Class usedServiceObjectClass, String filter, String functionName, Map args);

	/**
	 * This method does the same as the log method which asks for a ServiceReference Object, but asks for a Class Object and a String
	 * filter. This might be a cheaper version of the method to call, since the ServiceReference Object can be hard to obtain.
	 * 
	 * The Axioms in the OWLMessage will added to an Empty OWLOntology which can be converted to a string. Only a string representation of
	 * IRI of the individual is saved because the individual can be easily retrieved from the ontology.
	 *  Also a list with filter rules can be passed.
	 * 
	 * @param level
	 *            : severity level of log-message: 0: LOG_OFF, 1: LOG_ERROR, 2: LOG_WARNING, 3: LOG_INFO, 4: LOG_DEBUG.
	 * @param message
	 *            : short message added to log, specified by user.
	 * @param uri_id
	 *            : uri or id of the datapacket.
	 * @param ofUsingService
	 *            : BundleContext, enabling the service to extract metadata
	 * @param usedServiceObjectClass
	 *            Class Object of the used ServiceObject of the Service used by the logging bundle.
	 * @param filter
	 *            Used when there are multiple instances of a specific ServiceObject.
	 * @param functionName
	 *            : Name of the called method, of the used service.
	 * @param args
	 *            : arguments of the called function.
	 *            
	 *@param rules:
	 *				list of called rules
	 */
	@SuppressWarnings("rawtypes")
	public void log(int level, String message, String uriId, BundleContext ofUsingService, Class usedServiceObjectClass, String filter, String functionName, Serializable args,List rules);

	/**
	 * This method does the same as the log method which asks for a ServiceReference Object, but asks for a Class Object and a String
	 * filter. This might be a cheaper version of the method to call, since the ServiceReference Object can be hard to obtain.
	 * 
	 * The Axioms in the OWLMessage will added to an Empty OWLOntology which can be converted to a string. Only a string representation of
	 * IRI of the individual is saved because the individual can be easily retrieved from the ontology.
	 * 
	 * @param level
	 *            : severity level of log-message: 0: LOG_OFF, 1: LOG_ERROR, 2: LOG_WARNING, 3: LOG_INFO, 4: LOG_DEBUG.
	 * @param message
	 *            : short message added to log, specified by user.
	 * @param uri_id
	 *            : uri or id of the datapacket.
	 * @param ofUsingService
	 *            : BundleContext, enabling the service to extract metadata
	 * @param usedServiceObjectClass
	 *            Class Object of the used ServiceObject of the Service used by the logging bundle.
	 * @param filter
	 *            Used when there are multiple instances of a specific ServiceObject.
	 * @param functionName
	 *            : Name of the called method, of the used service.
	 * @param args
	 *            : arguments of the called function.
	 */
	@SuppressWarnings("rawtypes")
	public void log(int level, String message, String uriId, BundleContext ofUsingService, Class usedServiceObjectClass, String filter, String functionName, Serializable args);
	/**
	 * @Deprecated
	 * Adds endpoint to visualization logger
	 * WARNING: only used for visualization! Has no effect on other loggers
	 * Inf no endpoint is logged, the visualization gets stuck.
	 * @param level
	 * @param message
	 * @param uri_id
	 * @param ofUsingService
	 * @param functionName
	 */
	@Deprecated
	public void logEndPoint(int level, String message, String uriId, BundleContext ofUsingService, String functionName);

	/**
	 * This method provides a way for persistence modules to extract log-entries.
	 * 
	 * @param bundleName
	 *            : The bundlename of the persistence module is used to look up the registration for that module.
	 * @return this method returns a JSONObject, which is a "key":"value" pair Object, to map certain data.
	 * @throws Exception
	 *             this method throws an Exception when it is not registered in the JournalService as a persistencemodule.
	 */
	//public JSONObject takeFromQueue(String bundleName) throws NoQueueAcquiredException;

	/**
	 * This method will register the bundle, as a persistencemodule, with the JournalService. Once registered, the log-entries will be
	 * queued for the persistencemodule.
	 * 
	 * @param bundleName
	 *            : bundlename used for registration, to identify the correct bundle.
	 * @throws Exception
	 *             the method will throw an Exception when the bundle was already registered.
	 */
	public void addPersistenceBundle(String bundleName) throws QueueAlreadyAcquiredException;

	/**
	 * This method will unregister the bundle. It should we called when the bundle unbinds from the LogService. It will ignore this method
	 * call when no bundle with specified bundle name was registered.
	 * 
	 * @param bundleName
	 *            : bundlename used to unregister the service.
	 */
	public void removePersistenceBundle(String bundleName);

	/**
	 * This method is a normal log message.
	 * 
	 * @param level
	 *            : severity_level of the message.
	 * @param message
	 *            : log message.
	 */
	public void log(int level, String message);

	public void log(ServiceReference sr, int level, String message);

}