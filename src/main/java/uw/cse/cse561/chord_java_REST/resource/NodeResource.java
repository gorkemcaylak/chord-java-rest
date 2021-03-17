package uw.cse.cse561.chord_java_REST.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.client.ChordNodeModel;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Path(NodeResource.NODE_RESOURCE_PATH)
@SuperBuilder
public class NodeResource {
    public static final String NODE_RESOURCE_PATH = "/chord";
    public static final String FIND_SUCCESSOR = "/find-successor";
    public static final String GET_PREDECESSOR = "/get-predecessor";
    public static final String NOTIFY = "/notify";
    public static final String PING = "/ping";
    public static final String FINGER_TABLE = "/finger";

    private LocalChordNode chordNode;

    @GET
    @Path(FIND_SUCCESSOR + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNodeModel findSuccessor(@PathParam("id") int id) {
        ChordNodeModel ret = chordNode.findSuccessor(id);
        if (ret == null) {
            throw new NotFoundException();
        }
        return ret;
    }

    @POST
    @Path(FIND_SUCCESSOR + "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNodeModel findSuccessor(@PathParam("id") int id, VisitedModel visitedModel) {
        ChordNodeModel ret = chordNode.findSuccessor(id, visitedModel.getVisited());
        if (ret == null) {
            throw new NotFoundException();
        }
        return ret;
    }

    @GET
    @Path(GET_PREDECESSOR)
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNode getPredecessor() {
        ChordNode ret = chordNode.getPredecessor();
        if (ret == null) {
            throw new NotFoundException();
        }
        return ret;
    }

    @POST
    @Path(NOTIFY)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notify(ChordNodeModel target) {
        chordNode.notify(target.toChordNode());
        return Response.ok().build();
    }

    @GET
    @Path(PING)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.ok().build();
    }

    @GET
    @Path(FINGER_TABLE)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Integer, ChordNode> finger() {
        Map<Integer, ChordNode> ret = new HashMap<>();
        for (int i = 0; i < chordNode.getFingerTable().size(); ++i) {
            ret.put((chordNode.getId() + (int) Math.pow(2, i)) % chordNode.getChordSize(), chordNode.getFingerTable().get(i));
        }

        return ret;
    }
}