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

/**
 * The implementation of JarImpler class.
 */
public class Implementor implements JarImpler {
    /**
     * Arguments for usage class
     */
    private static String USAGE_MESSAGE = "Usage: [-jar] <className> [*.jar]";
    /**
     * Suffix of implementation file's name
     */
    private final String IMPL_SUFFIX = "Impl";
    /**
     * Extension of java source file
     */
    private final String JAVA_EXT = ".java";
    /**
     * Extension of java class file
     */
    private final String CLASS_EXT = ".class";
    /**
     * Unhandled exception to be processed after all
     */
    private ImplerException thrownException = null;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
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

    /**
     * Log {@code message} in system error output stream
     *
     * @param message {@link String} represents error
     */
    //region common
    private static void logError(String message) {
        System.err.println(message);
    }

    /**
     * Get {@code token}'s package name regarding the {@code root}'s path
     *
     * @param token     {@link Class}
     * @param root      {@link Path} from where resolves result path
     * @param extension is extension of result path
     * @return {@link Path} of combination {@code root}'s path, splitted {@code token}'s package name and <tt>extension</tt>
     * @throws IOException if couldn't create directories to {@code token}'s package
     */
    private Path resolvePath(Class<?> token, Path root, String extension) throws IOException {
        Path directoryPath = root.resolve(token.getPackageName().replace(".", File.separator));
        Files.createDirectories(directoryPath);
        return directoryPath.resolve(getImplementationName(token) + extension);
    }
    //endregion

    //region Java

