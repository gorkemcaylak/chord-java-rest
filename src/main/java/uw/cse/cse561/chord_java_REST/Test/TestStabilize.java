package uw.cse.cse561.chord_java_REST.Test;

import jakarta.ws.rs.core.UriBuilder;
import uw.cse.cse561.chord_java_REST.ChordApplication;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;

import java.net.URI;
import java.util.*;

public class TestStabilize {

    private static int successor(int id, List<Integer> sortedIDs) {
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

    private static int[][] getGoundTruth(List<ChordNode> nodes, final int keySpaceSize) {
        final int fingerTableSize = nodes.get(0).getFingerTable().size();

        List<Integer> sortedIDs = new ArrayList<>();
        for (ChordNode node : nodes) {
            sortedIDs.add(node.getId());
        }
        sortedIDs.sort(Integer::compare);

        int[][] groundTruth = new int[sortedIDs.size()][fingerTableSize];
        for (int nodeIndex = 0; nodeIndex < sortedIDs.size(); nodeIndex++) {
            for (int fingerIndex = 0; fingerIndex <  fingerTableSize; fingerIndex++) {
                groundTruth[nodeIndex][fingerIndex] =
                        successor((nodes.get(nodeIndex).getId() + (1 << fingerIndex)) % keySpaceSize, sortedIDs);
            }
        }
        return groundTruth;
    }

    private static boolean isStable(List<ChordNode> nodes, int[][] groundTruth) {
        for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
            List<ChordNode> fingerTable = nodes.get(nodeIndex).getFingerTable();
            for (int fingerIndex = 0; fingerIndex <  groundTruth[0].length; fingerIndex++) {
                int expected = groundTruth[nodeIndex][fingerIndex];
                ChordNode actual = fingerTable.get(fingerIndex);
                if (actual == null || expected != actual.getId())
                    return false;
            }
        }
        return true;
    }

    public static void experiment1() {
        int[] nNodesList = {50, 100, 150, 200, 250, 300, 350, 400};
        for (int iter = 0; iter < 100; iter++) {
            System.out.print(iter + ",");
            for (int nNodes : nNodesList) {
                System.out.print(testStabilize(1073741824, nNodes));
                System.out.print(",");
            }
            System.out.println();
        }
    }

    public static void experiment2() {
        int[] keySizeList = {(1<<9), (1<<12), (1<<15), (1<<18), (1<<21), (1<<24), (1<<27), (1<<30)};
        for (int iter = 0; iter < 100; iter++) {
            System.out.print(iter + ",");
            for (int keySize : keySizeList) {
                System.out.print(testStabilize(keySize, 200));
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

        List<ChordNode> nodesList = new ArrayList<>(chordNodes.values());
        int[][] groundTruth = getGoundTruth(nodesList, keySpaceSize);

        // Experiment
        for (int iter = 0; ; iter++) {

            if (isStable(nodesList, groundTruth)) {
                return iter;
            }

            for (ChordNode node : chordNodes.values()) {
                node.stabilize();
                node.fixFingers();
            }
        }
    }
}
