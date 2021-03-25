package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.Main;
import uw.cse.cse561.chord_java_REST.client.ChordNodeModel;
import uw.cse.cse561.chord_java_REST.client.NodeClient;

import java.util.List;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RemoteChordNode extends ChordNode {
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    NodeClient client;

    protected RemoteChordNode(RemoteChordNodeBuilder<?, ?> b) {
        super(b);
        this.client = new NodeClient(getUri());
        if (Main.MULTI) {
            Exception ex = new IllegalStateException();
            ex.printStackTrace();
            throw new IllegalStateException();
        }
    }

    @Override
    public ChordNodeModel findSuccessor(int id, List<Integer> visited) {
        return client.findSuccessor(id, visited);
    }

    @Override
    public ChordNodeModel findSuccessor(int id) {
        return client.findSuccessor(id);
    }

    @Override
    public void notify(ChordNode n) {
        client.notify(n);
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
