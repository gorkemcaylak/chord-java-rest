package uw.cse.cse561.chord_java_REST.chord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uw.cse.cse561.chord_java_REST.Main;
import uw.cse.cse561.chord_java_REST.client.ChordNodeModel;

import java.net.URI;
import java.util.*;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LocalChordNode extends ChordNode {
    public static final long STARTUP_DELAY_MILLI = 1000;

    public static final long STABILIZE_TIMER_MILLI = 200;

    public static final long FIX_FINGER_TIMER_MILLI = 200;

    public static final long CHECK_PREDECESSOR_MILLI = 200;


    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Timer timer;

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
    private int chordSize;

    // size = log(ChordSize-1)
    // contains successor nodes to 2^i jumps
    // interval is [2^i , 2^(i+1))
    // 1 2 4 8
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter
    private List<ChordNode> fingerTable;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter
    private Random rand;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Getter
    private boolean alive;

    @Override
    public ChordNodeModel findSuccessor(int id, List<Integer> visited) {
        if (!alive || visited.contains(getId())) {
            return null;
        }
        visited = new ArrayList<>(visited);
        visited.add(getId());
        ChordNode successor = getSuccessor();
        if (id == getId()) {
            return this.toChordNodeModel(visited);
        } else if (within(id, getId(), successor.getId(), true)) {
            return successor.toChordNodeModel(visited);
        } else {
            ChordNode closestPrecedingNode = closestPrecedingNode(id);
            if (closestPrecedingNode.getId() != getId()) {
                return closestPrecedingNode.findSuccessor(id, visited);
            } else {
                return successor.findSuccessor(id, visited);
            }
        }
    }

    @Override
    public ChordNodeModel findSuccessor(int id) {
        if (!alive) {
            return null;
        }
        return findSuccessor(id, Collections.EMPTY_LIST);
    }

    @Override
    public synchronized void notify(ChordNode n_other) {
        if (!alive) {
            return;
        }

        if (predecessor == null || within(n_other.getId(), predecessor.getId(), getId(), false)) {
            // do not need classify since notify is called by stabilize
            predecessor = n_other;
        }
    }

    @Override
    protected boolean isAlive() {
        return alive;
    }

    public boolean join(ChordNode n_other) {
        predecessor = null;
        ChordNodeModel temp = n_other.findSuccessor(getId());
        if (temp != null) {
            fingerTable.set(0, classify(temp.toChordNode()));
            return true;
        }

        return false;
    }

    private ChordNode closestPrecedingNode(int id) {
        synchronized (fingerTable) {
            ListIterator<ChordNode> iter = fingerTable.listIterator(fingerTable.size());
            while (iter.hasPrevious()) {
                ChordNode prev = iter.previous();
                if (prev != null && within(prev.getId(), getId(), id, false)) {
                    return prev;
                }
            }
        }

        return this;
    }

    private void stabilize() {
        ChordNode successor = getSuccessor();
        ChordNode x = successor.getPredecessor();
        if (x != null) {
            x = classify(x);
            if (within(x.getId(), getId(), successor.getId(), false)) {
                fingerTable.set(0, x);
            }
        } else if (!successor.isAlive()) {
            fingerTable.set(0, this);
        }
        getSuccessor().notify(this);
        timer.schedule(wrap(()->stabilize()), STABILIZE_TIMER_MILLI);
    }

    private int getStartOfFingerInterval(int i) {
        return (getId() + (int) Math.pow(2, i)) % chordSize;
    }

    private void fixFingers() {
        int rand_int = rand.nextInt(fingerTable.size());
        int i = getStartOfFingerInterval(rand_int);
        ChordNodeModel temp = findSuccessor(i);
        assert (rand_int != 0);
        if (temp != null) {
            fingerTable.set(rand_int, classify(temp.toChordNode()));
        }
        timer.schedule(wrap(()->fixFingers()), FIX_FINGER_TIMER_MILLI);
    }

    private void checkPredecessor() {
        if (predecessor != null && !predecessor.isAlive()) {
            predecessor = null;
        }

        timer.schedule(wrap(()->checkPredecessor()), CHECK_PREDECESSOR_MILLI);
    }

    public static LocalChordNode create(URI uri, int id, int chordSize) {
        LocalChordNode newNode = LocalChordNode.builder()
                .uri(uri)
                .id(id)
                .chordSize(chordSize)
                .predecessor(null)
                .timer(new Timer("Node task timer", true))
                .build();
        // TODO: Implement timers for periodical action.

        int temp = chordSize; // will always be a power of 2
        int fingerTableSize = 0;
        while ((temp >>= 1) > 0) fingerTableSize++;

        // initialize size array
        newNode.fingerTable = Collections.synchronizedList(
                new ArrayList<ChordNode>(Collections.nCopies(fingerTableSize, null)));

        newNode.rand = new Random();
        newNode.alive = true;

        newNode.fingerTable.set(0, newNode);

        newNode.timer.schedule(wrap(()->newNode.stabilize()), STARTUP_DELAY_MILLI);
        newNode.timer.schedule(wrap(()->newNode.fixFingers()), STARTUP_DELAY_MILLI);
        newNode.timer.schedule(wrap(()->newNode.checkPredecessor()), STARTUP_DELAY_MILLI);
        return newNode;
    }

    private boolean within(int searchId, int startId, int endId, boolean closeEnd) {
        if (searchId < 0 || searchId >= chordSize ||
                startId < 0 || startId >= chordSize ||
                endId < 0 || endId >= chordSize) {
            throw new IllegalArgumentException();
        }

        if (searchId == endId) {
            return closeEnd;
        }

        if (startId < endId) {
            return searchId > startId && searchId < endId;
        } else if (startId > endId) {
            return searchId > startId || searchId < endId;
        }

        return true;
    }

    private ChordNode classify(ChordNode node) {
        if (Main.MULTI) {
            return node;
        } else {
            if (node.getId() == getId()) {
                return this;
            } else {
                if (node instanceof RemoteChordNode) {
                    return node;
                } else {
                    return RemoteChordNode.builder().chordNode(node).build();
                }
            }
        }
    }

    public synchronized void shutdownNode() {
        timer.cancel();
        alive = false;
        predecessor = null;
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
