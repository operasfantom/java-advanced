package ru.ifmo.rain.yatcheniy.implementor;

import java.util.function.Consumer;

/**
 * This class is used for buffered format print.
 * Also it provides {@link #toString()} method to get current buffer
 */
public class PrettyPrinter {
    private static final String eol = System.lineSeparator();
    private StringBuilder builder = new StringBuilder();
    private int step = 4;
    private int indentsCount = 0;
    private boolean needIndent;

    /**
     * Default constructor
     */
    public PrettyPrinter() {
    }

    /**
     * Constructor from number of indents
     *
     * @param step number of spaces which will be used to indent new block
     */
    public PrettyPrinter(int step) {
        this.step = step;
    }

    /**
     * Print string
     *
     * @param s {@link String} to print into
     */
    public void print(String s) {
        s = toUnicode(s);

        for (var c : s.toCharArray()) {
            if (needIndent) {
                builder.append(" ".repeat(indentsCount));
                needIndent = false;
            }
            builder.append(c);
            if (c == '\n') {
                needIndent = true;
            }
        }
    }

    /**
     * Print string with line separator
     *
     * @param s {@link String} to print into
     */
    public void println(String s) {
        print(s);
        println();
    }

    /**
     * Print line separator
     */
    public void println() {
        print(eol);
    }

    /**
     * Write block of data with indents providing by {@code body}. Surround it with {@code prefix} and {@code suffix}
     *
     * @param prefix {@link String} to write before accepting {@code body}
     * @param suffix {@link String} to write after accepting {@code body}
     * @param body   {@link Consumer} which provides writting action
     */
    public void block(String prefix, String suffix, Consumer<PrettyPrinter> body) {
        println(prefix);
        indent(body);
        println(suffix);
    }

    /**
     * Convert written data to {@link String}
     *
     * @return data
     */
    @Override
    public String toString() {
        return builder.toString();
    }

    /**
     * Accept {@code body}. And do action with one more indent
     *
     * @param body {@link Consumer} to accept
     */
    private void indent(Consumer<PrettyPrinter> body) {
        try {
            indentsCount += step;
            body.accept(this);
        } finally {
            indentsCount -= step;
        }
    }

    /**
     * Converts an international String to unicode numbers
     *
     * @param s {@link String} to convert from
     * @return converted string
     */
    private String toUnicode(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
}
