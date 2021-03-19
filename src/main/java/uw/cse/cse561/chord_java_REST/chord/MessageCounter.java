package uw.cse.cse561.chord_java_REST.chord;

import java.util.HashMap;
import java.util.Map;

public class MessageCounter {
    private final Map<String, Integer> counter = new HashMap<>();

    public synchronized void recordMessage(String methodName, ChordNode source, ChordNode destination) {
        if (source == null || source.getId() != destination.getId()) {
            counter.put(methodName, counter.getOrDefault(methodName, 0) + 1);
        }
    }

    public int countAll() {
        return counter.values().stream().reduce(Integer::sum).orElse(0);
    }
}
