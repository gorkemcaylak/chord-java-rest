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
    @Setter(AccessLevel.PROTECTED)
    private ChordNode predecessor;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final boolean automaticStabilize;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final MessageCounter messageCounter = new MessageCounter();

    @Override
    public int getMessageCount() {
        return messageCounter.countAll();
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
    public ChordNode getSuccessor(ChordNode caller) {
        messageCounter.recordMessage("getSuccessor", caller, this);
        return fingerTable.get(0);
    }

    @Override
    public ChordNode getPredecessor(ChordNode caller) {
        messageCounter.recordMessage("getPredecessor", caller, this);
        return predecessor;
    }

    @Override
    public void setPredecessor(ChordNode caller, ChordNode predecessor) {
        messageCounter.recordMessage("setPredecessor", caller, this);
        this.predecessor = predecessor;
    }

    @Override
    public ChordNode findSuccessor(ChordNode caller, int id) {
        messageCounter.recordMessage("findSuccessor", caller, this);

        return findPredecessor(this, id).getSuccessor(this);
    }

    @Override
    public ChordNode findPredecessor(ChordNode caller, int id) {
        messageCounter.recordMessage("findPredecessor", caller, this);
        ChordNode result = this;
        while (!within(id,
                result.getId(), false,
                result.getSuccessor(this).getId(), true)) {
            result = result.closestPrecedingNode(this, id);
        }
        return result;
    }

    @Override
    public ChordNode closestPrecedingNode(ChordNode caller, int id) {
        messageCounter.recordMessage("closestPrecedingNode", caller, this);
        synchronized (fingerTable) {
            ListIterator<ChordNode> iter = fingerTable.listIterator(fingerTable.size());
            while (iter.hasPrevious()) {
                ChordNode finger = iter.previous();
                if (finger != null && within(finger.getId(), getId(), false, id, false)) {
                    return finger;
                }
            }
        }
        return this;
    }

    @Override
    public void notify(ChordNode caller, ChordNode n_other) {
        messageCounter.recordMessage("notify", caller, this);
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
    protected boolean isAlive(ChordNode caller) {
        messageCounter.recordMessage("isAlive", caller, this);
        return true;
    }

    public void join(ChordNode n_other) {
        predecessor = null;

        if (n_other == null) {
            fingerTable.set(0, this);
            return;
        }

        ChordNode successor = n_other.findSuccessor(this, getId());
        assert successor != null;

        fingerTable.set(0, successor);
    }

    public void aggressiveJoin(ChordNode n_other) {
        if (n_other == null) {
            predecessor = this;
            for (int i = 0; i < fingerTable.size(); i++) {
                fingerTable.set(i, this);
            }
            return;
        }

        ChordNode successor = n_other.findSuccessor(this, getId());
        assert successor != null;

        fingerTable.set(0, successor);

        predecessor = successor.getPredecessor(this);
        successor.setPredecessor(this, this);

        for (int i = 1; i < fingerTable.size(); i++) {
            if (within(getStartOfFingerInterval(i),
                    getId(), true,
                    fingerTable.get(i - 1).getId(), false)) {
                fingerTable.set(i, fingerTable.get(i - 1));
            } else {
                fingerTable.set(i, n_other.findSuccessor(this, getStartOfFingerInterval(i)));
            }
        }

        for (int i = 0; i < fingerTable.size(); i++) {
            n_other.findPredecessor(this, (getId() - (1 << i) + keySpaceSize) % keySpaceSize)
                    .updateFingerTable(this, this, i);
        }
    }

    public void updateFingerTable(ChordNode caller, ChordNode node, int fingerIndex) {
        messageCounter.recordMessage("updateFingerTable", node, this);
        if (within(node.getId(),
                getId(), false,
                fingerTable.get(fingerIndex).getId(), false)) {
            fingerTable.set(fingerIndex, node);
            predecessor.updateFingerTable(this, node, fingerIndex);
        }
    }

    public void stabilize() {
        ChordNode successor = getSuccessor(this);
        ChordNode x = successor.getPredecessor(this);
        if (x != null) {
            if (within(x.getId(), getId(), false, getSuccessor(this).getId(), false)) {
                fingerTable.set(0, x);
            }
        } else if (!successor.isAlive(this)) {
            throw new RuntimeException();
        }
        getSuccessor(this).notify(this, this);
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
        ChordNode temp = findSuccessor(this, i);
        if (temp != null) {
            fingerTable.set(rand_int, temp);
        }

        if (automaticStabilize) {
            timer.schedule(wrap(()->fixFingers()), FIX_FINGER_TIMER_MILLI);
        }
    }

    private void checkPredecessor() {
        if (predecessor != null && !predecessor.isAlive(this)) {
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
