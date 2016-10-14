package massif.scb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import massif.framework.dashboard.api.AdaptableSCB;
import massif.framework.util.owl.OntologyCache;
import massif.journal.api.JournalService;
import massif.mciservice.api.MCIService;
import massif.scb.api.OWLMessage;
import massif.scb.api.OWLMessageFilter;
import massif.scb.api.OWLSemanticCommunicationBus;
//import massif.fw.journalapi.JournalService;
import massif.scb.cache.SCBCache;
import massif.scb.config.SCBConfig;
import massif.scb.config.SCBConfigLoader;
import massif.scb.util.EqualOWLClassChecker;
import massif.scb.util.ServiceInfo;
import massif.watchdog.api.WatchdogDummyComponent;
import massif.watchdog.api.WatchdogEventService;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Configuration.PrepareReasonerInferences;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.ConvertSuperClassesToEquivalentClass;
import org.semanticweb.owlapi.formats.*;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class OWLCentralizedSemanticBus implements OWLSemanticCommunicationBus, AdaptableSCB {
	// LOGGER
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// ontology
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private PrefixManager prefixManager;
	private OWLOntology ontology;
	private Reasoner reasoner;
	private OntologyCache ontologyCache;
	// threading
	private Object ontologyLock = new Object();
	private ExecutorService workerpool = Executors.newFixedThreadPool(1);
	private boolean active = false;
	// service handling
	private Map<String, Set<MCIService>> filterMapping;
	private ArrayList<OWLMessageFilter> filterList;
	private LinkedList<ServiceInfo> serviceQueue;
	private Map<String, ServiceInfo> registeredServices;
	private Map<MCIService, ServiceInfo> serviceInfoMapping;
	// caching
	private SCBCache cache;

	// MASSIF watchdog service
	private WatchdogEventService watchdogService;

	// Journal service
	private JournalService jsservice;
	private BundleContext bundleContext;
	private String type;

	/**
	 * Constructor
	 */
	public OWLCentralizedSemanticBus() {
		type = this.getClass().getSimpleName();
		serviceQueue = new LinkedList<ServiceInfo>();
		registeredServices = new HashMap<String, ServiceInfo>();
		serviceInfoMapping = new HashMap<MCIService, ServiceInfo>();
	}

	public OWLCentralizedSemanticBus(String ontologyIRI, String eventIRI, boolean enableCache) {
		this();
		startSCB(ontologyIRI, eventIRI, enableCache);
	}

	private void startSCB(String ontologyIRI, String eventIRI, boolean enableCache) {
		this.ontologyCache = OntologyCache.getOntologyCache();
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();

		// load ontology
		try {
			if (SCBConfig.ONTOLOGY_IRI != null) {
				ontology = ontologyCache.getOntology(ontologyIRI, manager);

			} else {
				// load an empty ontology
				ontology = manager.createOntology(IRI.create(SCBConfig.FALLBACK_ONTOLOGY_IRI));
			}
			prefixManager = new DefaultPrefixManager(ontology.getOntologyID().getOntologyIRI().toString() + "#");

			// new bus version

			filterMapping = new HashMap<String, Set<MCIService>>();
			this.filterList = new ArrayList<OWLMessageFilter>();

			// config reasoner
			Configuration conf = new Configuration();
			// conf.prepareReasonerInferences = new PrepareReasonerInferences();
			// conf.prepareReasonerInferences.realisationRequired = true;
			this.reasoner = new Reasoner(conf, ontology);
			// create SCB cache
			this.cache = new SCBCache(enableCache);

			this.active = true;
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to load ontology", e);
		} catch (Exception e1) {
			logger.error("Exception while starting SCB", e1);
		}
	}

	@Activate
	public void start(ComponentContext context) {
		try {
			this.bundleContext = context.getBundleContext();
			SCBConfigLoader.loadPropertyConfig(context.getBundleContext());
			SCBConfigLoader.loadRuntimeConfig(context.getBundleContext());
			if (context.getProperties() != null) {
				SCBConfigLoader.loadProperties(context.getProperties());
			}
			startSCB(SCBConfig.ONTOLOGY_IRI, SCBConfig.EVENT_IRI, SCBConfig.ENABLE_CACHE);
			// add service if any in queue
			while (serviceQueue.peek() != null) {
				ServiceInfo serviceInfo = serviceQueue.poll();
				addService(serviceInfo);
			}
		} catch (Exception e) {
			logger.error("Unable to start SCB", e);
		}
	}

	@Reference(unbind = "unbindJournalService", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void bindJournalService(JournalService js) {
		this.jsservice = js;
	}

	public void unbindJournalService(JournalService js) {
		this.jsservice = null;
	}

	@Override
	public synchronized boolean publish(final OWLMessage message, final String packetID) {
		// Notify our watchdogservice
		if (watchdogService != null) {
			watchdogService.eventStart(packetID);
		}
		// Process the message
		workerpool.execute(new Runnable() {

			@Override
			public void run() {
				/*
				 * if(jsservice!=null){ jsservice.log(1, type, "" + packetID,
				 * bundleContext, MCIService.class, null, "transmitIn",
				 * message); }
				 */

				Set<MCIService> combinedHandlers = new HashSet<MCIService>();
				Set<String> triggeredFilters = new HashSet<String>();

				final long time1 = System.currentTimeMillis();
				System.out.println("Received Event in Semantic Bus");

				OWLOntology ont = ontology;
				// add the message to the ontology
				message.getAxioms().stream().forEach(axiom -> manager.addAxiom(ont, axiom));
				saveOntology(ont, manager, "scbTest");

				OWLClass messageClass = factory.getOWLClass(SCBConfig.EVENT_IRI);
				if (message.getOWLMessage() != null) {
					// event defined
					OWLAxiom messageInstanceAxiom = factory.getOWLClassAssertionAxiom(messageClass,
							message.getOWLMessage());
					manager.addAxiom(ont, messageInstanceAxiom);

					Set<String> foundFilters = new HashSet<String>();
					// check cache
					// get Type of LevelOne type
					String levelOne = cache.getLevelOneType(ont, message.getOWLMessage(), "hasContext");
					Set<String> cacheFilters = cache.checkCache(levelOne, message.getOWLMessage(), ont, manager);

					if (cacheFilters != null) {
						for (String cacheFilter : cacheFilters) {
							if (filterMapping.containsKey(cacheFilter)) {
								// Add all handlers in new Set so same handler
								// is
								// not called twice when a handler is registerd
								// with
								// multiple filters
								combinedHandlers.addAll(filterMapping.get(cacheFilter));
								triggeredFilters.add(cacheFilter);
							} else {
								logger.debug("Filter <" + cacheFilter + "> not registered in Bus");
							}
						}
					} else {
						// ask reasoner for the types of the arriving event
						reasoner.flush();
						NodeSet<OWLClass> inferedClasses = reasoner.getTypes(message.getOWLMessage(), false);

						for (OWLClass owlclss : inferedClasses.getFlattened()) {
							String clss = owlclss.getIRI().toString();
							if (filterMapping.containsKey(clss)) {
								triggeredFilters.add(clss);
								for (MCIService handlerService : filterMapping.get(clss)) {
									ServiceInfo handlerInfo = serviceInfoMapping.get(handlerService);
									combinedHandlers.addAll(handlerInfo.getDuplicates(clss));
								}
							}
						}
						logger.debug(triggeredFilters.toString());

						if (!triggeredFilters.isEmpty()) {
							// Add new Results to Cache:
							for (String foundFilter : foundFilters) {
								OWLClassExpression preprocessing = cache.preProcessing2(message.getAxioms(),
										message.getOWLMessage(), ont, manager, foundFilter, reasoner);
								cache.addToCache2(levelOne, preprocessing, foundFilter);
							}
						}

					}
					// remove the added axioms from the ontology
					manager.removeAxiom(ont, messageInstanceAxiom);
				} else {
					// no event defined
					// retrieve the individuals that match one of the filter
					// rules
					reasoner.flush();
					NodeSet<OWLNamedIndividual> eventIndividuals = reasoner.getInstances(messageClass, false);
					for (OWLNamedIndividual eventInd : eventIndividuals.getFlattened()) {
						// ask reasoner for the types of the arriving event
						NodeSet<OWLClass> inferedClasses = reasoner.getTypes(eventInd, false);

						for (OWLClass owlclss : inferedClasses.getFlattened()) {
							String clss = owlclss.getIRI().toString();
							if (filterMapping.containsKey(clss)) {
								triggeredFilters.add(clss);
								for (MCIService handlerService : filterMapping.get(clss)) {
									ServiceInfo handlerInfo = serviceInfoMapping.get(handlerService);
									combinedHandlers.addAll(handlerInfo.getDuplicates(clss));
								}
							}
						}

					}
				}
				// add the triggered filterrules to the message
				message.addTriggeredFilters(triggeredFilters);
				// Execute filters
				for (final MCIService handler : combinedHandlers) {
					workerpool.execute(new Runnable() {

						@Override
						public void run() {
							// Notify watchdog service handler is running
							if (watchdogService != null) {
								// Find the name of the service
								ServiceInfo handlerInfo = serviceInfoMapping.get(handler);

								String handlerName = handlerInfo.getSimpleClassName();
								if (!handlerName.equals(handler.getClass().getSimpleName())) {
									WatchdogDummyComponent dcomp = new WatchdogDummyComponent(handlerName);

									// Send the dummy component with the corrent
									// name
									logger.info("Triggering " + handlerName);
									watchdogService.eventRunning(dcomp, packetID);
								} else {
									logger.info("Triggering " + handler.getClass().getSimpleName());
									watchdogService.eventRunning(handler, packetID);
								}
							}

							handler.transmitIn(message);
						}

					});
				}

				if (!combinedHandlers.isEmpty()) {
					if (jsservice != null) {
						jsservice.log(1, type, "" + packetID, bundleContext, MCIService.class, null, "transmitOut",
								message);
					}
				}

				manager.removeAxioms(ont, message.getAxioms());

				logger.info("Processed Event in Semantic Bus in " + (System.currentTimeMillis() - time1) + " ms");
			}
		});

		return true;
	}

	@Reference(unbind = "unbindMCIService", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
	public void bindMCIService(MCIService mciService, Map<String, Object> properties) {
		logger.info("binding mci" + properties);
		String serviceId = properties.get("service.id").toString();
		String componentName = properties.get("component.name").toString();
		ServiceInfo serviceInfo = new ServiceInfo(mciService, serviceId, componentName);
		serviceInfoMapping.put(mciService, serviceInfo);
		if (active) {
			addService(serviceInfo);
		} else {
			serviceQueue.offer(serviceInfo);
		}

	}

	public void addService(ServiceInfo serviceInfo) {
		MCIService mciService = serviceInfo.getMCIService();
		mciService.registerBus(this);
		List<OWLMessageFilter> filters = mciService.getFilter();
		// check if duplicate, but no generic services
		if (registeredServices.containsKey(serviceInfo.getClassName()) && !serviceInfo.isGeneric()) {
			// add duplicate to list of duplicates
			logger.info("duplicate detected");
			registeredServices.get(serviceInfo.getClassName()).addDuplicate(serviceInfo);
		} else {
			// only add filters if duplicate
			registeredServices.put(serviceInfo.getClassName(), serviceInfo);
			for (OWLMessageFilter filter : filters) {
				subscribe(mciService, filter);
			}

		}
	}

	public void unbindMCIService(MCIService mciService, Map<String, Object> properties) {
		logger.info("Unsubscribing MCIService");
		String componentName = properties.get("component.name").toString();
		if (registeredServices.containsKey(componentName)) {
			registeredServices.remove(componentName);
		}
		if (serviceInfoMapping.containsKey(mciService)) {
			ServiceInfo removeInfo = serviceInfoMapping.get(mciService);
			// remove from filters
			for (String filter : removeInfo.getFilters()) {
				if (filterMapping.containsKey(filter)) {
					filterMapping.get(filter).remove(mciService);
				}
			}
			// remove mapping
			serviceInfoMapping.remove(mciService);
		}

	}

	public synchronized boolean subscribe(MCIService handler, OWLMessageFilter filter) {
		logger.info("A Handler is subscribing: " + handler);
		// first check if the filter already exists for this handler
		String filterIRI = filter.getOWLFilter().getIRI().toString();
		long startTime = System.currentTimeMillis();
		if (filterMapping.containsKey(filterIRI) && filterMapping.get(filterIRI).contains(handler)) {
			logger.info("filter (" + filter + ") already exists for handler (" + handler + ")");
			return false;
		}
		if (!serviceInfoMapping.containsKey(handler)) {
			return false;
		}
		ServiceInfo serviceInfo = serviceInfoMapping.get(handler);
		OWLMessageFilter equalFilter = findEqualFilter(filter);

		if (equalFilter != null) {
			// equal filter found, no need to check consistency
			filterIRI = equalFilter.getOWLFilter().getIRI().toString();
			filterMapping.get(filterIRI).add(handler);
			// add filter to the ServiceInfo
			serviceInfo.addFilter(filterIRI, filter.isLoadBalanced());
			logger.info("filter already in the ontology(but added handler): " + equalFilter + " (execTime="
					+ (System.currentTimeMillis() - startTime) + "ms)");
			return true;
		} else {
			// add filter to the ServiceInfo
			serviceInfo.addFilter(filterIRI, filter.isLoadBalanced());

			// add filter to filter list
			filterList.add(filter);

			// add the filter to the ontology as a sub-concept of message
			OWLClass messageClass = factory.getOWLClass(SCBConfig.EVENT_IRI);
			OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(filter.getOWLFilter(), messageClass);
			// lock ontology so no axioms get lost when multiple threads add
			// axioms at the same time
			synchronized (ontologyLock) {
				manager.addAxiom(ontology, subclassAxiom);
				manager.addAxioms(ontology, filter.getAxioms());
				ConvertSuperClassesToEquivalentClass change = new ConvertSuperClassesToEquivalentClass(factory,
						filter.getOWLFilter(), Collections.singleton(ontology), ontology);
				if (filter.getAxioms().toString().contains("hasContext")) {
					manager.applyChanges(change.getChanges());
				}

			}
			saveOntology(ontology, manager, "subscribeOnt");

			reasoner.flush();

			// check if the filter is satisfiable (i.e. the filter is a valid
			// message sub-concept)
			if (!SCBConfig.CHECK_CONSISTENCY || !reasoner.getUnsatisfiableClasses().contains(filter.getOWLFilter())) {
				if (!filterMapping.containsKey(filterIRI)) {
					filterMapping.put(filterIRI, new HashSet<MCIService>());
				}
				filterMapping.get(filterIRI).add(handler);
				logger.info("filter succesfully added to the ontology: " + filter + " (execTime="
						+ (System.currentTimeMillis() - startTime) + "ms)");
				return true;
			} else { // else remove the filter from the ontology
				OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
				filter.getOWLFilter().accept(remover);
				synchronized (ontologyLock) {
					manager.applyChanges(remover.getChanges());
					manager.removeAxioms(ontology, filter.getAxioms());
					manager.removeAxiom(ontology, subclassAxiom);
				}
				logger.warn("failed to add filter to the ontology: " + filter + " (execTime="
						+ (System.currentTimeMillis() - startTime) + "ms)");
				return false;
			}
		}
	}

	/**
	 * Finds a semantically equal filter through reasoning.
	 * 
	 * @param filter
	 *            The new filter
	 * @return A semantic equal filter (through reasoning)
	 */
	private OWLMessageFilter findEqualFilterReasoning(OWLMessageFilter filter) {
		Node<OWLClass> equilClass = reasoner.getEquivalentClasses(filter.getOWLFilter());
		for (OWLClass possibleClass : equilClass.getEntities()) {
			// check if class is one of the registered filter rules
			for (OWLMessageFilter messageFilter : filterList) {
				if (messageFilter.getOWLFilter().equals(possibleClass)) {
					return messageFilter;
				}
			}
		}
		return null;
	}

	/**
	 * Finds an equal filter based on the syntax of the filter and the previous
	 * registered filters.
	 * 
	 * @param filter
	 *            The new filter
	 * @return An already registered equal filter (based on sytax)
	 */
	private OWLMessageFilter findEqualFilterSyntax(OWLMessageFilter filter) {
		OWLSubClassOfAxiom clazz1 = null;
		OWLSubClassOfAxiom clazz2 = null;
		// find subclass axiom in axiom list of new filter
		for (OWLAxiom ax : filter.getAxioms()) {
			if (ax instanceof OWLSubClassOfAxiom) {
				clazz1 = (OWLSubClassOfAxiom) ax;
				break;
			}
		}
		// iterate over the filters
		for (OWLMessageFilter messageFilter : filterList) {
			// find subclass axiom in axiom list of existing filter
			for (OWLAxiom ax : messageFilter.getAxioms()) {
				if (ax instanceof OWLSubClassOfAxiom) {
					clazz2 = (OWLSubClassOfAxiom) ax;
					break;
				}
			}
			// check if classes are equal
			EqualOWLClassChecker equalChecker = new EqualOWLClassChecker(clazz1, clazz2);
			if (equalChecker.isEqual()) {
				return messageFilter;
			}
		}
		return null;
	}

	/*
	 * Finds an equal filter based on the set property
	 */
	private OWLMessageFilter findEqualFilter(OWLMessageFilter filter) {
		OWLMessageFilter result = null;
		switch (SCBConfig.FILTER_CHECKING) {
		case SYNTAX:
			result = findEqualFilterSyntax(filter);
			break;
		case REASONING:
			result = findEqualFilterReasoning(filter);
			break;
		default:
			result = null;
			break;
		}
		return result;
	}

	public OWLOntology getBaseOntology() {
		return ontology;
	}

	public OWLDataFactory getDataFactory() {
		return factory;
	}

	public OWLOntologyManager getOntologyManager() {
		return manager;
	}

	public PrefixManager getPrefixManager() {
		return prefixManager;
	}

	private void saveOntology(OWLOntology ontology, OWLOntologyManager manager, String suffix) {
		if (SCBConfig.WRITE_LOCATION != null) {
			try {
				File file = new File(SCBConfig.WRITE_LOCATION + "savedontology" + suffix + ".owl");
				if (!file.canExecute()) {
					File mkdir = new File(SCBConfig.WRITE_LOCATION);
					mkdir.mkdirs();
				}
				file.createNewFile();
				manager.saveOntology(ontology, new N3DocumentFormat(), new FileOutputStream(file));
			} catch (OWLOntologyStorageException | IOException e) {
				logger.error("could not write ontology to location <" + SCBConfig.WRITE_LOCATION + ">", e);
			}
		}
	}

	@Modified
	public void update(Map<String, String> props) {
		logger.info("Updated: " + props.toString());
		SCBConfigLoader.loadProperties(props);
		if (active) {
			active = false;
			cache.setEnableCache(SCBConfig.ENABLE_CACHE);
			// check if ontology has been changed:
			if (!ontology.getOntologyID().getOntologyIRI().toString().equals(SCBConfig.ONTOLOGY_IRI)) {
				try {
					// extract filter rules
					Set<OWLAxiom> axioms = null;
					if (ontology != null) {
						axioms = ontology.getAxioms();
					}
					if (SCBConfig.ONTOLOGY_IRI != null) {
						ontology = ontologyCache.getOntology(SCBConfig.ONTOLOGY_IRI, manager);
					} else {
						ontology = manager.getOntology(IRI.create(SCBConfig.FALLBACK_ONTOLOGY_IRI));
						if (ontology == null) {
							ontology = manager.createOntology(IRI.create(SCBConfig.FALLBACK_ONTOLOGY_IRI));
						}
					}
					// add filter rules again
					manager.addAxioms(ontology, axioms);
					// config reasoner
					Configuration conf = new Configuration();
					// conf.prepareReasonerInferences = new
					// PrepareReasonerInferences();
					// conf.prepareReasonerInferences.realisationRequired =
					// true;
					this.reasoner = new Reasoner(conf, ontology);

					// add service if any in queue
					while (serviceQueue.peek() != null) {
						ServiceInfo serviceInfo = serviceQueue.poll();
						addService(serviceInfo);
					}
					active = true;

				} catch (OWLOntologyCreationException e) {
					logger.error("Unable to load ontology", e);
				}
			}
		}
	}

	@Reference(unbind = "unregisterWatchdog", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void registerWatchdog(WatchdogEventService watchdog) {
		logger.info("registerWatchdog");
		this.watchdogService = watchdog;
	}

	public void unregisterWatchdog(WatchdogEventService watchdog) {
		logger.info("unregisterWatchdog");
		this.watchdogService = null;
	}

	@Override
	public synchronized String getOntologyPrint() {
		return getOntologyPrint(Syntax.turtle);
	}

	public synchronized String getOntologyPrint(Syntax syntax) {
		OWLDocumentFormat format = null;
		switch (syntax) {
		case jsonld:
			format = new RDFJsonLDDocumentFormat();
			break;
		case rdfxml:
			format = new RDFXMLDocumentFormat();
			break;

		case turtle:
			format = new TurtleDocumentFormat();
			break;
		case dl:
			format = new DLSyntaxDocumentFormat();
			break;
		case functional:
			format = new FunctionalSyntaxDocumentFormat();
			break;
		case latexDocument:
			format = new LatexDocumentFormat();
			break;
		case manchester:
			format = new ManchesterSyntaxDocumentFormat();
			break;
		default:
			format = new TurtleDocumentFormat();

		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			manager.saveOntology(ontology, baos);
		} catch (OWLOntologyStorageException e) {
			logger.error("Could not write ontology to OutputStream", e);
		}
		return baos.toString();
	}

	@Override
	public synchronized boolean addToOntology(String concepts) {
		// logger.info("Adding new filter "+filterRule);
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		boolean result = false;
		try {
			OWLOntology ont = ontologyManager.loadOntologyFromOntologyDocument(
					new ByteArrayInputStream(concepts.getBytes(StandardCharsets.UTF_8)));
			manager.addAxioms(ontology, ont.axioms());
			// check if ontology is inconsistent
			reasoner.flush();
			result = reasoner.isConsistent();
		} catch (OWLOntologyCreationException e) {
			logger.error("Could not load concepts from <" + concepts + ">", e);
		}
		return result;

	}

	@Override
	public boolean unsubscribe(MCIService handler, OWLMessageFilter filter) {
		String filterIRI = filter.getOWLFilter().getIRI().toString();
		long startTime = System.currentTimeMillis();
		if (!serviceInfoMapping.containsKey(handler)) {
			logger.error("No filters found for service!");
			return false;
		}
		ServiceInfo serviceInfo = serviceInfoMapping.get(handler);
		if (!filterMapping.containsKey(filterIRI)) {
			filter = findEqualFilter(filter);
			if(filter == null){
				logger.error("Filter <"+filterIRI+"> not found");
				return false;
			}
			filterIRI = filter.getOWLFilter().getIRI().toString();
		}
		if (filterMapping.get(filterIRI).size() > 1) {
			// we can just delete the handler, there are still other services
			// subscribed with this filter
			filterMapping.get(filterIRI).remove(handler);
			// add filter to the ServiceInfo
			serviceInfo.removeFilter(filterIRI);
			logger.info("filter still in the ontology(but removed handler): " + filterIRI + " (execTime="
					+ (System.currentTimeMillis() - startTime) + "ms)");
		} else {
			// we need to remove the filter from the ontology
			// add filter to the ServiceInfo
			serviceInfo.removeFilter(filterIRI);

			// add filter to filter list
			filterList.remove(filter);
			filterMapping.remove(filterIRI);
			// remove the filter from the ontology as a sub-concept of message
			OWLClass messageClass = factory.getOWLClass(SCBConfig.EVENT_IRI);
			OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(filter.getOWLFilter(), messageClass);
			// lock ontology so no axioms get lost when multiple threads add
			// axioms at the same time
			synchronized (ontologyLock) {
				OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
				filter.getOWLFilter().accept(remover);
				synchronized (ontologyLock) {
					manager.applyChanges(remover.getChanges());
					manager.removeAxioms(ontology, filter.getAxioms());
					manager.removeAxiom(ontology, subclassAxiom);
				}
			}

			reasoner.flush();
			logger.info("filter removed from the ontology: " + filterIRI + " (execTime="
					+ (System.currentTimeMillis() - startTime) + "ms)");
		}
		return true;

	}

}
