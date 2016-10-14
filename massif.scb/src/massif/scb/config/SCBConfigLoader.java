package massif.scb.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import javax.jws.soap.SOAPBinding.ParameterStyle;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCBConfigLoader {

	private static Logger logger = LoggerFactory.getLogger(SCBConfigLoader.class);

	public static void loadPropertyConfig(BundleContext context) {
		URL url = context.getBundle().getEntry("semanticbus.properties");
		if (url != null) {
			logger.debug("Found property file");
			Properties properties = new Properties();
			InputStream in;
			try {
				in = url.openStream();
				properties.load(in);
			} catch (IOException e1) {
				logger.error("Unable to load properties", e1);
			}
			String prop = properties.getProperty(SCBConfig.PROP_EVENT_IRI);
			if(prop != null){
				SCBConfig.EVENT_IRI = prop;
			}
			if(properties.containsKey(SCBConfig.PROP_ENABLE_CACHE)){
				SCBConfig.ENABLE_CACHE = properties.getProperty(SCBConfig.PROP_ENABLE_CACHE).equals("true") ? true : false;
			}

			prop =  properties.getProperty(SCBConfig.PROP_ONTOLOGY_IRI);
			if(prop != null){
				SCBConfig.ONTOLOGY_IRI = prop;
			}
			if(properties.containsKey(SCBConfig.PROP_FILTER_CHECKING)){
				SCBConfig.FILTER_CHECKING = parseSyntaxCheking(properties.get(SCBConfig.PROP_FILTER_CHECKING).toString());
			}
		}

	}

	public static void loadRuntimeConfig(BundleContext context) {
		String prop = context.getProperty(SCBConfig.PROP_EVENT_IRI);
		if (prop != null) {
			SCBConfig.EVENT_IRI = prop;
		}
		prop = context.getProperty(SCBConfig.PROP_ONTOLOGY_IRI);
		if (prop != null) {
			SCBConfig.ONTOLOGY_IRI = prop;
		}
		prop = context.getProperty(SCBConfig.PROP_ENABLE_CACHE);
		if (prop != null) {
			SCBConfig.ENABLE_CACHE = prop.equals("true") ? true : false;
		}
		prop = context.getProperty(SCBConfig.PROP_FILTER_CHECKING);
		if (prop != null) {
			SCBConfig.FILTER_CHECKING = parseSyntaxCheking(prop);
		}

	}
	
	public static void loadProperties(Map<String,String> properties){
		if(properties != null){
			String prop = properties.get(SCBConfig.PROP_EVENT_IRI).toString();
			if(prop != null){
				SCBConfig.EVENT_IRI = prop;
			}
			if(properties.containsKey(SCBConfig.PROP_ENABLE_CACHE)){
				SCBConfig.ENABLE_CACHE = properties.get(SCBConfig.PROP_ENABLE_CACHE).equals("true") ? true : false;
			}		
			prop =  properties.get(SCBConfig.PROP_ONTOLOGY_IRI).toString();
			if(prop != null){
				SCBConfig.ONTOLOGY_IRI = prop;
			}
			if(properties.containsKey(SCBConfig.PROP_FILTER_CHECKING)){
				SCBConfig.FILTER_CHECKING = parseSyntaxCheking(properties.get(SCBConfig.PROP_FILTER_CHECKING).toString());
			}
		}
	}
	public static void loadProperties(Dictionary<String,Object> properties){
		if(properties != null){
			if(properties.get(SCBConfig.PROP_EVENT_IRI)!= null){
				SCBConfig.EVENT_IRI = properties.get(SCBConfig.PROP_EVENT_IRI).toString();
			}
			if(properties.get(SCBConfig.PROP_ENABLE_CACHE)!=null){
				SCBConfig.ENABLE_CACHE = properties.get(SCBConfig.PROP_ENABLE_CACHE).equals("true") ? true : false;
			}		
			if(properties.get(SCBConfig.PROP_ONTOLOGY_IRI) != null){
				SCBConfig.ONTOLOGY_IRI = properties.get(SCBConfig.PROP_ONTOLOGY_IRI).toString();
			}
			if(properties.get(SCBConfig.PROP_FILTER_CHECKING)!=null){
				SCBConfig.FILTER_CHECKING = parseSyntaxCheking(properties.get(SCBConfig.PROP_FILTER_CHECKING).toString());
			}
		}
	}
	
	private static SCBConfig.FilterCheckingStrategy parseSyntaxCheking(String syntaxString){
		SCBConfig.FilterCheckingStrategy strategy = null;
		switch(syntaxString.trim().toLowerCase()){
		case "reasoning": strategy = SCBConfig.FilterCheckingStrategy.REASONING;
			break;
		case "reason": strategy = SCBConfig.FilterCheckingStrategy.REASONING;
			break;
		case "syntax": strategy = SCBConfig.FilterCheckingStrategy.SYNTAX;
			break;
		case "none": strategy = SCBConfig.FilterCheckingStrategy.NONE;
			break;
		default: strategy = SCBConfig.FilterCheckingStrategy.NONE;
			break;
			
		
		}
		
		return strategy;
	}

}
