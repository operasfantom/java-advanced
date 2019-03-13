package ru.ifmo.rain.yatcheniy.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class Implementor implements JarImpler {
    private final String IMPL_SUFFIX = "Impl";
    private final String JAVA_EXT = ".java";
    private final String JAR_EXT = ".jar";
    private final String CLASS_EXT = ".class";
    private ImplerException thrownException = null;
    private static String USAGE_MESSAGE = "Usage: [-jar] <className> [*.jar]";

    public static void main(String[] args) {
        if (args == null) {
            logError(USAGE_MESSAGE);
            return;
        }
        try {
            var implementor = new Implementor();
            if (args.length == 3) {
                if (args[0] == null || args[1] == null || args[2] == null) {
                    logError("Required non null parameters");
                    return;
                }
                if ("-jar".equals(args[0])) {
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } else {
                    logError(USAGE_MESSAGE);
                }
            } else {
                logError(USAGE_MESSAGE);
            }
        } catch (ClassNotFoundException e) {
            logError(String.format("Class: %s not found", args[1]));
        } catch (ImplerException e) {
            logError(String.format("Error while implementing: %s", e.getMessage()));
        } catch (InvalidPathException e) {
            logError(String.format("Invalid path: %s", args[2]));
        }
    }

    //region common
    private static void logError(String message) {
        System.err.println(message);
    }

    private Path resolvePath(Class<?> token, Path root, String extension) throws IOException {
        Path directoryPath = root.resolve(token.getPackageName().replace(".", File.separator));
        Files.createDirectories(directoryPath);
        return directoryPath.resolve(getImplementationName(token) + extension);
    }
    //endregion

    //region Java
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

        try (Writer out = Files.newBufferedWriter(resolveJavaPath(token, root))) {
            out.write(printer.toString());
            /*try (Writer writer = Files.newBufferedWriter(Paths.get("C:/temp/output.log"), StandardOpenOption.APPEND)) {
                writer.write(printer.toString());
            }*/
//            System.err.println(printer.toString());
        } catch (IOException e) {
            throw new ImplerException("Error while writing: " + e.getMessage());
        }
    }

    private void implementConstructors(Class<?> token, PrettyPrinter printer) {
        getOverriddenConstructors(token)
                .forEach(constructor -> printer.block(String.format("%s {", getConstructorHeader(constructor)), "}",
                        printer2 -> printer2.println(String.format("super(%s);", getParameters(constructor, true)))
                        )
                );
    }

    private String getConstructorHeader(Constructor<?> constructor) {
        return String.format("%s %s(%s)%s",
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
                    .collect(Collectors.joining(",", " throws ", ""));
        } else {
            return "";
        }
    }

    private int getConstructorModifiers(Constructor<?> constructor) {
        return constructor.getModifiers() & (Modifier.constructorModifiers());
    }

    private List<Constructor<?>> getOverriddenConstructors(Class<?> token) {
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(getConstructorModifiers(constructor)))
                .collect(Collectors.toCollection(ArrayList::new));
        if (constructors.isEmpty() && !token.isInterface()) {
            thrownException = new ImplerException("Token hasn't non private constructors");
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

    private void file(PrettyPrinter printer, Class<?> token) {
        printer.println(getPackage(token));
        printer.println();
        printer.block(String.format("%s {", getClassDeclaration(token)), "};",
                printer1 -> {
                    implementConstructors(token, printer1);
                    implementMethods(token, printer1);
                }
        );
    }

    private void implementMethods(Class<?> token, PrettyPrinter printer1) {
        getOverrideMethods(token)
                .forEach(method -> printer1.block(String.format("%s%s {", getMethodHead(method), getCheckedException(method)), "}",
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
                .distinct()
                .map(UniqueMethod::getMethod)
                .filter(Predicate.not(Method::isDefault))
                .filter(method -> Modifier.isAbstract(getMethodModifiers(method)) && !Modifier.isFinal(getMethodModifiers(method)))
                .filter(method -> (getMethodModifiers(method) & (Modifier.NATIVE | Modifier.VOLATILE | Modifier.PRIVATE | Modifier.FINAL)) == 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private int getMethodModifiers(Method method) {
        return method.getModifiers();
    }

    private String getMethodBody(Method method) {
        return String.format("return %s;", getDefaultValue(method.getReturnType()));
    }

    private String getDefaultValue(Class<?> returnType) {
        if (returnType.equals(void.class)) {
            return "";
        } else if (returnType.equals(Boolean.TYPE)) {
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
        return getMethodModifiers(method) & ~(Modifier.ABSTRACT | Modifier.TRANSIENT);
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

    private Path resolveJavaPath(Class<?> token, Path root) throws IOException {
        return resolvePath(token, root, JAVA_EXT);
    }
    //endregion

    //region Jar
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            Path implementationsDirectory = Files.createTempDirectory(".");
            Path buildDirectory = Files.createTempDirectory(".");
            implement(token, implementationsDirectory);
            Path javaFilePath = resolvePath(token, implementationsDirectory, JAVA_EXT);
            Path classFilePath = resolvePath(token, buildDirectory, CLASS_EXT);
            compileFile(buildDirectory, javaFilePath);
            writeJar(token, jarFile, classFilePath);
        } catch (IOException e) {
            throw new ImplerException("Error occurred while witting jar: " + e.getMessage());
        }
    }

    private void compileFile(Path buildDirectory, Path file) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler");
        }
        final List<String> args = new ArrayList<>();
        args.add(file.toString());
        args.add("-cp");
        args.add(String.format("%s%s%s", buildDirectory, File.pathSeparator, System.getProperty("java.class.path")));
        args.add("-d");
        args.add(buildDirectory.toString());
        final int exitCode = compiler.run(null, null, null, args.toArray(String[]::new));
        if (exitCode != 0) {
            throw new ImplerException(String.format("Couldn't create jar, exit code:%d", exitCode));
        }
    }

    private void writeJar(Class<?> token, Path jarFile, Path classFilePath) throws IOException {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Pavel Yatcheniy");
        mainAttributes.put(Attributes.Name.MAIN_CLASS, token.getCanonicalName());
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            stream.putNextEntry(new ZipEntry(resolvePath(token, Paths.get("."), CLASS_EXT).toString()));
            Files.copy(classFilePath, stream);
            stream.closeEntry();
        }
    }
    //endregion
}
