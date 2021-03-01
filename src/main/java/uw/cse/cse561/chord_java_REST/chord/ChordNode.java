package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class ChordNode {
    @Getter
    private final URI uri;

    @Getter
    private final int id;

    public abstract ChordNode findSuccessor(int id);


    public abstract ChordNode getPredecessor();

    public abstract void notify(ChordNode n);

    // Local chord node would return true. Remote node need pinging,
    // use this for checkPredecessor()
    protected abstract boolean isAlive();
}
