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
    private Timer timer;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private ChordNode predecessor;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final boolean automaticStabilize;

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
        if (id == getId()) {
            return this;
        }
        if (within(id, getId(), false, successor.getId(), true)) {
            return successor;
        } else {
            ChordNode closestPrecedingNode = closestPrecedingNode(id);
            if (closestPrecedingNode.getId() != getId()) {
                return closestPrecedingNode.findSuccessor(id);
            } else {
                return successor.findSuccessor(id);
            }
        }
    }

    private ChordNode closestPrecedingNode(int id) {
        synchronized (fingerTable) {
            ListIterator<ChordNode> iter = fingerTable.listIterator(fingerTable.size());
            while (iter.hasPrevious()) {
                ChordNode finger = iter.previous();
                if (finger != null && within(finger.getId(), getId(), false, id, false)) {
                    return finger;
                }
            }
        }

        // TODO: we shouldn't reach this line?

        return this;
    }

    @Override
    public void notify(ChordNode n_other) {
        if (predecessor == null || within(n_other.getId(), predecessor.getId(), false, getId(), false)) {
            // do not need classify since notify is called by stabilize
            predecessor = n_other;
        }
    }

    @Override
    public List<ChordNode> getFingerTable() {
        return Collections.unmodifiableList(fingerTable);
    }

    @Override
    protected boolean isAlive() {
        return true;
    }

    public void join(ChordNode n_other) {
        predecessor = null;

        if (n_other == null) {
            fingerTable.set(0, this);
            return;
        }

        ChordNode successor = n_other.findSuccessor(getId());
        assert successor != null;

        fingerTable.set(0, successor);
    }

    public void stabilize() {
        ChordNode successor = getSuccessor();
        ChordNode x = successor.getPredecessor();
        if (x != null) {
            if (within(x.getId(), getId(), false, getSuccessor().getId(), false)) {
                fingerTable.set(0, x);
            }
        } else if (!successor.isAlive()) {
            throw new RuntimeException();
            // TODO: why?
//            fingerTable.set(0, this);
        }
        getSuccessor().notify(this);
        if (automaticStabilize) {
            timer.schedule(wrap(()->stabilize()), STABILIZE_TIMER_MILLI);
        }
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

    private final Random rand = new Random(); // uniform pick
    public void fixFingers() {
        // TODO: Add 1?
        int rand_int = rand.nextInt(fingerTable.size() - 1) + 1;
        int i = getStartOfFingerInterval(rand_int);
        ChordNode temp = findSuccessor(i);
        if (temp != null) {
            fingerTable.set(rand_int, temp);
        }

        if (automaticStabilize) {
            timer.schedule(wrap(()->fixFingers()), FIX_FINGER_TIMER_MILLI);
        }
    }

    private void checkPredecessor() {
        if (predecessor != null && !predecessor.isAlive()) {
            predecessor = null;
        }
    }

    public static LocalChordNode create(URI uri, int id, int keySpaceSize, boolean automaticStabilize) {
        LocalChordNode newNode = LocalChordNode.builder()
                .uri(uri)
                .id(id)
                .keySpaceSize(keySpaceSize)
                .predecessor(null)
                .automaticStabilize(automaticStabilize)
                .build();

        int fingerTableSize = 1;
        while ((1 << fingerTableSize) < keySpaceSize / 2) fingerTableSize++;

        // initialize size array
        newNode.fingerTable = Collections.synchronizedList(
                new ArrayList<ChordNode>(Collections.nCopies(fingerTableSize, null)));
        newNode.fingerTable.set(0, newNode);

        if (automaticStabilize) {
            newNode.timer = new Timer("Node task timer", true);
            newNode.timer.schedule(wrap(() -> newNode.stabilize()), STARTUP_DELAY_MILLI);
            newNode.timer.schedule(wrap(() -> newNode.fixFingers()), STARTUP_DELAY_MILLI);
            newNode.timer.schedule(wrap(() -> newNode.checkPredecessor()), STARTUP_DELAY_MILLI);
        }
        return newNode;
    }

    private boolean within(int searchId, int startId, boolean closedStart, int endId, boolean closedEnd) {
        if (searchId < 0 || searchId >= keySpaceSize ||
                startId < 0 || startId >= keySpaceSize ||
                endId < 0 || endId >= keySpaceSize) {
            throw new IllegalArgumentException();
        }

        boolean matchStart = closedStart? (searchId >= startId) : (searchId > startId);
        boolean matchEnd = closedEnd? (searchId <= endId) : (searchId < endId);

        if (startId < endId) {
            return matchStart && matchEnd;
        } else {
            return matchStart || matchEnd;
        }
    }

    private static TimerTask wrap(Runnable r) {
        return new TimerTask() {

            @Override
            public void run() {
                r.run();
            }
        };
    }
}