    /**
     * Implement {@code token} to file with prefix of {@code token}'s name and suffix equals to {@link Implementor#IMPL_SUFFIX}
     *
     * @param token {@link Class} to be implemented
     * @param root  {@link Path} where to store implemented class
     * @throws ImplerException if couldn't implement given {@code token}
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Required non null parameters");
        }

        if (token.isPrimitive()) {
            throw new ImplerException("Token can't be primitive type");
        }

        if (token.isArray()) {
            throw new ImplerException("Token can't be array");
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

        try (Writer out = Files.newBufferedWriter(resolvePath(token, root, JAVA_EXT))) {
            out.write(printer.toString());
            /*try (Writer writer = Files.newBufferedWriter(Paths.get("C:/temp/output.log"), StandardOpenOption.APPEND)) {
                writer.write(printer.toString());
            }*/
//            System.err.println(printer.toString());
        } catch (IOException e) {
            throw new ImplerException("Error while writing: " + e.getMessage());
        }
    }

    /**
     * Implement constructor for {@code token} and print it to <tt>printer</tt>
     *
     * @param token   {@link Class} whose constructors are implementing
     * @param printer {@link PrettyPrinter} where to print formatted constructors
     */
    private void implementConstructors(Class<?> token, PrettyPrinter printer) {
        getOverriddenConstructors(token)
                .forEach(constructor -> printer.block(String.format("%s {", getConstructorHeader(constructor)), "}",
                        printer2 -> printer2.println(String.format("super(%s);", getParameters(constructor, true)))
                        )
                );
    }

    /**
     * @param constructor {@link Constructor}
     * @return formatted header of according class
     */
    private String getConstructorHeader(Constructor<?> constructor) {
        return String.format("%s %s(%s)%s",
                Modifier.toString(getConstructorModifiers(constructor)),
                getImplementationName(constructor.getDeclaringClass()),
                getParameters(constructor, false),
                getCheckedException(constructor)
        );
    }

    /**
     * @param constructor {@link Class}'s constructor to provide exceptions
     * @return <tt>constructor</tt>'s exception types
     * @implSpec This implementation calls {@code getCheckedException(constructor.getExceptionTypes())}.
     */
    private String getCheckedException(Constructor<?> constructor) {
        return getCheckedException(constructor.getExceptionTypes());
    }

    /**
     * Format <tt>exceptionTypes</tt> joining by delimiter {@code ", throws"}
     *
     * @param exceptionTypes {@link Class[]} to be formatted
     * @return formatted <tt>exceptionTypes</tt>
     */
    private String getCheckedException(Class<?>[] exceptionTypes) {
        if (exceptionTypes.length > 0) {
            return Arrays.stream(exceptionTypes)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(",", " throws ", ""));
        } else {
            return "";
        }
    }

    /**
     * @param constructor {@link Class}'s constructor to provide modifiers
     * @return all <tt>constructor</tt>'s modifiers
     */
    private int getConstructorModifiers(Constructor<?> constructor) {
        return constructor.getModifiers() & (Modifier.constructorModifiers());
    }

    /**
     * Get {@code token}'s constructors to be overriden
     *
     * @param token {@link Class} from which loaded declared constructors
     * @return {@link List} of constructor to be overridden
     */
    private List<Constructor<?>> getOverriddenConstructors(Class<?> token) {
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(getConstructorModifiers(constructor)))
                .collect(Collectors.toCollection(ArrayList::new));
        if (constructors.isEmpty() && !token.isInterface()) {
            thrownException = new ImplerException("Token hasn't non private constructors");
        }
        return constructors;
    }

    /**
     * Get parameters of provided <tt>constructor</tt>
     *
     * @param constructor {@link Constructor} to get parameters from
     * @param isUsage     {@link #getParameters(Parameter[], boolean)}
     * @return {@link String} presentation of parameters
     */
    private String getParameters(Constructor<?> constructor, boolean isUsage) {
        return getParameters(constructor.getParameters(), isUsage);
    }

    /**
     * Get formatted parameters.
     *
     * @param parameters {@link Parameter[]} to get from
     * @param isUsage    {@code boolean} to identify whether to write parameters' name or not
     * @return {@link String} presentation of parameters
     */
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

    /**
     * Get implementation's file name of {@code token}
     *
     * @param token {@link Class} to get implementation's name from
     * @return implementation's file name
     */
    private String getImplementationName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX;
    }

    /**
     * Print {@code token}'s implementation into <tt>printer</tt>
     *
     * @param printer where to print
     * @param token   {@link Class} which is implemented
     */
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

    /**
     * Implement methods into <tt>printer</tt>
     *
     * @param token   {@link Class} where to get methods from
     * @param printer where to write implementation
     */
    private void implementMethods(Class<?> token, PrettyPrinter printer) {
        getOverrideMethods(token)
                .forEach(method -> printer.block(String.format("%s%s {", getMethodHead(method), getCheckedException(method)), "}",
                        printer2 -> printer2.println(getMethodBody(method))
                        )
                );
    }

    /**
     * Get checked exceptions of <tt>method</tt>
     *
     * @param method {@link Method} where to get Exception types
     * @return {@link String} presentation of checked exceptions
     */
    private String getCheckedException(Method method) {
        return getCheckedException(method.getExceptionTypes());
    }

    /**
     * Get unique {@code token}'s {@link Method}s to be overriden
     *
     * @param token {@link Class} to get methods from
     * @return {@link List} of methods
     */
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

    /**
     * Get method modifiers
     *
     * @param method {@link Method} from which modifiers got
     * @return mask of method modifiers
     */
    private int getMethodModifiers(Method method) {
        return method.getModifiers();
    }

    /**
     * Get method body
     *
     * @param method {@link Method} from which method got
     * @return {@link String} presentation of method body
     */
    private String getMethodBody(Method method) {
        return String.format("return %s;", getDefaultValue(method.getReturnType()));
    }

    /**
     * Get default value for {@link Class}
     *
     * @param returnType {@link Class} for which get default value
     * @return default value
     */
    private String getDefaultValue(Class<?> returnType) {
        if (returnType.equals(Void.TYPE)) {
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

    /**
     * Get method's head of <tt>method</tt>
     *
     * @param method {@link Method} from which method's head got
     * @return {@link String} presentation of method's head
     */
    private String getMethodHead(Method method) {
        return String.format("%s %s %s(%s)",
                Modifier.toString(getOverriddenModifiers(method)),
                method.getReturnType().getCanonicalName(),
                method.getName(),
                getParameters(method, false)
        );
    }

    /**
     * Get overridden modifiers of <tt>method</tt>
     *
     * @param method {@link Method} from which modifiers got
     * @return mask of overridden modifiers
     */
    private int getOverriddenModifiers(Method method) {
        return getMethodModifiers(method) & ~(Modifier.ABSTRACT | Modifier.TRANSIENT);
    }

    /**
     * Get parameters of provided <tt>constructor</tt>
     *
     * @param method  {@link Method} to get parameters from
     * @param isUsage {@link #getParameters(Parameter[], boolean)}
     * @return {@link String} presentation of parameters
     */
    private String getParameters(Method method, boolean isUsage) {
        return getParameters(method.getParameters(), isUsage);
    }

    /**
     * Get class' declaration of {@code token}
     *
     * @param token {@link Class} to get declaration from
     * @return {@link String} presentation of class' declaration
     */
    private String getClassDeclaration(Class<?> token) {
        String keyword = token.isInterface() ? "implements" : "extends";
        return String.format("class %s %s %s", getImplementationName(token), keyword, token.getCanonicalName());
    }

    /**
     * Get formatted java package name.
     *
     * @param token {@link Class} to get from
     * @return java package name
     */
    private String getPackage(Class<?> token) {
        return String.format("package %s;", token.getPackageName());
    }

    //endregion

    //region Jar

    /**
     * Implement {@code token}. Compile it to .jar by path {@code jarFile} if it's possible.
     *
     * @param token   {@link Class} which is implementing and compiling to .jar
     * @param jarFile {@link Path} where to store .jar file
     * @throws ImplerException if error occurred while writting jar
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            Path implementationsDirectory = Files.createTempDirectory(".");
//            Path buildDirectory = Files.createTempDirectory(".");
            Path buildDirectory = implementationsDirectory;
            implement(token, implementationsDirectory);
            Path javaFilePath = resolvePath(token, implementationsDirectory, JAVA_EXT);
            Path classFilePath = resolvePath(token, buildDirectory, CLASS_EXT);
            compileFile(buildDirectory, javaFilePath);
            writeJar(token, jarFile, classFilePath);
            return;
        } catch (IOException e) {
            throw new ImplerException("Error occurred while witting jar: " + e.getMessage());
        }
    }

    /**
     * Compile {@code file} to {@code buildDirectory}
     *
     * @param buildDirectory {@link Path} where to store compiled .class files
     * @param file           {@link Path} .java file which is compiled
     * @throws ImplerException if couldn't compile file
     */
    private void compileFile(Path buildDirectory, Path file) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler");
        }
        final List<String> args = new ArrayList<>();
        args.add(file.toString());
        String classPath = String.format("%s%s%s", buildDirectory, File.pathSeparator, System.getProperty("java.class.path"));
        args.addAll(Arrays.asList("-cp", classPath));
        args.addAll(Arrays.asList("-d", buildDirectory.toString()));
        args.addAll(Arrays.asList("-encoding", "UTF-8"));
        final int exitCode = compiler.run(null, null, null, args.toArray(String[]::new));
        if (exitCode != 0) {
            throw new ImplerException(String.format("Couldn't create jar, exit code:%d", exitCode));
        }
    }

    /**
     * Write compiled class {@code token} from {@code classFilePath} to {@code jarFile}
     *
     * @param token         {@link Class} whose class are compiled
     * @param jarFile       {@link Path} to .jar file where create it
     * @param classFilePath {@link Path} to temporary folder with .class files
     * @throws IOException if an I/O occurs while writting .jar file
     */
    private void writeJar(Class<?> token, Path jarFile, Path classFilePath) throws IOException {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Pavel Yatcheniy");
//        mainAttributes.put(Attributes.Name.MAIN_CLASS, token.getCanonicalName());
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            stream.putNextEntry(new ZipEntry(resolvePath(token, Paths.get(""), CLASS_EXT).toString()));
            Files.copy(classFilePath, stream);
            stream.closeEntry();
        }
    }
    //endregion
}
