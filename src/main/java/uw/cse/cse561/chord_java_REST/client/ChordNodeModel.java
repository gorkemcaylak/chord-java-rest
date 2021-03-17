package uw.cse.cse561.chord_java_REST.client;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uw.cse.cse561.chord_java_REST.Main;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;

import java.net.URI;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChordNodeModel {
    @JsonUnwrapped
    private ChordNode chordNode;

    @Builder.Default
    private int pathCount = 0;

    @Builder.Default
    private String path = "";

    public ChordNode toChordNode() {
        if (Main.MULTI && chordNode instanceof LocalChordNode) {
            return chordNode;
        } else {
            return RemoteChordNode.builder().chordNode(chordNode).build();
        }
    }
}
