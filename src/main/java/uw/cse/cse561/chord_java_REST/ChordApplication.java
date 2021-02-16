package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import lombok.Builder;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.resource.NodeResource;

import java.util.Set;

@ApplicationPath("/")
@Builder
public class ChordApplication extends Application {
    private LocalChordNode chordNode;

    @Override
    public Set<Object> getSingletons() {
        NodeResource nodeResource = NodeResource.builder().chordNode(chordNode).build();
        return Set.of(nodeResource);
    }
}