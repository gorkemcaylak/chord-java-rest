package uw.cse.cse561.chord_java_REST.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Builder;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;
import uw.cse.cse561.chord_java_REST.client.ChordNodeModel;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/")
@Builder
public class NodeMultiResource {
    public static final String FIND_SUCCESSOR = "/find-successor";
    public static final String GET_PREDECESSOR = "/get-predecessor";
    public static final String NOTIFY = "/notify";
    public static final String PING = "/ping";
    public static final String CREATE = "/create";
    public static final String JOIN = "/join";
    public static final String LEAVE = "/leave";
    public static final String FINGER_TABLE = "/finger";

    private final String hostname;
    private final int port;
    private final int chordLength;

    @Builder.Default
    private Map<Integer, LocalChordNode> chordNodes = new ConcurrentHashMap<>();

    @GET
    @Path("/{my_id}" + FIND_SUCCESSOR + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNodeModel findSuccessor(@PathParam("my_id") int my_id, @PathParam("id") int id) {
        LocalChordNode current = chordNodes.get(my_id);
        ChordNodeModel ret = null;
        if (current != null) {
            ret = current.findSuccessor(id);
        }
        if (current == null || ret == null) {
            throw new NotFoundException();
        }
        return ret;
    }

    @POST
    @Path("/{my_id}" + FIND_SUCCESSOR + "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNodeModel findSuccessor(@PathParam("my_id") int my_id, @PathParam("id") int id, VisitedModel visitedModel) {
        LocalChordNode current = chordNodes.get(my_id);
        ChordNodeModel ret = null;
        if (current != null) {
            ret = current.findSuccessor(id, visitedModel.getVisited());
        }
        if (current == null || ret == null) {
            throw new NotFoundException();
        }
        return ret;
    }

    @GET
    @Path("/{my_id}" + GET_PREDECESSOR)
    @Produces(MediaType.APPLICATION_JSON)
    public ChordNode getPredecessor(@PathParam("my_id") int my_id) {
        LocalChordNode current = chordNodes.get(my_id);
        ChordNode ret = null;
        if (current != null) {
            ret = current.getPredecessor();
        }
        if (current == null || ret == null) {
            throw new NotFoundException();
        }
        return ret;
    }

    @POST
    @Path("/{my_id}" + NOTIFY)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notify(@PathParam("my_id") int my_id, ChordNodeModel target) {
        LocalChordNode current = chordNodes.get(my_id);
        if (current == null) {
            throw new NotFoundException();
        }
        current.notify(target.toChordNode());
        return Response.ok().build();
    }

    @GET
    @Path("/{my_id}" + PING)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping(@PathParam("my_id") int my_id) {
        LocalChordNode current = chordNodes.get(my_id);
        if (current == null) {
            throw new NotFoundException();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{my_id}" + CREATE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("my_id") int my_id) {
        URI remoteAccessUri = UriBuilder
                .fromPath("/")
                .scheme("http")
                .host(hostname)
                .port(port)
                .build();
        LocalChordNode current = chordNodes.putIfAbsent(my_id, LocalChordNode.create(remoteAccessUri, my_id, chordLength));
        if (current != null) {
            throw new BadRequestException("NodeAlreadyExist");
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{my_id}" + JOIN + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response join(@PathParam("my_id") int my_id, @PathParam("id") int id) {
        LocalChordNode current = chordNodes.get(my_id);
        if (current == null) {
            throw new NotFoundException();
        }

        LocalChordNode joiningNode = chordNodes.get(id);
        if (joiningNode == null) {
            throw new BadRequestException();
        }

        if (current.join(joiningNode)) {
            return Response.ok().build();
        } else {
            throw new InternalServerErrorException("Something is horribly wrong");
        }
    }

    @GET
    @Path("/{my_id}" + LEAVE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response leave(@PathParam("my_id") int my_id) {
        LocalChordNode current = chordNodes.remove(my_id);
        if (current == null) {
            throw new NotFoundException();
        }
        current.shutdownNode();
        return Response.ok().build();
    }

    @GET
    @Path("/{my_id}" + FINGER_TABLE)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Integer, ChordNode> finger(@PathParam("my_id") int my_id) {
        LocalChordNode current = chordNodes.get(my_id);
        if (current == null) {
            throw new NotFoundException();
        }
        Map<Integer, ChordNode> ret = new HashMap<>();
        for (int i = 0; i < current.getFingerTable().size(); ++i) {
            ret.put((my_id + (int) Math.pow(2, i)) % chordLength, current.getFingerTable().get(i));
        }

        return ret;
    }

    public static class NodeMultiResourceBuilder {
        public NodeMultiResourceBuilder chordNodes(Map<Integer, LocalChordNode> chordNodes) {
            throw new IllegalArgumentException();
        }
    }
}