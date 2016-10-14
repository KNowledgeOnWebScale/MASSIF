package massif.logging.testing;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import massif.journal.api.JournalService;

@Component(immediate = true)
public class LogTester implements JournalService {

	
	// HACK HACK HACK
	public static String currentMessageID = "";
	
	private final ExecutorService threadpool = Executors.newFixedThreadPool(1);
	private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
	private Map<String, ArrayList<String>> logTimes;
	private Map<String,ArrayList<String>> logTimesSequential;
	private long lastModification = System.currentTimeMillis();
	private PrintWriter writer;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public void activate() {
		logTimes = new HashMap<String, ArrayList<String>>();
		logTimesSequential =  new HashMap<String, ArrayList<String>>();
		
		/*timer.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				System.out.println("Called "+(System.currentTimeMillis() - lastModification));
				if ((System.currentTimeMillis() - lastModification) > (1000 * 20)) {
					try {
						writer = new PrintWriter("C:/tmp/nomralBus.txt");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for (String key : logTimes.keySet()) {
						for (String time : logTimes.get(key)) {
							System.out.println(key + "\t" + time);
							writer.println(key + "\t" + time);
						}
						System.out.println();
						writer.println("");
					}
					System.out.println("Path:");
					writer.println("Path");
					for (String key : logTimesSequential.keySet()) {
						for (String time : logTimesSequential.get(key)) {
							System.out.println(key + "\t" + time);
							writer.println(key + "\t" + time);
						}
						System.out.println();
						writer.println("");
					}
					writer.close();
				}
				
			}

		}, 60, 20, TimeUnit.SECONDS);*/
	}

	@Override
	public void log(final int time, final String message, String uriId, BundleContext ofUsingService, ServiceReference ofUsedService,
			String functionName, Map args) {
		

	}

	@Override
	public void log(int level, String message, String uriId, BundleContext ofUsingService, Class usedServiceObjectClass, String filter,
			String functionName, Map args) {
		boolean end = usedServiceObjectClass==null?true:false;
		logger.info("PacketID:\t" + uriId + "\tComponent:\t"+message+"\tEnd:\t"+end+"\tTime:\t"+System.currentTimeMillis()+"\tFunctionName:\t"+functionName);
	}

	@Override
	public void log(int level, String message, String uriId, BundleContext ofUsingService, Class usedServiceObjectClass, String filter,
			String functionName, Serializable args, List rules) {
		boolean end = usedServiceObjectClass==null?true:false;
		logger.info("PacketID:\t" + uriId + "\tComponent:\t"+message+"\tEnd:\t"+end+"\tTime:\t"+System.currentTimeMillis()+"\tFunctionName:\t"+functionName);
	}

	@Override
	public void log(int level, String message, String uriId, BundleContext ofUsingService, Class usedServiceObjectClass, String filter,
			String functionName, Serializable args) {
		boolean end = usedServiceObjectClass==null?true:false;
		logger.info("PacketID:\t" + uriId + "\tComponent:\t"+message+"\tEnd:\t"+end+"\tTime:\t"+System.currentTimeMillis()+"\tFunctionName:\t"+functionName);
	}

	@Override
	public void logEndPoint(int level, String message, String uriId, BundleContext ofUsingService, String functionName) {
		// TODO Auto-generated method stub
		System.out.println(message);

	}

//	@Override
//	public JSONObject takeFromQueue(String bundleName) throws NoQueueAcquiredException {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void addPersistenceBundle(String bundleName) throws QueueAlreadyAcquiredException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePersistenceBundle(String bundleName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(final int time, final String message) {
		threadpool.execute(new Runnable() {

			@Override
			public void run() {
				String[] input=message.split(";");
				if (!logTimes.containsKey(input[0])) {
					logTimes.put(input[0], new ArrayList<String>());
				}
				if(!logTimesSequential.containsKey(input[1])){
					logTimesSequential.put(input[1], new ArrayList<String>());
				}
				logTimes.get(input[0]).add(input[1]+"\t"+time);
				logTimesSequential.get(input[1]).add(input[0]+"\t"+time);
				//System.out.println(input[1]+"\t"+input[0]+"\t"+time);
				lastModification = System.currentTimeMillis();
			}
		});

	}

	@Override
	public void log(ServiceReference sr, int level, String message) {
		// TODO Auto-generated method stub

	}

}
