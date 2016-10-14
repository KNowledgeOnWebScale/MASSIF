package massif.generic.service.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import massif.parallelization.api.LocalConfigurator;


@Component(immediate=true, property={"service.pid=massif.generic.service.factory.GenericServiceFactory"},
configurationPolicy=ConfigurationPolicy.REQUIRE)
public class GenericServiceFactory {
	
	private static String DIR = "services";
	private static String FILE_NAME = "generic.config";
	private LocalConfigurator localConfig; 

	
	@Activate
	public void activate(ComponentContext context){	
		Dictionary properties = context.getProperties();	
		System.out.println(properties);
		File locationFile = context.getBundleContext().getDataFile(DIR);
		String location = locationFile.getAbsolutePath().toString();
		if(locationFile.getAbsolutePath().toString().contains("generated/fw/bundle")){
			location = location.replaceAll("generated/fw/bundle[0-9]*/data/", "");
			System.out.println(location);
		}
		location += "/" + FILE_NAME;
		System.out.println(location);
		File readFile = new File(location);
		Scanner scanner = null;
		try {
			scanner = new Scanner( readFile );
			String input = scanner.useDelimiter("\\A").next();
			JSONParser parser = new JSONParser();
			Object json = parser.parse(input);
			System.out.println(json);
			List<Map<String,Object>> jsonList = (List<Map<String,Object>>)json;
			for(Map<String,Object> jsonItem: jsonList){
				Dictionary<String, Object> dict = new Hashtable<String,Object>();
				dict.put("name", jsonItem.get("name"));
				dict.put("description", jsonItem.get("description"));
				List<Map<String,String>> inputs = (List<Map<String,String>>)jsonItem.get("input");
				for(int i =0;i<inputs.size();i++){
					dict.put("input."+i, inputs.get(i).get("input"));
					dict.put("inputName."+i, inputs.get(i).get("name"));
				}
				dict.put("ontology", jsonItem.get("ontology"));
				List<String> queries = (List<String>) jsonItem.get("query");
				for(int i = 0; i<queries.size();i++){
					dict.put("query."+i, queries.get(i));
				}
				//start the new service
				localConfig.startService("massif.generic.service.GenericService", dict);
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}finally{
			scanner.close(); // Put this call in a finally block
		}
		
		
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unbindLocalConfig")
	public void bindLocalConfig(LocalConfigurator localConf) {
		this.localConfig = localConf;
	}

	public void unbindLocalConfig(LocalConfigurator localConf) {
		if(this.localConfig == localConf){
			this.localConfig = null;
		}
	}

}
