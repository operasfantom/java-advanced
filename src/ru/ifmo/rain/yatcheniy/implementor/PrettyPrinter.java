package ru.ifmo.rain.yatcheniy.implementor;

import java.util.function.Consumer;

public class PrettyPrinter {
    private static final String eol = System.lineSeparator();
    private StringBuilder builder = new StringBuilder();
    private int step = 4;
    private int indentsCount = 0;
    private boolean needIndent;

    public PrettyPrinter() {
    }

    public PrettyPrinter(int step) {
        this.step = step;
    }

    public void file(Class<?> token) {

    }

    public void print(String s) {
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

    public void println(String s) {
        print(s);
        println();
    }

    public void println() {
        print(eol);
    }

    public void block(String prefix, String suffix, Consumer<PrettyPrinter> body) {
        println(prefix);
        indent(body);
        println(suffix);
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    private void indent(Consumer<PrettyPrinter> body) {
        try {
            indentsCount += step;
            body.accept(this);
        } finally {
            indentsCount -= step;
        }
    }
}
