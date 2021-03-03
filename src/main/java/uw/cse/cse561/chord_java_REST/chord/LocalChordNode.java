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
    public static final long STARTUP_DELAY_MILLI = 1000;

    public static final long STABILIZE_TIMER_MILLI = 200;

    public static final long FIX_FINGER_TIMER_MILLI = 200;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private ChordNode predecessor;

    private ChordNode getSuccessor() {
        return fingerTable.get(0);
    }

    @JsonIgnore
    @Getter
    @EqualsAndHashCode.Exclude
    // Use this value to limit chord size for testing.
    // n in (n1, n2) means
    // n in (n1, n1 =< n2 ? n2 : chordSize - 1)
    // or n in (n1 =< n2 ? n1 : 0, n2)
    private final int keySpaceSize;

    // size = log(ChordSize-1)
    // contains successor nodes to 2^i jumps
    // interval is [2^i , 2^(i+1))
    // 1 2 4 8
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private List<ChordNode> fingerTable;

    @Override
    public ChordNode findSuccessor(int id) {
        ChordNode successor = getSuccessor();
        if (within(id, getId(), successor.getId(), false)) {
            return successor;
        } else {
            ChordNode closestPrecedingNode = closestPrecedingNode(id);
            return closestPrecedingNode.findSuccessor(id);
        }
    }

    @Override
    public void notify(ChordNode n_other) {
        if (predecessor == null || within(n_other.getId(), predecessor.getId(), getId(), false)) {
            // do not need classify since notify is called by stabilize
            predecessor = n_other;
        }
    }

    @Override
    protected boolean isAlive() {
        return true;
    }

    public void join(ChordNode n_other) {
        if (n_other == null) {
            predecessor = this;
            for (int i = 0; i < fingerTable.size(); i++)
                fingerTable.set(i, this);
        } else {
            predecessor = null;
            ChordNode successor = n_other.findSuccessor(getId());
            assert successor != null;

            for (int i = 0; i < fingerTable.size(); i++)
                fingerTable.set(i, successor);
        }
    }

    private ChordNode closestPrecedingNode(int id) {
        synchronized (fingerTable) {
            ListIterator<ChordNode> iter = fingerTable.listIterator(fingerTable.size());
            while (iter.hasPrevious()) {
                ChordNode prev = iter.previous();
                if (within(prev.getId(), getId(), id, false)) {
                    return prev;
                }
            }
        }

        return this;
    }

    private void stabilize() {
        ChordNode x = getSuccessor().getPredecessor();
        if (within(x.getId(), getId(), getSuccessor().getId(), false)) {
            fingerTable.set(0, x);
        }
        x.notify(this);
    }

    private int getStartOfFingerInterval(int i) {
        // (1 << i) is equivalent to (int) Math.pow(2, i - 1)
        return (getId() + (1 << i)) % keySpaceSize;
    }

//    private void updateFingers(ChordNode s, int i) {
//        // might not be necessary
//        if (s.getId() >= getId() && s.getId() < getStartOfFingerInterval(i)) {
//            fingerTable.set(i, s);
//            // we don't need to update the remote nodes
//        }
//    }

    private void fixFingers() {
        Random rand = new Random(); // uniform pick
        int rand_int = rand.nextInt(fingerTable.size());
        int i = getStartOfFingerInterval(rand_int);
        ChordNode temp = findSuccessor(i);
        if (temp != null) {
            fingerTable.set(rand_int, temp);
        }
    }

    private void checkPredecessor() {
        if (predecessor != null && !predecessor.isAlive()) {
            predecessor = null;
        }
    }

    public static LocalChordNode create(URI uri, int id, int keySpaceSize) {
        LocalChordNode newNode = LocalChordNode.builder()
                .uri(uri)
                .id(id)
                .keySpaceSize(keySpaceSize)
                .predecessor(null)
                .build();

        int fingerTableSize = 1;
        while ((1 << fingerTableSize) < keySpaceSize / 2) fingerTableSize++;

        // initialize size array
        newNode.fingerTable = Collections.synchronizedList(
                new ArrayList<ChordNode>(Collections.nCopies(fingerTableSize, newNode)));

        Timer timer = new Timer("Node task timer", true);

        timer.scheduleAtFixedRate(wrap(()->newNode.stabilize()), STARTUP_DELAY_MILLI, STABILIZE_TIMER_MILLI);
        timer.scheduleAtFixedRate(wrap(()->newNode.fixFingers()), STARTUP_DELAY_MILLI, STABILIZE_TIMER_MILLI);
        return newNode;
    }

    private boolean within(int searchId, int startId, int endId, boolean closeEnd) {
        if (searchId < 0 || searchId >= keySpaceSize ||
                startId < 0 || startId >= keySpaceSize ||
                endId < 0 || endId >= keySpaceSize) {
            throw new IllegalArgumentException();
        }

        assert startId != endId;

        if (closeEnd && searchId == endId) {
            return true;
        }

        if (startId < endId) {
            return searchId >= startId && searchId < endId;
        } else {
            return searchId >= startId || searchId < endId;
        }
    }
//
//    private ChordNode classify(ChordNode node) {
//        if (node.getId() == getId()) {
//            return this;
//        } else {
//            if (node instanceof RemoteChordNode) {
//                return node;
//            } else  {
//                return RemoteChordNode.builder().chordNode(node).build();
//            }
//        }
//    }

    private static TimerTask wrap(Runnable r) {
        return new TimerTask() {

            @Override
            public void run() {
                r.run();
            }
        };
    }
}
