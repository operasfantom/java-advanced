package ru.ifmo.rain.yatcheniy.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Implementor implements Impler {
    private final String IMPL_SUFFIX = "Impl";
    private final String JAVA_EXT = ".java";
    private ImplerException thrownException = null;

    private void implementConstructors(Class<?> token, PrettyPrinter printer) throws ImplerException {
        getOverriddenConstructors(token)
                .forEach(constructor -> printer.block(String.format("%s {", getConstructorHeader(constructor)), "}",
                        printer2 -> printer2.println(String.format("super(%s);", getParameters(constructor, true)))
                        )
                );
    }

    private String getConstructorHeader(Constructor<?> constructor) {
        return String.format("%s %s(%s) %s",
                Modifier.toString(getConstructorModifiers(constructor)),
                getImplementationName(constructor.getDeclaringClass()),
                getParameters(constructor, false),
                getCheckedException(constructor)
        );
    }

    private String getCheckedException(Constructor<?> constructor) {
        return getCheckedException(constructor.getExceptionTypes());
    }

    private String getCheckedException(Class<?>[] exceptionTypes) {
        if (exceptionTypes.length > 0) {
            return Arrays.stream(exceptionTypes)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(",", "throws ", ""));
        } else {
            return "";
        }
    }

    private int getConstructorModifiers(Constructor<?> constructor) {
        return constructor.getModifiers() & (Modifier.constructorModifiers());
    }

    private List<Constructor<?>> getOverriddenConstructors(Class<?> token) throws ImplerException {
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (constructors.isEmpty() && !token.isInterface()) {
            throw new ImplerException("Token hasn't non private constructors");
        }
        return constructors;
    }

    private String getParameters(Constructor<?> constructor, boolean isUsage) {
        return getParameters(constructor.getParameters(), isUsage);
    }

    private String getParameters(Parameter[] parameters, boolean isUsage) {
        return Arrays.stream(parameters)
                .map(parameter ->
                        isUsage ?
                                String.format("%s", parameter.getName())
                                :
                                String.format("%s %s", parameter.getType().getCanonicalName(), parameter.getName())
                )
                .collect(Collectors.joining(", "));

    }

    private String getImplementationName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX;
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Required non null parameters");
        }

        if (token.isPrimitive()) {
            throw new ImplerException("Token can't be primitive type");
        }

        if (token == Enum.class) {
            throw new ImplerException("Token can't be java.lang.Enum");
        }

        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Token can't final");
        }

        var printer = new PrettyPrinter();
        file(printer, token);

        if (thrownException != null) {
            throw thrownException;
        }

        try (Writer out = Files.newBufferedWriter(resolvePath(token, root))) {
            out.write(printer.toString());
            /*try (Writer writer = Files.newBufferedWriter(Paths.get("C:/temp/output.log"), StandardOpenOption.APPEND)) {
                writer.write(printer.toString());
            }*/
            System.err.println(printer.toString());
        } catch (IOException e) {
            throw new ImplerException("Error while writing: " + e.getMessage());
        }
    }

    private void file(PrettyPrinter printer, Class<?> token) {
        printer.println(getPackage(token));
        printer.println();
        printer.block(String.format("%s {", getClassDeclaration(token)), "};",
                printer1 -> {
                    try {
                        implementConstructors(token, printer1);
                        implementMethods(token, printer1);
                    } catch (ImplerException e) {
                        thrownException = e;
                    }
                }
        );
    }

    private void implementMethods(Class<?> token, PrettyPrinter printer1) {
        getOverrideMethods(token)
                .forEach(method -> printer1.block(String.format("%s %s {", getMethodHead(method), getCheckedException(method)), "}",
                        printer2 -> printer2.println(getMethodBody(method))
                        )
                );
    }

    private String getCheckedException(Method method) {
        return getCheckedException(method.getExceptionTypes());
    }

    private List<Method> getOverrideMethods(Class<?> token) {
        return Stream.<Class<?>>iterate(token, Objects::nonNull, Class::getSuperclass)
                .map(aClass -> new ArrayList<Method>() {{
                    addAll(Arrays.asList(aClass.getDeclaredMethods()));
                    addAll(Arrays.asList(aClass.getMethods()));
                }})
                .flatMap(ArrayList::stream)
                .map(UniqueMethod::new)
//                .peek(uniqueMethod -> System.err.println(uniqueMethod.method))
//                .peek(uniqueMethod -> System.err.println('.'))
                .distinct()
//                .peek(uniqueMethod -> System.err.println(uniqueMethod.method))
                .map(UniqueMethod::getMethod)
                .filter(method -> Modifier.isAbstract(method.getModifiers() & Modifier.methodModifiers()))
                .filter(Predicate.not(Method::isDefault))
                .filter(method -> (method.getModifiers() & (Modifier.NATIVE | Modifier.VOLATILE | Modifier.PRIVATE | Modifier.FINAL)) == 0)
                .filter(method -> !Modifier.isFinal(method.getModifiers()))
                .collect(Collectors.toCollection(ArrayList::new));
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
        } else if (returnType.equals(String.class)) {
            return "\"\"";
        } else {
            return "null";
        }
    }

    private String getMethodHead(Method method) {
        return String.format("%s %s %s(%s)",
                Modifier.toString(getOverriddenModifiers(method)),
                method.getReturnType().getCanonicalName(),
                method.getName(),
                getParameters(method, false)
        );
    }

    private int getOverriddenModifiers(Method method) {
        return method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.TRANSIENT);
    }

    private String getParameters(Method method, boolean isUsage) {
        return getParameters(method.getParameters(), isUsage);
    }

    private String getClassDeclaration(Class<?> token) {
        String keyword = token.isInterface() ? "implements" : "extends";
        return String.format("class %s %s %s", getImplementationName(token), keyword, token.getSimpleName());
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
