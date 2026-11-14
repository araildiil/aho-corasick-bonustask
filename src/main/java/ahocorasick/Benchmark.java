package ahocorasick;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    static final int[] PATTERN_COUNTS = {10, 100, 1000};
    static final int[] TEXT_LENGTHS = {1_000, 10_000, 100_000};
    static final int RUNS = 5;
    static final long SEED = 42L;

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outDir);

        warmUp();

        StringBuilder md = new StringBuilder();
        md.append("| Patterns | Text length | Trie nodes | Matches | AC build (ms) | AC search (ms) | AC total (ms) | KMP total (ms) | KMP / AC |\n");
        md.append("|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(outDir.resolve("results.csv")))) {
            csv.println("patterns,text_length,trie_nodes,matches,ac_build_ms,ac_search_ms,ac_total_ms,kmp_total_ms,speedup");
            System.out.printf("%-9s %-11s %-10s %-8s %-13s %-14s %-13s %-14s %s%n",
                    "Patterns", "TextLen", "Nodes", "Matches", "AC build ms", "AC search ms", "AC total ms", "KMP total ms", "KMP/AC");

            for (int p : PATTERN_COUNTS) {
                for (int n : TEXT_LENGTHS) {
                    Random rnd = new Random(SEED + 31L * p + n);
                    String text = DataGenerator.randomText(n, rnd);
                    List<String> patterns = DataGenerator.randomPatterns(p, text, rnd);

                    long buildSum = 0, searchSum = 0, kmpSum = 0;
                    long acMatches = 0, kmpMatches = 0;
                    int nodes = 0;

                    for (int r = 0; r < RUNS; r++) {
                        long t0 = System.nanoTime();
                        AhoCorasick ac = new AhoCorasick(patterns);
                        long t1 = System.nanoTime();
                        acMatches = ac.countMatches(text);
                        long t2 = System.nanoTime();
                        kmpMatches = KMP.countAll(text, patterns);
                        long t3 = System.nanoTime();

                        buildSum += t1 - t0;
                        searchSum += t2 - t1;
                        kmpSum += t3 - t2;
                        nodes = ac.getNodeCount();
                    }

                    if (acMatches != kmpMatches) {
                        throw new IllegalStateException("Mismatch: AC=" + acMatches + " KMP=" + kmpMatches);
                    }

                    double build = ms(buildSum / RUNS);
                    double search = ms(searchSum / RUNS);
                    double total = build + search;
                    double kmp = ms(kmpSum / RUNS);
                    double speedup = kmp / total;

                    System.out.printf(Locale.US, "%-9d %-11d %-10d %-8d %-13.3f %-14.3f %-13.3f %-14.3f %.1fx%n",
                            p, n, nodes, acMatches, build, search, total, kmp, speedup);
                    csv.printf(Locale.US, "%d,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%.2f%n",
                            p, n, nodes, acMatches, build, search, total, kmp, speedup);
                    md.append(String.format(Locale.US, "| %d | %,d | %,d | %,d | %.3f | %.3f | %.3f | %.3f | %.1fx |%n",
                            p, n, nodes, acMatches, build, search, total, kmp, speedup));
                }
            }
        }

        Files.writeString(outDir.resolve("results.md"), md.toString());
        System.out.println("\nSaved " + outDir.resolve("results.csv") + " and " + outDir.resolve("results.md"));
    }

    private static void warmUp() {
        Random rnd = new Random(7);
        String text = DataGenerator.randomText(50_000, rnd);
        List<String> pats = DataGenerator.randomPatterns(200, text, rnd);
        long sink = 0;
        for (int i = 0; i < 30; i++) {
            sink += new AhoCorasick(pats).countMatches(text);
            sink += KMP.countAll(text, pats);
        }
        if (sink == 42) System.out.println();
    }

    private static double ms(long nanos) {
        return nanos / 1_000_000.0;
    }
}
