package uw.cse.cse561.chord_java_REST.Test;

import jakarta.ws.rs.core.UriBuilder;
import uw.cse.cse561.chord_java_REST.ChordApplication;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;

import java.net.URI;
import java.util.*;

public class TestStabilize {

    // Give a list of node ID, return the ground truth sucessor
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

    // Return the expected IDs in the finger table when the cluster stabilize
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

    // Check if the cluster is stabilized
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

    // Vary the number of nodes, test the number of iterations needed to stabilize finger table
    public static void experiment1() {
        int[] nNodesList = {50, 100, 150, 200, 250, 300, 350, 400};
//        int[] nNodesList = {10};
        for (int iter = 0; iter < 100; iter++) {
            System.out.print(iter + ",");
            for (int nNodes : nNodesList) {
                System.out.print(testStabilize(65536, nNodes, true));
                System.out.print(",");
            }
            System.out.println();
        }
    }

    // Vary the number of key space size, test the number of iterations needed to stabilize finger table
    public static void experiment2() {
        int[] keySizeList = {(1<<9), (1<<12), (1<<15), (1<<18), (1<<21), (1<<24), (1<<27), (1<<30)};
        for (int iter = 0; iter < 100; iter++) {
            System.out.print(iter + ",");
            for (int keySize : keySizeList) {
                System.out.print(testStabilize(keySize, 200, true));
                System.out.print(",");
            }
            System.out.println();
        }
    }

    // Vary the number of nodes,
    // record the message count until finger table stabilizes
    public static void experiment3() {
        int[] nNodesList = {50, 100, 150, 200, 250, 300, 350, 400};
        for (int iter = 0; iter < 100; iter++) {
            System.out.print(iter + ",");
            for (int nNodes : nNodesList) {
                System.out.print(testStabilizeMessageCount(65536, nNodes, true));
                System.out.print(",");
            }
            System.out.println();
        }
    }

    // Vary the number of nodes, wait until finger table stabilizes
    // record the message count for findSuccessor call
    public static void experiment4() {
        int[] nNodesList = {10, 20, 30, 40, 50, 60, 70, 80};
        for (int iter = 0; iter < 1000; iter++) {
            System.out.print(iter + ",");
            for (int nNodes : nNodesList) {
                System.out.print(testfindSucessorMessageCount(65536, nNodes, 1));
                System.out.print(",");
            }
            System.out.println();
        }
    }

    public static double testStabilize(final int keySpaceSize, final int nNodes, final int nRepeat, boolean aggresiveJoin) {
        long total = 0;
        int count = 0;
        for (int i = 0; i < 10; i++) {
            total += testStabilize(65536, 1000, aggresiveJoin);
            count++;
        }
        return (double) total / count;
    }

    private static Map<Integer, ChordNode> buildChordNodes(final int keySpaceSize, final int nNodes) {
        return buildChordNodes(keySpaceSize, nNodes, false);
    }

    private static Map<Integer, ChordNode> buildChordNodes(final int keySpaceSize, final int nNodes, boolean aggressiveJoin) {
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
                if (aggressiveJoin) {
                    node.aggressiveJoin(application.getNode(otherID.get()));
                } else {
                    node.join(application.getNode(otherID.get()));
                }
            } else {
                if (aggressiveJoin) {
                    node.aggressiveJoin(null);
                } else {
                    node.join(null);
                }
            }

            chordNodes.put(id, node);
            ids.add(id);
        }
        return chordNodes;
    }

    public static int testStabilize(final int keySpaceSize, final int nNodes, boolean aggressiveJoin) {

        Map<Integer, ChordNode> chordNodes = buildChordNodes(keySpaceSize, nNodes, aggressiveJoin);

        List<ChordNode> nodesList = new ArrayList<>(chordNodes.values());
        int[][] groundTruth = getGoundTruth(nodesList, keySpaceSize);

        // Experiment
        for (int iter = 0; ; iter++) {

            if (isStable(nodesList, groundTruth)) {
                return iter;
            }

            if (iter > 10000) {
                throw new RuntimeException("finger tables are not converging!");
            }

            for (ChordNode node : chordNodes.values()) {
                node.stabilize();
                node.fixFingers();
            }
        }
    }

    public static void waitForConvergance(Map<Integer, ChordNode> chordNodes, int keySpaceSize) {
        List<ChordNode> nodesList = new ArrayList<>(chordNodes.values());
        int[][] groundTruth = getGoundTruth(nodesList, keySpaceSize);

        // Wait for stabilize
        while (!isStable(nodesList, groundTruth)) {
            for (ChordNode node : chordNodes.values()) {
                node.stabilize();
                node.fixFingers();
            }
        }
    }

    public static int testStabilizeMessageCount(final int keySpaceSize, final int nNodes, boolean aggressiveJoin) {
        // Create nodes
        URI listenURI = UriBuilder.fromPath("/").scheme("http").host("0.0.0.0").port(6666).build();

        Map<Integer, ChordNode> chordNodes = buildChordNodes(keySpaceSize, nNodes, aggressiveJoin);
        waitForConvergance(chordNodes, keySpaceSize);

        // Return message count
        return chordNodes.values().stream().map(ChordNode::getMessageCount).reduce(Integer::sum).orElse(0);
    }

    public static double testfindSucessorMessageCount(final int keySpaceSize, final int nNodes, final int nKeys) {
        // Create nodes
        URI listenURI = UriBuilder.fromPath("/").scheme("http").host("0.0.0.0").port(6666).build();

        Map<Integer, ChordNode> chordNodes = buildChordNodes(keySpaceSize, nNodes);
        List<ChordNode> nodesList = new ArrayList<>(chordNodes.values());
        waitForConvergance(chordNodes, keySpaceSize);

        // Test keys
        int startingCount = chordNodes.values().stream().map(ChordNode::getMessageCount).reduce(Integer::sum).orElse(0);
        Random random = new Random();
        for (int i = 0; i < nKeys; i++) {
            nodesList.get(random.nextInt(nodesList.size())).findSuccessor(null, random.nextInt(keySpaceSize));
        }
        int endingCount = chordNodes.values().stream().map(ChordNode::getMessageCount).reduce(Integer::sum).orElse(0);

        // Return message count
        return (double)(endingCount - startingCount) / nKeys;
    }
}
