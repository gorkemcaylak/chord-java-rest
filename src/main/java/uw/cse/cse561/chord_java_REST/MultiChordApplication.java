package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import lombok.Builder;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.resource.NodeMultiResource;
import uw.cse.cse561.chord_java_REST.resource.NodeResource;

import java.util.Set;

@ApplicationPath("/")
@Builder
public class MultiChordApplication extends Application {
    private String hostname;
    private int port;
    private int chordLength;

    @Override
    public Set<Object> getSingletons() {
        NodeMultiResource nodeResource = NodeMultiResource.builder().hostname(hostname).port(port).chordLength(chordLength).build();
        return Set.of(nodeResource);
    }
}