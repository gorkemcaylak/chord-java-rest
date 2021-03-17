package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.ChordApplication;
import uw.cse.cse561.chord_java_REST.client.NodeClient;
import uw.cse.cse561.chord_java_REST.resource.NodeResource;

import java.util.List;


@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RemoteChordNode extends ChordNode {
    @JsonIgnore
    NodeClient client;

    @JsonIgnore
    @NotNull
    ChordApplication application;

    protected RemoteChordNode(RemoteChordNodeBuilder<?, ?> b) {
        super(b);
        this.client = new NodeClient(application, getUri(), getId());
    }

    @Override
    public ChordNode findSuccessor(int id) {
        return client.findSuccessor(id);
    }

    @Override
    public void notify(ChordNode n) {
        client.notify(n);
    }

    @Override
    public List<ChordNode> getFingerTable() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isAlive() {
        return client.ping();
    }

    @Override
    public ChordNode getPredecessor() {
        return client.getPredecessor();
    }

    public static abstract class RemoteChordNodeBuilder<C extends RemoteChordNode, B extends RemoteChordNodeBuilder<C, B>> extends ChordNodeBuilder<C, B> {
        public B client(NodeClient client) {
            throw new UnsupportedOperationException("un-settable field");
        }

        public B chordNode(ChordNode node) {
            id(node.getId());
            uri(node.getUri());
            return self();
        }
    }
}
