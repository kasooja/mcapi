package org.insightcentre.mcapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/demo")
public class GrctcApi {
	
	private static final String TITLE = "Regulation Classification";
	
	private static final String FACETS = "{" + 
	    "\"Domain Specific Provisions\": {" + 
	        "\"labels\": [\"Customer Due Diligence\","+ 
	        "\"Enforcement\","+
	        "\"Reporting\","+
	        "\"Monitoring\""+ 
	        "]" +
	    "}," +  
	    "\"Generic Provisions\":{" + 
	        "\"labels\": [\"Prohibition\"," + 
	        "\"Obligation\"," + 
	        "\"Others\"]" +
	    "}" + 	        
	"}";

//	@GET
//	@Path("/{param}")
//	public Response getMsg(@PathParam("param") String msg) {
//		String output = "Jersey say : " + msg;
//		return Response.status(200).entity(output).build();
//	}
	
	@GET
	@Path("/title")
	public Response getTitle() {
		return Response.status(200).entity(TITLE).build();
	}

	@GET
	@Path("/facets")
	public Response getSchema() {
		return Response.status(200).entity(FACETS).build();
	}
	
}


 