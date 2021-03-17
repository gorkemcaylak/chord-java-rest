package uw.cse.cse561.chord_java_REST.Test;

import jakarta.ws.rs.core.UriBuilder;
import uw.cse.cse561.chord_java_REST.ChordApplication;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;

import java.net.URI;
import java.util.*;

public class TestStabilize {

    public static int successor(int id, List<Integer> sortedIDs) {
        if (sortedIDs.size() <= 1) {
            return sortedIDs.get(0);
        }

        for (int i = 0; i + 1 < sortedIDs.size(); i++) {
            int a = sortedIDs.get(i);
            int b = sortedIDs.get(i + 1);
            if (id > a && id <= b) {
                return b;
            }
        }
        return sortedIDs.get(0);
    }

    public static boolean isStable(ChordNode node, Set<Integer> ids, int keySpaceSize) {
        List<Integer> sortedIDs = new ArrayList<>(ids);
        sortedIDs.sort(Integer::compare);

        List<ChordNode> fingerTable = node.getFingerTable();
        for (int i = 0; i < fingerTable.size(); i++) {
            ChordNode finger = fingerTable.get(i);
            int expected = successor((node.getId() + (1 << i)) % keySpaceSize, sortedIDs);
            int actual = (finger == null? -1 : finger.getId());
            if (expected != actual) {
                return false;
            }
        }
        return true;
    }

    public static void main() {
        int[] nNodesList = {50, 100, 150, 200, 250, 300, 350, 400};
        for (int iter = 0; iter < 100; iter++) {
            System.out.print(iter + ",");
            for (int nNodes : nNodesList) {
                System.out.print(testStabilize(65536, nNodes));
                System.out.print(",");
            }
            System.out.println();
        }
    }

    public static double testStabilize(final int keySpaceSize, final int nNodes, final int nRepeat) {
        long total = 0;
        int count = 0;
        for (int i = 0; i < 10; i++) {
            total += testStabilize(65536, 1000);
            count++;
        }
        return (double) total / count;
    }

    public static int testStabilize(final int keySpaceSize, final int nNodes) {
        // Create nodes
        URI listenURI = UriBuilder.fromPath("/").scheme("http").host("0.0.0.0").port(6666).build();

        Map<Integer, ChordNode> chordNodes = new HashMap<>();
        ChordApplication application = ChordApplication.builder()
                .chordNodes(chordNodes)
                .build();

        Set<Integer> ids = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < nNodes; i++) {
            int id;
            do {
                id = random.nextInt(keySpaceSize);
            } while (ids.contains(id));

            LocalChordNode node = LocalChordNode.create(listenURI, id, keySpaceSize, false);

            Optional<Integer> otherID = ids.stream().findAny();

            if (otherID.isPresent()) {
                node.join(application.getNode(otherID.get()));
            } else {
                node.join(null);
            }

            chordNodes.put(id, node);
            ids.add(id);
        }

        // Experiment
        for (int iter = 0; ; iter++) {
            boolean stable = true;
            for (ChordNode node : chordNodes.values()) {
                if (!isStable(node, ids, keySpaceSize)) {
                    stable = false;
                    break;
                }
            }

            for (ChordNode node : chordNodes.values()) {
                node.stabilize();
                node.fixFingers();
            }

            if (stable) {
                return iter;
            }
        }
    }
}
