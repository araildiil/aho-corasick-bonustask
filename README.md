# Aho–Corasick Multi-Pattern String Search (Java)

Implementation of the Aho–Corasick algorithm, compared experimentally with running KMP once per pattern.

## Project structure

```
src/main/java/ahocorasick/
    AhoCorasick.java     trie, failure links, output links, search
    KMP.java             prefix function + single-pattern KMP (baseline)
    Match.java           one match: start position + pattern
    DataGenerator.java   reproducible random texts and pattern sets
    Benchmark.java       timing experiments (System.nanoTime, 5 runs, average)
src/test/java/ahocorasick/
    AhoCorasickTest.java correctness tests vs brute force and KMP
results/
    results.csv          raw experimental results
    results.md           results as a table
run.sh                   compile + test + benchmark
```

## How to run

Requires Java 17+ (tested on OpenJDK 21). No external libraries.

```bash
./run.sh
```

or manually:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out ahocorasick.AhoCorasickTest     # tests
java -cp out ahocorasick.Benchmark results   # experiments
```

## Implementation

1. **Trie construction** — every pattern is inserted character by character. Each node stores its children (`HashMap<Character, Node>`), its depth, and the index of the pattern that ends there (or `-1`).
2. **Failure links** — built with BFS from the root. Depth-1 nodes fail to the root. For a child `v` of `u` reached by character `c`, we follow `u.fail, u.fail.fail, …` until we find a node with a `c`-edge; `v.fail` is that child (or the root if none exists). `v.fail` points to the longest proper suffix of `v`'s string that is also a prefix of some pattern. BFS guarantees all shallower failure links are ready.
3. **Output links** — `v.output` = `v.fail` if `v.fail` ends a pattern, otherwise `v.fail.output`. This chains together only the nodes that end patterns, so reporting matches never walks through "empty" failure nodes.
4. **Search** — scan the text once. For each character, follow failure links until a matching edge exists (or stay at the root), move along it, then report the pattern at the current node plus every pattern along its output-link chain. Start position = `i − depth + 1`.

Example (classic test): patterns `{he, she, his, hers}`, text `ushers` → `she` at 1, `he` at 2, `hers` at 2.

### Tests

`AhoCorasickTest` checks the classic example, overlapping patterns (`aa`, `aaa` in `aaaaa`), patterns that are suffixes of other patterns (needs output links), no-match, single-character patterns, duplicate patterns, and 300 random cases compared against brute force and KMP. All 8 test groups pass.

## Experimental setup

- Pattern counts: 10, 100, 1,000. Text lengths: 1,000, 10,000, 100,000.
- Alphabet: 10 letters (`a`–`j`), so patterns actually match and share prefixes.
- Patterns: distinct, length 3–10; half are substrings cut from the text (guaranteed hits), half are random.
- Fixed random seed, so results are reproducible.
- JVM warm-up before measuring. Each case is run **5 times** with `System.nanoTime()`; the table shows the **average**.
- **AC total** = trie + links build + search. **KMP total** = prefix function + scan, repeated for every pattern.
- Both algorithms are checked to return the same number of matches in every case.

## Results

Average of 5 runs, in milliseconds (OpenJDK 21, 2-core Linux VM):

| Patterns | Text length | Trie nodes | Matches | AC build (ms) | AC search (ms) | AC total (ms) | KMP total (ms) | KMP / AC |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 1,000 | 64 | 9 | 0.042 | 0.194 | 0.236 | 0.023 | 0.1x |
| 10 | 10,000 | 53 | 17 | 0.031 | 0.176 | 0.207 | 0.254 | 1.2x |
| 10 | 100,000 | 48 | 309 | 0.035 | 1.710 | 1.744 | 2.801 | 1.6x |
| 100 | 1,000 | 582 | 55 | 0.202 | 0.020 | 0.222 | 0.298 | 1.3x |
| 100 | 10,000 | 472 | 195 | 0.172 | 0.188 | 0.360 | 2.661 | 7.4x |
| 100 | 100,000 | 494 | 1,635 | 0.168 | 1.802 | 1.969 | 26.592 | 13.5x |
| 1000 | 1,000 | 3,912 | 651 | 1.066 | 0.028 | 1.094 | 2.305 | 2.1x |
| 1000 | 10,000 | 4,249 | 1,808 | 1.228 | 0.272 | 1.500 | 24.763 | 16.5x |
| 1000 | 100,000 | 4,133 | 14,266 | 1.379 | 2.667 | 4.046 | 268.348 | 66.3x |

Timings will differ on other machines; the trends are what matter. Sub-millisecond values are noisy (for example, the AC search time in the very first row is inflated by JIT compilation still finishing).

### Observations

- **AC search time grows with the text length and hardly depends on the number of patterns**: for n = 100,000 it is about 1.7–2.7 ms whether there are 10 or 1,000 patterns. The small increase comes from more matches being reported (z grows from 309 to 14,266).
- **AC build time grows with the total pattern length** (about 0.04 ms for 10 patterns, about 1.2 ms for 1,000) and does not depend on the text.
- **KMP time grows with both**: it scales almost exactly linearly in P × n (2.8 → 26.6 → 268 ms for 10 → 100 → 1,000 patterns at n = 100,000).
- **With few patterns and a short text, KMP wins** (10 patterns, 1,000 chars): there is no automaton to build and the arrays are cache-friendly. As soon as patterns or text grow, Aho–Corasick wins, reaching **66×** faster for 1,000 patterns on 100,000 characters.

## Complexity analysis

Notation: `n` = text length, `P` = number of patterns, `m` = total length of all patterns, `z` = number of reported matches, `σ` = alphabet size.

### Time

| Phase | Time | Why |
|---|---|---|
| Trie construction | O(m) | each pattern character creates or follows one edge |
| Failure + output links | O(m) (× map lookup) | BFS visits each node once; the failure-chain walks along any single pattern's path add up to at most its length (amortised, same argument as the KMP prefix function) |
| Search | O(n + z) | each step moves one level deeper or follows failure links that strictly reduce depth, so total failure steps ≤ n; each output link visited is a real match |
| **Total** | **O(n + m + z)** | |

With `HashMap` children each lookup is O(1) expected. With an array of size σ per node (full goto table) transitions are O(1) worst case, but building it costs O(m·σ).

**KMP for each pattern separately:** pattern `i` costs O(n + mᵢ), so all patterns cost **O(P·n + m)**. The text is scanned P times.

### Space

- Aho–Corasick: O(m) nodes. Each node stores a child map, a failure link, an output link, depth and a pattern index → **O(m)** total with hash maps (O(m·σ) with full arrays). Plus O(z) if all matches are stored.
- KMP: O(mᵢ) for one prefix-function array at a time, **O(max mᵢ)** if patterns are processed one by one.

## Role of the trie and the failure links

- **Trie**: merges all patterns into one structure. Patterns with a common prefix share nodes, so while reading the text we track progress on *all* patterns at once. A node = "the longest pattern prefix that matches the text ending here".
- **Failure links**: tell the automaton where to continue on a mismatch without moving back in the text. They jump to the longest suffix of the current match that is still a prefix of some pattern, so no possible match is lost. This is the multi-pattern generalisation of the KMP prefix function.
- **Output links**: a pattern can end inside another one (e.g. `he` inside `she`). Output links let the algorithm report all patterns ending at the current position by visiting only the nodes that actually end patterns.

## Why Aho–Corasick suits multiple patterns

The text is read **once**, no matter how many patterns there are. The cost of extra patterns is paid only in the preprocessing (O(m)), not in the search. KMP must rescan the whole text for every pattern, so its cost multiplies by P. This is exactly what the experiments show: AC search stays about 2 ms for n = 100,000 while KMP goes from 3 ms to 268 ms.

## Aho–Corasick vs KMP: advantages and limitations

**Aho–Corasick**
- + One pass over the text: O(n + m + z), independent of P in the search phase.
- + Finds overlapping matches and patterns nested inside each other.
- + Good for streaming text: the automaton is built once and reused (antivirus signatures, intrusion detection, keyword filters, DNA motif search).
- − More memory: a trie with links (pointers and maps per node), much larger than KMP's arrays.
- − More complex to implement and debug.
- − The pattern set is fixed: adding a pattern means rebuilding the links.
- − Build overhead and pointer-chasing make it slower than KMP for one or a few patterns on a short text (first row of the table).

**KMP (per pattern)**
- + Simple, small (one int array), cache-friendly.
- + Optimal O(n + m) for a single pattern; patterns can be added or removed freely.
- − Text is scanned P times: O(P·n + m), impractical for hundreds or thousands of patterns.

**Summary:** for one or a handful of patterns, KMP is simpler and just as fast. For many patterns, or large texts, Aho–Corasick is clearly better.
