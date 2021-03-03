package uw.cse.cse561.chord_java_REST.chord;

import lombok.Getter;
import uw.cse.cse561.chord_java_REST.ChordApplication;

import java.net.URI;

public class ChordNodeInfo {
    @Getter
    private final URI uri;

    @Getter
    private final int id;

    public ChordNodeInfo(ChordNode node) {
        if (node == null) {
            uri = null;
            id = -1;
        } else {
            uri = node.getUri();
            id = node.getId();
        }
    }

    public RemoteChordNode asRemoteChordNode(ChordApplication application) {
        if (id == -1) {
            return null;
        }
        return RemoteChordNode.builder().uri(uri).id(id).application(application).build();
    }
}
