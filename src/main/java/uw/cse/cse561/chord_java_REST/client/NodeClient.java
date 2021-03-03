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
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;
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

    public ChordNodeModel findSuccessor(int id) {
        Response response = null;
        try {
            WebTarget targetPath = webTarget.path(NodeResource.FIND_SUCCESSOR).path(String.valueOf(id));
            response = targetPath.request(MediaType.APPLICATION_JSON)
                    .get();
            if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                return response.readEntity(ChordNodeModel.class);
            }
        } catch (ProcessingException ex) {
            ex.printStackTrace();
        } finally {
            if (response !=null) {
                response.close();
            }
        }
        return null;
    }

    public ChordNode getPredecessor() {
        Response response = null;
        try {
            WebTarget targetPath = webTarget.path(NodeResource.GET_PREDECESSOR);
            response = targetPath.request(MediaType.APPLICATION_JSON)
                    .get();
            if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                return response.readEntity(ChordNodeModel.class).toChordNode();
            }
        } catch (ProcessingException ex) {
            ex.printStackTrace();
        } finally {
            if (response !=null) {
                response.close();
            }
        }
        return null;
    }

    public void notify(ChordNode target) {
        try {
            WebTarget targetPath = webTarget.path(NodeResource.NOTIFY);
            Response response = targetPath.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(target, MediaType.APPLICATION_JSON));
            response.close();
        } catch (ProcessingException ex) {
            ex.printStackTrace();
        }
        return;
    }

    public boolean ping() {
        try {
            WebTarget targetPath = webTarget.path(NodeResource.PING);
            Response response = targetPath.request(MediaType.APPLICATION_JSON).get();
            response.close();
            return response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
