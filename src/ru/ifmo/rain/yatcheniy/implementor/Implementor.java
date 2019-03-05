package ru.ifmo.rain.yatcheniy.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Implementor implements Impler {
    private static final String IMPL_SUFFIX = "Impl";
    private static final String JAVA_EXT = ".java";

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Required non null parameters");
        }

        if (token.isPrimitive()) {
            throw new ImplerException("Token can't be primitive type");
        }

        var printer = new PrettyPrinter();
        file(printer, token);

        try (Writer out = Files.newBufferedWriter(resolvePath(token, root))) {
            out.write(printer.toString());
            System.err.println(printer.toString());
        } catch (IOException e) {
            throw new ImplerException("Error while writing:" + e.getMessage());
        }
    }

    private void file(PrettyPrinter printer, Class<?> token) {
        printer.println(getPackage(token));
        printer.println();
        printer.block(String.format("%s {", getClassDeclaration(token)), "};",
                printer1 -> Arrays.stream(token.getMethods()).
                        forEach(method -> printer1.block(String.format("%s {", getMethodHead(method)), "}",
                                printer2 -> printer2.println(getMethodBody(method))
                                )
                        )
        );
    }

    private String getMethodBody(Method method) {
        return String.format("return %s;", getDefaultValue(method.getReturnType()));
    }

    private String getDefaultValue(Class<?> returnType) {
        if (returnType.equals(void.class)) {
            return "";
        } else if (returnType.equals(boolean.class)) {
            return "true";
        } else if (returnType.isPrimitive()) {
            return "0";
        } else {
            return "null";
        }
    }

    private String getMethodHead(Method method) {
        return String.format("%s %s %s(%s)",
                Modifier.toString(getOverriddenModifiers(method)),
                method.getReturnType().getCanonicalName(),
                method.getName(),
                getParameters(method)
        );
    }

    private int getOverriddenModifiers(Method method) {
        return method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.TRANSIENT);
    }

    private String getParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> String.format("%s %s", parameter.getType().getCanonicalName(), parameter.getName()))
                .collect(Collectors.joining(", "));

    }

    private String getClassDeclaration(Class<?> token) {
        return String.format("class %s implements %s", getImplementationName(token), token.getSimpleName());
    }

    private String getImplementationName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX;
    }

    private String getPackage(Class<?> token) {
        return String.format("package %s;", token.getPackageName());
    }

    private Path resolvePath(Class<?> token, Path root) throws IOException {
        Path directoryPath = root.resolve(token.getPackageName().replace(".", File.separator));
        Files.createDirectories(directoryPath);
        return directoryPath.resolve(getImplementationName(token) + JAVA_EXT);
    }
}
