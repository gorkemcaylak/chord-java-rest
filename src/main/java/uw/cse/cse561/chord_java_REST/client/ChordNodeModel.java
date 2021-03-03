package uw.cse.cse561.chord_java_REST.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;

import java.net.URI;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChordNodeModel {
    private URI uri;
    private int id;
    @Builder.Default
    private int pathCount = 0;

    public ChordNode toChordNode() {
        return RemoteChordNode.builder().uri(uri).id(id).build();
    }
}
