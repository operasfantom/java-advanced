package ru.ifmo.rain.yatcheniy.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

    private static int defaultCharBufferSize = 8192;
    private static byte[] buffer = new byte[defaultCharBufferSize];
    private BiConsumer<Path, Integer> onCalculatedHashAction;
    private Consumer<Path> onFailAction;

    private static int FNV32Hash(Path file) throws IOException {
        try (InputStream reader = Files.newInputStream(file)) {
            int hash = FNV_32_INIT;
            int byteRead;
            while ((byteRead = reader.read(buffer)) != -1) {
                for (int i = 0; i < byteRead; i++) {
                    hash *= FNV_32_PRIME;
                    hash ^= Byte.toUnsignedInt(buffer[i]);
                }
            }
            return hash;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        super.visitFile(file, attrs);

        int hash;
        try {
            hash = FNV32Hash(file);
        } catch (IOException e) {
            return visitFileFailed(file, e);
        }
        if (onCalculatedHashAction != null) {
            onCalculatedHashAction.accept(file, hash);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        if (onFailAction != null) {
            onFailAction.accept(file);
        }
        return FileVisitResult.CONTINUE;
    }

    public HashFileVisitor onCalculatedHash(BiConsumer<Path, Integer> function) {
        this.onCalculatedHashAction = function;
        return this;
    }

    public HashFileVisitor onFail(Consumer<Path> function) {
        this.onFailAction = function;
        return this;
    }
}
