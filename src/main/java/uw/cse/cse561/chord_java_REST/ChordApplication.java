package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import lombok.Builder;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.ChordNodeInfo;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.resource.NodeResource;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationPath("/")
@Builder
public class ChordApplication extends Application {
    private final Map<Integer, ChordNode> chordNodes;

    @Override
    public Set<Object> getSingletons() {
        NodeResource nodeResource = NodeResource.builder().application(this).build();
        return Set.of(nodeResource);
    }

    public ChordNode getNode(int id) {
        return chordNodes.get(id);
    }

    public ChordNode getNode(ChordNodeInfo node) {
        if (node == null) {
            return null;
        }

        if (!chordNodes.containsKey(node.getId())) {
            chordNodes.put(node.getId(), node.asRemoteChordNode(this));
        }
        return chordNodes.get(node.getId());
    }
}