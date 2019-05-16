package ru.ifmo.rain.yatcheniy.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AwaitingList<T> {
    private final List<T> list;
    private final Object monitor = new Object();
    private int filled = 0;

    AwaitingList(int size) {
        list = new ArrayList<>(Collections.nCopies(size, null));
    }

    List<T> toList() throws InterruptedException {
        synchronized (monitor) {
            while (filled < list.size()) {
                monitor.wait();
            }
            return list;
        }
    }

    void set(int i, T apply) {
        synchronized (monitor) {
            list.set(i, apply);
            ++filled;
            if (filled == list.size()) {
                monitor.notify();
            }
        }
    }
}
