package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.UriBuilder;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;

import java.net.URI;

@Path("/hello-world")
public class HelloResource {
    @GET
    @Produces("application/json")
    public ChordNode hello() {
        return LocalChordNode.create(UriBuilder.fromUri("http://localhost/").port(8080).build(),
                0, 128);
    }
}