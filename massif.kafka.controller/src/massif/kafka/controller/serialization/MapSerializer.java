package massif.kafka.controller.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class MapSerializer implements Serializer<Map<Object, Object>> {

	// Kryo serializer
	private Kryo serializer;
	
	// The byte array used to write the object
	private ByteArrayOutputStream outputbytearray;

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {
		serializer = SerializationFactory.getKryoInstace();
	}

	@Override
	public byte[] serialize(String topic, Map<Object, Object> data) {
		// Check if we have a valid serializer;
		if (serializer == null) {
			throw new SerializationException("Error when serializing map to byte[] due to invalid serializer.");
		}
		
		// Create bytearray to send over the network
		outputbytearray = new ByteArrayOutputStream();
		Output output = new Output(outputbytearray);
					
		// Write object to outputstream
		serializer.writeClassAndObject(output, data);
		output.flush();
		
		return outputbytearray.toByteArray();
	}

	@Override
	public void close() {
		if (outputbytearray != null) {
			try {
				outputbytearray.close();
			} catch (IOException e) {
				throw new SerializationException("Could not close the bytearray input when serializing map to byte[].");
			}
		}
	}
	
}
