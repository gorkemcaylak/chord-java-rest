package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@SuperBuilder
public class LocalChordNode extends ChordNode {
    @JsonIgnore
    @Getter @Setter(AccessLevel.PROTECTED)
    private ChordNode predecessor;

    @JsonIgnore
    @Getter @Setter(AccessLevel.PROTECTED)
    private ChordNode successor;

    @JsonIgnore
    @Getter
    // Use this value to limit chord size for testing.
    // n in (n1, n2) means
    // n in (n1, n1 =< n2 ? n2 : chordSize - 1)
    // or n in (n1 =< n2 ? n1 : 0, n2)
    private int chordSize;

    @Override
    public ChordNode findSuccessor(ChordNode n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notify(ChordNode n) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isAlive() {
        return true;
    }

    private void join(ChordNode n) {
        throw new UnsupportedOperationException();
    }

    private ChordNode closestPrecedingNode() {
        throw new UnsupportedOperationException();
    }

    private void stabilize() {
        throw new UnsupportedOperationException();
    }

    private void fixFingers() {
        throw new UnsupportedOperationException();
    }

    private void checkPredecessor() {
        throw new UnsupportedOperationException();
    }

    public static LocalChordNode create(URI uri, int id, int chordSize) {
        LocalChordNode newNode = LocalChordNode.builder()
                .uri(uri)
                .id(id)
                .chordSize(chordSize)
                .predecessor(null)
                .successor(null)
                .build();
        newNode.setSuccessor(newNode);
        // TODO: Implement timers for periodical action.

        return newNode;
    }
}
