package ahocorasick;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class AhoCorasick {
    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        Node fail;
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
        buildFailureLinks();
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

    private void buildFailureLinks() {
        Queue<Node> queue = new ArrayDeque<>();
        root.fail = root;

        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            for (Map.Entry<Character, Node> e : u.children.entrySet()) {
                char c = e.getKey();
                Node v = e.getValue();

                Node f = u.fail;
                while (f != root && !f.children.containsKey(c)) {
                    f = f.fail;
                }
                Node target = f.children.get(c);
                v.fail = (target != null && target != v) ? target : root;

                queue.add(v);
            }
        }
    }

    public int getNodeCount() {
        return nodeCount;
    }
}
