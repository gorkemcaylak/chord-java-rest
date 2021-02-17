package uw.cse.cse561.chord_java_REST.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;

@Path(NodeResource.NODE_RESOURCE_PATH)
@SuperBuilder
public class NodeResource {
    public static final String NODE_RESOURCE_PATH = "/chord";
    public static final String FIND_SUCCESSOR = "/find-successor";
    public static final String GET_PREDECESSOR = "/get-predecessor";
    public static final String NOTIFY = "/notify";
    public static final String PING = "/ping";

    private LocalChordNode chordNode;

//    @GET
//    @Path(FIND_SUCCESSOR + "/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ChordNode findSuccessor(@PathParam("id") int id) {
//        return chordNode.findSuccessor(id);
//    }
//
//    @GET
//    @Path(GET_PREDECESSOR)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ChordNode getPredecessor() {
//        return chordNode.getPredecessor();
//    }
//
//    @POST
//    @Path(NOTIFY)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response notify(ChordNode target) {
//        chordNode.notify(target);
//        return Response.ok().build();
//    }
//
//    @GET
//    @Path(PING)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response ping() {
//        return Response.ok().build();
//    }
}