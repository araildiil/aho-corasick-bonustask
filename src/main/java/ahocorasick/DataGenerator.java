package ahocorasick;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class DataGenerator {
    public static final String ALPHABET = "abcdefghij";

    private DataGenerator() {
    }

    public static String randomText(int length, Random rnd) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static List<String> randomPatterns(int count, String text, Random rnd) {
        Set<String> set = new LinkedHashSet<>();
        while (set.size() < count) {
            int len = 3 + rnd.nextInt(8);
            if (rnd.nextBoolean() && text.length() > len) {
                int start = rnd.nextInt(text.length() - len);
                set.add(text.substring(start, start + len));
            } else {
                set.add(randomText(len, rnd));
            }
        }
        return new ArrayList<>(set);
    }
}
