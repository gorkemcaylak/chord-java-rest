package uw.cse.cse561.chord_java_REST.chord;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class RemoteChordNode extends ChordNode {

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
        throw new UnsupportedOperationException();
    }

    public ChordNode getPredecessor() {
        throw new UnsupportedOperationException();
    }
}
