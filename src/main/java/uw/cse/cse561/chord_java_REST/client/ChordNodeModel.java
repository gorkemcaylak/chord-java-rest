package uw.cse.cse561.chord_java_REST.client;

import lombok.Data;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;

import java.net.URI;

@Data
public class ChordNodeModel {
    private URI uri;
    private int id;

    public ChordNode toChordNode() {
        return RemoteChordNode.builder().uri(uri).id(id).build();
    }
}
