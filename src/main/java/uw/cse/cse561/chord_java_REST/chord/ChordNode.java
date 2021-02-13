package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@SuperBuilder
@EqualsAndHashCode
@ToString
public abstract class ChordNode {
    @Getter
    private URI uri;

    @Getter
    private int id;

    @JsonIgnore
    @Getter @Setter(AccessLevel.PROTECTED)
    private ChordNode predecessor;

    @JsonIgnore
    @Getter @Setter(AccessLevel.PROTECTED)
    private ChordNode successor;

    public abstract ChordNode findSuccessor(ChordNode n);

    public abstract void notify(ChordNode n);

    // Local chord node would return true. Remote node need pinging,
    // use this for checkPredecessor()
    protected abstract boolean isAlive();
}
