package ahocorasick;

public record Match(int start, int patternIndex, String pattern) {
    @Override
    public String toString() {
        return "\"" + pattern + "\" at " + start;
    }
}
