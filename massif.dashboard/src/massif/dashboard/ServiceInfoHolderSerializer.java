package massif.dashboard;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ServiceInfoHolderSerializer  extends JsonSerializer<ServiceInfoHolder>{

	@Override
	public void serialize(ServiceInfoHolder serviceInfos, JsonGenerator jgen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		//jgen.writeArrayFieldStart("services");
		jgen.writeStartArray();
		for(ServiceInfo serviceInfo: serviceInfos.getServices()){
			jgen.writeStartObject();
			//jgen.writeObjectField("service", serviceInfo);
			jgen.writeStringField("serviceId", serviceInfo.getServiceID());
			jgen.writeStringField("componentName", serviceInfo.getComponentName());
			jgen.writeBooleanField("adaptable", serviceInfo.isAdaptable());
			
			//write queries	
			jgen.writeArrayFieldStart("queries");
			for(String query: serviceInfo.getQueries()){			
				jgen.writeString(query);
			}
			jgen.writeEndArray();
			//write filter rules
			jgen.writeArrayFieldStart("filterRules");
			for(String query: serviceInfo.getFilterRules()){			
				jgen.writeString(query);
			}
			jgen.writeEndArray();
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		/*jgen.writeStartObject();
		jgen.writeStartArray();
		for(ServiceInfo serviceInfo: serviceInfos.getServices()){
			arg2.defaultSerializeValue(serviceInfo, jgen);
			
		}
		jgen.writeEndArray();
	    jgen.writeEndObject();*/
	}
	
}
