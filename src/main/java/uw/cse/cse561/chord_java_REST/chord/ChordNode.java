package uw.cse.cse561.chord_java_REST.chord;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.List;

@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class ChordNode {
    @Getter
    @NotNull
    private final URI uri;

    @Getter
    @NotNull
    private final int id;

    public abstract int getMessageCount();

    public abstract void updateFingerTable(ChordNode caller, ChordNode node, int fingerIndex);

    public abstract ChordNode findSuccessor(ChordNode caller, int id);

    public abstract ChordNode findPredecessor(ChordNode caller, int id);

    public abstract ChordNode closestPrecedingNode(ChordNode caller, int id);

    public abstract ChordNode getSuccessor(ChordNode caller);

    public abstract ChordNode getPredecessor(ChordNode caller);
    public abstract void setPredecessor(ChordNode caller, ChordNode predecessor);

    public abstract void notify(ChordNode caller, ChordNode n);

    public abstract List<ChordNode> getFingerTable();

    public abstract void stabilize();

    public abstract void fixFingers();

    // Local chord node would return true. Remote node need pinging,
    // use this for checkPredecessor()
    protected abstract boolean isAlive(ChordNode caller);
}
