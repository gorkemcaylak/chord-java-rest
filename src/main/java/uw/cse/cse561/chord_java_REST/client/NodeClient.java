package uw.cse.cse561.chord_java_REST.client;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.resource.NodeResource;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class NodeClient {
    public static final long CONNECT_TIMEOUT_SEC = 300;
    public static final long READ_TIMEOUT_SEC = 300;

    private final WebTarget webTarget;

    public NodeClient(URI baseUri) {
        Client client = ClientBuilder.newBuilder().connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS).build();
        webTarget = client.target(UriBuilder.fromUri(baseUri).path(NodeResource.NODE_RESOURCE_PATH));
    }

    public ChordNode findSuccessor(int id) {
        try {
            WebTarget targetPath = webTarget.path(NodeResource.FIND_SUCCESSOR).path(String.valueOf(id));
            Response response = targetPath.request(MediaType.APPLICATION_JSON)
                    .get();
            return response.readEntity(ChordNode.class);
        } catch (ProcessingException ex) {
        }
        return null;
    }

    public ChordNode getPredecessor() {
        try {
            WebTarget targetPath = webTarget.path(NodeResource.GET_PREDECESSOR);
            Response response = targetPath.request(MediaType.APPLICATION_JSON)
                    .get();
            return response.readEntity(ChordNode.class);
        } catch (ProcessingException ex) {
        }
        return null;
    }

    public void notify(ChordNode target) {
        try {
            WebTarget targetPath = webTarget.path(NodeResource.NOTIFY);
            targetPath.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(target, MediaType.APPLICATION_JSON));
        } catch (ProcessingException ex) {
        }
        return;
    }

    public boolean ping() {
        try {
            WebTarget targetPath = webTarget.path(NodeResource.PING);
            Response response = targetPath.request(MediaType.APPLICATION_JSON).get();
            return response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
