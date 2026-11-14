package ahocorasick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AhoCorasick {
    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        int patternIndex = -1;
        final int depth;

        Node(int depth) {
            this.depth = depth;
        }
    }

    private final Node root = new Node(0);
    private final List<String> patterns;
    private int nodeCount = 1;

    public AhoCorasick(List<String> patterns) {
        this.patterns = new ArrayList<>(patterns);
        buildTrie();
    }

    private void buildTrie() {
        for (int i = 0; i < patterns.size(); i++) {
            String p = patterns.get(i);
            if (p.isEmpty()) {
                throw new IllegalArgumentException("Empty patterns are not allowed");
            }
            Node cur = root;
            for (int k = 0; k < p.length(); k++) {
                char c = p.charAt(k);
                Node next = cur.children.get(c);
                if (next == null) {
                    next = new Node(cur.depth + 1);
                    cur.children.put(c, next);
                    nodeCount++;
                }
                cur = next;
            }

            if (cur.patternIndex == -1) {
                cur.patternIndex = i;
            }
        }
    }

    public int getNodeCount() {
        return nodeCount;
    }
}
