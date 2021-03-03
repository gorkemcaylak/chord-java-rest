package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.client.ChordNodeModel;

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

    public abstract ChordNodeModel findSuccessor(int id);

    @JsonIgnore
    public abstract ChordNode getPredecessor();

    public abstract void notify(ChordNode n);

    // Local chord node would return true. Remote node need pinging,
    // use this for checkPredecessor()
    protected abstract boolean isAlive();

    public ChordNodeModel toChordNodeModel() {
        return ChordNodeModel.builder().uri(uri).id(id).build();
    }

}
