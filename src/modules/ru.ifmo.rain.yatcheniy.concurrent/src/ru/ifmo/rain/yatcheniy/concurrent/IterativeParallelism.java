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

public class IterativeParallelism implements ListIP {

    private <T> List<List<? extends T>> split(List<? extends T> values, int threads) {
        int groupSize = values.size() / threads;
        int tailSize = values.size() - groupSize * (threads - 1);
        List<List<? extends T>> collect = IntStream.range(0, threads - 1)
                .mapToObj(i -> values.subList(i * groupSize, min((i + 1) * (groupSize), values.size())))
                .collect(Collectors.toList());
        collect.add(values.subList(values.size() - tailSize, values.size()));
        return collect;
    }

    private <R, T> R applyParallel(int threads, List<? extends T> values, Function<Stream<? extends T>, R> mapper, BinaryOperator<R> mergeFunction) throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("number of threads must be grater than zero, current: " + threads);
        }
        if (values == null) {
            throw new IllegalArgumentException("Requires non-null list of values");
        }
        threads = min(threads, values.size());

        List<List<? extends T>> splittedList = split(values, threads);
        List<R> results = new ArrayList<>(Collections.nCopies(threads, null));
        List<Thread> threadList = IntStream.range(0, threads)
                .mapToObj(i -> new Thread(() -> {
                            try {
                                R apply = mapper.apply(splittedList.get(i).stream());
                                results.set(i, apply);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                ).collect(Collectors.toList());

        threadList.forEach(Thread::start);

        boolean interrupted = threadList.stream()
                .reduce(false, (aBoolean, thread) -> {
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                return true;
                            }
                            return false;
                        },
                        Boolean::logicalOr);

        if (interrupted) {
            throw new InterruptedException("Interrupted while executing parallel");
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
