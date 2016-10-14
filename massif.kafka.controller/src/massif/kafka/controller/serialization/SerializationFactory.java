package massif.kafka.controller.serialization;

import java.util.Arrays;

import org.objenesis.strategy.StdInstantiatorStrategy;

import massif.kafka.api.KafkaController;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

public class SerializationFactory {

	/**
	 * Kryo serializer 
	 */
	private static Kryo kryoInstance;
	
	/**
	 * FACTORY GETTER kryoInstance
	 * @return				The kryo instance
	 */
	public static Kryo getKryoInstace() {
		// Create a new kryo instance if it does not exist
		if (kryoInstance == null) {
			kryoInstance = new Kryo();
			kryoInstance.setAutoReset(true);
			kryoInstance.setClassLoader(KafkaController.class.getClassLoader());
			// Sometimes problems with serializing exceptions in Kryo (e.g. Throwable discrepancy between android/jdk)
			kryoInstance.addDefaultSerializer(Throwable.class, JavaSerializer.class);
			// required to instantiate classes without no-arg constructor
			kryoInstance.setInstantiatorStrategy(new StdInstantiatorStrategy());
			// required to correctly handle unmodifiable collections (i.e. used in EndpointDescription)
			UnmodifiableCollectionsSerializer.registerSerializers(kryoInstance);
			// required to correctly handle Arrays$ArrayList class (i.e. used in EndpointDescription)
			kryoInstance.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
		}
		
		return kryoInstance;
	}
		
}
