package uw.cse.cse561.chord_java_REST.chord;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RemoteChordNode extends ChordNode {

    @Override
    public ChordNode findSuccessor(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notify(ChordNode n) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isAlive() {
        throw new UnsupportedOperationException();
    }

    public ChordNode getPredecessor() {
        throw new UnsupportedOperationException();
    }
}
