package ru.ifmo.rain.yatcheniy.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ParallelMapperImpl implements ParallelMapper {
    private static final int TASKS_LIMIT = 32_000;
    private final List<Thread> workers;
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final List<Exception> suppressedExceptions = new ArrayList<>();

    public ParallelMapperImpl(int threads) {
        workers = new ArrayList<>(Collections.nCopies(threads, null));

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        execute();
                    }
                } catch (InterruptedException ignored) {

                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            workers.set(i, thread);
            thread.start();
        }
    }

    private void execute() throws InterruptedException {
        Runnable runnable;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            runnable = tasks.poll();
            tasks.notify();
        }
        try {
            runnable.run();
        } catch (Exception e) {
            synchronized (suppressedExceptions) {
                suppressedExceptions.add(e);
            }
        }
    }

    private void put(final Runnable task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() >= TASKS_LIMIT) {
                tasks.wait();
            }
            tasks.add(task);
            tasks.notify();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        var answers = new AwaitingList<R>(list.size());
        for (int i = 0; i < list.size(); i++) {
            int j = i;
            put(() -> answers.set(j, function.apply(list.get(j))));
        }
        List<R> result = answers.toList();

        if (!suppressedExceptions.isEmpty()) {
            RuntimeException compoundException = new RuntimeException();
            suppressedExceptions.forEach(compoundException::addSuppressed);
            throw compoundException;
        }

        return result;
    }

    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }
}
