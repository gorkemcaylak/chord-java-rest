package uw.cse.cse561.chord_java_REST.resource;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.ChordApplication;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.ChordNodeInfo;

import java.util.List;

@Path(NodeResource.NODE_RESOURCE_PATH)
@SuperBuilder
public class NodeResource {
    public static final String NODE_RESOURCE_PATH = "/chord";
    public static final String FIND_SUCCESSOR = "/find-successor";
    public static final String GET_PREDECESSOR = "/get-predecessor";
    public static final String NOTIFY = "/notify";
    public static final String GET_FINGER_TABLE = "/get-finger-table";
    public static final String PING = "/ping";

    @NotNull
    private final ChordApplication application;

    @GET
    @Path(FIND_SUCCESSOR)
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNodeInfo findSuccessor(@QueryParam("id") int id, @QueryParam("key") int key) {
        ChordNodeInfo ret = new ChordNodeInfo(application.getNode(id).findSuccessor(key));
        return ret;
    }

    @GET
    @Path(GET_PREDECESSOR)
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNodeInfo getPredecessor(@QueryParam("id") int id) {
        ChordNodeInfo ret = new ChordNodeInfo(application.getNode(id).getPredecessor());
        return ret;
    }

    @POST
    @Path(NOTIFY)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notify(@QueryParam("id") int id, ChordNodeInfo target) {
        application.getNode(id).notify(application.getNode(target));
        return Response.ok().build();
    }

    @GET
    @Path(GET_FINGER_TABLE)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChordNode> getFingerTable(@QueryParam("id") int id) {
        return application.getNode(id).getFingerTable();
    }

    @GET
    @Path(PING)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.ok().build();
    }
}