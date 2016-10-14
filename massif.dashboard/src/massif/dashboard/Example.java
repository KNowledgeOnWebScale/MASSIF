package massif.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
@Path("todo")
@Component(immediate=true,service=Object.class)
public class Example {

	//@Activate
	public void start(BundleContext context) {
		System.out.println("Started Dashboard");
	}	
	// TODO: class provided by template
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{user}")
	public Response list(@PathParam("user") String user) {
		System.out.println(user);
		List<String> list = new ArrayList<String>();
		list.add(user);
		if(list != null) {
			return Response.ok(list).build();
		} else {
			return Response.status(503).build();
		}
	}
}
