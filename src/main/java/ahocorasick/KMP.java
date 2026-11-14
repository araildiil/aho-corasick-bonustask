package ahocorasick;

import java.util.ArrayList;
import java.util.List;

public final class KMP {
    private KMP() {
    }

    public static int[] prefixFunction(String p) {
        int[] pi = new int[p.length()];
        int k = 0;
        for (int i = 1; i < p.length(); i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) {
                k = pi[k - 1];
            }
            if (p.charAt(i) == p.charAt(k)) {
                k++;
            }
            pi[i] = k;
        }
        return pi;
    }

    public static List<Integer> search(String text, String p) {
        List<Integer> res = new ArrayList<>();
        int[] pi = prefixFunction(p);
        int k = 0;
        for (int i = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != p.charAt(k)) {
                k = pi[k - 1];
            }
            if (text.charAt(i) == p.charAt(k)) {
                k++;
            }
            if (k == p.length()) {
                res.add(i - p.length() + 1);
                k = pi[k - 1];
            }
        }
        return res;
    }

    public static long count(String text, String p) {
        int[] pi = prefixFunction(p);
        long count = 0;
        int k = 0;
        for (int i = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != p.charAt(k)) {
                k = pi[k - 1];
            }
            if (text.charAt(i) == p.charAt(k)) {
                k++;
            }
            if (k == p.length()) {
                count++;
                k = pi[k - 1];
            }
        }
        return count;
    }

    public static long countAll(String text, List<String> patterns) {
        long total = 0;
        for (String p : patterns) {
            total += count(text, p);
        }
        return total;
    }
}
