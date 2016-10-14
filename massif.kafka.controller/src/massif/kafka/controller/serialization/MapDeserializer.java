package massif.kafka.controller.serialization;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class MapDeserializer implements Deserializer<Map<Object, Object>> {

	// Kryo serializer
	private Kryo serializer;
	
	// The incoming message input
	private Input input;

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {
		serializer = SerializationFactory.getKryoInstace();
	}

	@Override
	public Map<Object, Object> deserialize(String topic, byte[] data) {
		// Read the byte array
		input = new Input(new ByteArrayInputStream(data));
		
		return (Map<Object, Object>) serializer.readClassAndObject(input);
	}
	
	@Override
	public void close() {
		if (input != null) {
			input.close();
		}
	}

}
