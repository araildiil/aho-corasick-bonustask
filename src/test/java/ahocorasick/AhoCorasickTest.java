package ahocorasick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AhoCorasickTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        classicExample();
        overlappingPatterns();
        patternInsidePattern();
        noMatches();
        singleCharacterPatterns();
        duplicatePatterns();
        randomizedAgainstBruteForce();

        System.out.printf("%nPassed: %d, Failed: %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void classicExample() {
        List<String> pats = List.of("he", "she", "his", "hers");
        String text = "ushers";
        List<Match> m = new AhoCorasick(pats).search(text);
        for (Match x : m) System.out.println("  ushers -> " + x);

        check("classic 'ushers'", sorted(m), sorted(bruteForce(text, pats)));
        check("classic count = 3", m.size() == 3);
    }

    static void overlappingPatterns() {
        List<String> pats = List.of("aa", "aaa");
        String text = "aaaaa";
        check("overlapping 'aaaaa'", sorted(new AhoCorasick(pats).search(text)), sorted(bruteForce(text, pats)));
    }

    static void patternInsidePattern() {
        List<String> pats = List.of("abc", "bc", "c", "b");
        String text = "xabcabcx";
        check("nested suffix patterns", sorted(new AhoCorasick(pats).search(text)), sorted(bruteForce(text, pats)));
    }

    static void noMatches() {
        List<Match> m = new AhoCorasick(List.of("xyz", "qq")).search("abcdefgh");
        check("no matches", m.isEmpty());
    }

    static void singleCharacterPatterns() {
        List<String> pats = List.of("a", "b");
        String text = "abba";
        check("single chars", sorted(new AhoCorasick(pats).search(text)), sorted(bruteForce(text, pats)));
    }

    static void duplicatePatterns() {
        List<String> pats = List.of("ab", "ab", "b");
        List<Match> m = new AhoCorasick(pats).search("abab");

        check("duplicates", m.size() == 4);
    }

    static void randomizedAgainstBruteForce() {
        Random rnd = new Random(123);
        boolean ok = true;
        for (int t = 0; t < 300 && ok; t++) {
            String text = DataGenerator.randomText(1 + rnd.nextInt(400), rnd);
            List<String> pats = DataGenerator.randomPatterns(1 + rnd.nextInt(30), text, rnd);
            AhoCorasick ac = new AhoCorasick(pats);
            List<Match> got = sorted(ac.search(text));
            List<Match> exp = sorted(bruteForce(text, pats));
            long kmp = KMP.countAll(text, pats);
            if (!got.equals(exp) || kmp != got.size() || ac.countMatches(text) != got.size()) {
                ok = false;
                System.out.println("  failing case: text=" + text + " patterns=" + pats);
            }
        }
        check("300 random cases vs brute force and KMP", ok);
    }

    static List<Match> bruteForce(String text, List<String> pats) {
        List<Match> res = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (int idx = 0; idx < pats.size(); idx++) {
            String p = pats.get(idx);
            if (seen.contains(p)) continue;
            seen.add(p);
            for (int i = 0; i + p.length() <= text.length(); i++) {
                if (text.startsWith(p, i)) res.add(new Match(i, idx, p));
            }
        }
        return res;
    }

    static List<Match> sorted(List<Match> m) {
        List<Match> c = new ArrayList<>(m);
        c.sort(Comparator.comparingInt(Match::start).thenComparing(Match::pattern));
        return c;
    }

    static void check(String name, List<Match> got, List<Match> expected) {
        check(name, got.equals(expected));
        if (!got.equals(expected)) {
            System.out.println("    expected " + expected + "\n    got      " + got);
        }
    }

    static void check(String name, boolean cond) {
        if (cond) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }
}
