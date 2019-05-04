package ru.ifmo.rain.yatcheniy.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;

@SuppressWarnings("unused")
public class IterativeParallelism implements ListIP {

    private <T> List<List<? extends T>> split(List<? extends T> values, int threads) {
        int smallGroupSize = values.size() / threads;
        int largeGroupSize = smallGroupSize + 1;
        int largeGroups = values.size() % threads;
        int smallGroups = threads - largeGroups;
        return Stream.concat(
                IntStream.range(0, largeGroups).mapToObj(i ->
                        values.subList(i * largeGroupSize, (i + 1) * (largeGroupSize))),
                IntStream.range(0, smallGroups).mapToObj(i ->
                        values.subList(largeGroups * largeGroupSize + i * smallGroupSize, largeGroups * largeGroupSize + (i + 1) * smallGroupSize))
        ).collect(Collectors.toList());
    }

    private <R, T> R applyParallel(int threads, List<? extends T> values, Function<Stream<? extends T>, R> mapper, BinaryOperator<R> mergeFunction) throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads must be grater than zero, current: " + threads);
        }
        if (values == null) {
            throw new IllegalArgumentException("Requires non-null list of values");
        }
        threads = min(threads, values.size());

        List<List<? extends T>> splitList = split(values, threads);
        final List<R> results = new ArrayList<>(Collections.nCopies(threads, null));
        final List<Exception> suppressedExceptions = new ArrayList<>(Collections.nCopies(threads, null));
        List<Thread> threadList = IntStream.range(0, threads)
                .mapToObj(i -> new Thread(() -> {
                            try {
                                R result = mapper.apply(splitList.get(i).stream());
                                results.set(i, result);
                            } catch (RuntimeException e) {
                                suppressedExceptions.set(i, e);
                            }
                        })
                ).collect(Collectors.toList());

        threadList.forEach(Thread::start);

        InterruptedException joinException = null;
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                if (joinException == null) {
                    joinException = e;
                } else {
                    joinException.addSuppressed(e);
                }
            }
        }
        if (joinException != null) {
            throw joinException;
        }

        var realSuppressedExceptions = suppressedExceptions
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!realSuppressedExceptions.isEmpty()) {
            RuntimeException compoundException = new RuntimeException();
            realSuppressedExceptions.forEach(compoundException::addSuppressed);
            throw compoundException;
        }
        return results.stream()
                .reduce(mergeFunction)
                .orElseThrow();
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                String::concat
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> stream.filter(predicate).collect(Collectors.toCollection(LinkedList::new)),
                (ts, ts2) -> {
                    ts.addAll(ts2);
                    return ts;
                }
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> stream.map(f).collect(Collectors.toCollection(LinkedList::new)),
                (ts, ts2) -> {
                    ts.addAll(ts2);
                    return ts;
                }
        );
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> (T) stream.max(comparator).orElseThrow(),
                (a, b) -> BinaryOperator.<T>maxBy(comparator).apply(a, b)
        );
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> (T) stream.min(comparator).orElseThrow(),
                (a, b) -> BinaryOperator.<T>minBy(comparator).apply(a, b)
        );
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> stream.allMatch(predicate),
                Boolean::logicalAnd
        );
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return applyParallel(
                threads,
                values,
                stream -> stream.anyMatch(predicate),
                Boolean::logicalOr
        );
    }
}
