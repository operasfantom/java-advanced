package ru.ifmo.rain.yatcheniy.walk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class RecursiveWalk {
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new RecursiveWalkException("Usage: RecursiveWalk inputFile outputFile");
            }
            walk(args[0], args[1]);
        } catch (RecursiveWalkException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void printHash(Writer writer, String file, Integer hash) {
        try {
            writer.write(String.format("%08x %s%n", hash, file));
        } catch (IOException e) {
            System.err.println(String.format("Filing hash of %s failed:%s", file, e.getMessage()));
        }
    }

    private static void walk(String inputFileName, String outputFileName) throws RecursiveWalkException {
        Path inputPath = getCheckedPath(inputFileName, "input");
        Path outputPath = getCheckedPath(outputFileName, "output");
        try (BufferedReader inputStream = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            try {
                Path parentPath = outputPath.getParent();
                if (parentPath != null) {
                    try {
                        Files.createDirectories(parentPath);
                    } catch (IOException e) {
                        throw new RecursiveWalkException("couldn't create directory above the output file", e);
                    }
                }
                try (Writer outputStream = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                    String filePath;
                    while ((filePath = inputStream.readLine()) != null) {
                        FileVisitor<Path> fileVisitor = new HashFileVisitor()
                                .onCalculatedHash((path, hash) -> printHash(outputStream, String.valueOf(path), hash))
                                .onFail((path) -> printHash(outputStream, String.valueOf(path), 0));
                        try {
                            Files.walkFileTree(Paths.get(filePath), fileVisitor);
                        } catch (IOException | InvalidPathException e) {
                            printHash(outputStream, filePath, 0);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RecursiveWalkException(String.format("couldn't write to output file: %s", outputFileName), e);
            }
        } catch (FileNotFoundException e) {
            throw new RecursiveWalkException("input file not found");
        } catch (IOException e) {
            throw new RecursiveWalkException(String.format("couldn't read from input file: %s", inputFileName), e);
        }
    }

    private static Path getCheckedPath(String fileName, String fileDescription) throws RecursiveWalkException {
        try {
            return Paths.get(fileName);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException(String.format("invalid path of %s: %s", fileDescription, fileName));
        }
    }
}
