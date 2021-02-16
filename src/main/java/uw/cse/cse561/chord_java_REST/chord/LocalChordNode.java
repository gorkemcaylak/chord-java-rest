package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.*;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LocalChordNode extends ChordNode {
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter @Setter(AccessLevel.PROTECTED)
    private LocalChordNode predecessor;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter @Setter(AccessLevel.PROTECTED)
    private LocalChordNode successor;

    @JsonIgnore
    @Getter
    // Use this value to limit chord size for testing.
    // n in (n1, n2) means
    // n in (n1, n1 =< n2 ? n2 : chordSize - 1)
    // or n in (n1 =< n2 ? n1 : 0, n2)
    private final int keySpaceSize;

    // size = log(ChordSize-1)
    // contains successor nodes to 2^i jumps
    // interval is [2^i , 2^(i+1))
    // 1 2 4 8

    private static class Finger {
        LocalChordNode node;
        int start;
    }
    private final List<Finger> fingerTable = new ArrayList<>();

    private final Map<String, String> storage = new HashMap<>();

    @Override
    protected boolean isAlive() {
        return true;
    }

    private boolean inRange(int n, int start, int end) {
        assert 0 <= n && n < keySpaceSize;
        if (start == end) {
            return n != start;
        } else if (start < end) {
            return start < n && n < end;
        } else {
            return start < n || n < end;
        }
    }

    private LocalChordNode closestPrecedingFinger(int key) {
        for (int i = fingerTable.size() - 1; i >= 0; i--) {
            if (inRange(fingerTable.get(i).node.getId(), this.getId(), key)) {
                return fingerTable.get(i).node;
            }
        }
        return this;
    }

    private LocalChordNode findPredecessor(int key) {
        LocalChordNode currentNode = this;
        while (!inRange(key, currentNode.getId(), currentNode.getSuccessor().getId())) {
            currentNode = currentNode.closestPrecedingFinger(key);
        }
        return currentNode;
    }

    private LocalChordNode findSuccessor(int key) {
        return findPredecessor(key).getSuccessor();
    }

    private void updateFingers(ChordNode s, int i) {
        // might not be necessary
        if(s.getId() >= getId() && s.getId() < getStartOfFingerInterval(i)) {
            fingerTable.set(i, s);
            // we don't need to update the remote nodes
        }
    }

    private void fixFingers() {
        Random rand = new Random(); // uniform pick
        int rand_int = rand.nextInt(fingerTable.size()-1);
        rand_int += 1;
        int i = getStartOfFingerInterval(rand_int);
        fingerTable.set(rand_int, findSuccessor(i));
    }

    public static LocalChordNode create(URI uri, int id, int chordSize) {
        LocalChordNode newNode = LocalChordNode.builder()
                .uri(uri)
                .id(id)
                .keySpaceSize(chordSize)
                .predecessor(null)
                // .successor(null)
                .build();
        // TODO: Implement timers for periodical action.

        int temp = chordSize; // will always be a power of 2
        int fingerTableSize = 0;
        while((temp >>= 1) > 0) fingerTableSize++;
        newNode.fingerTable = new ArrayList<>(fingerTableSize);
        newNode.fingerTable.set(0, newNode);
        for(int i=1; i<fingerTableSize; i++){
            newNode.fingerTable.set(i, null);
        }

        return newNode;
    }
}
