package ru.ifmo.rain.yatcheniy.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AwaitingList<T> {
    private final List<T> list;
    private int filled = 0;

    AwaitingList(int size) {
        list = new ArrayList<>(Collections.nCopies(size, null));
    }

    synchronized List<T> toList() throws InterruptedException {
        while (filled < list.size()) {
            wait();
        }
        return list;
    }

    void set(int i, T apply) {
        list.set(i, apply);
        synchronized (this) {
            ++filled;
            if (filled == list.size()) {
                notify();
            }
        }
    }
}
