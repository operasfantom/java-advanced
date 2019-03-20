package ru.ifmo.rain.yatcheniy.arrayset;

import java.util.*;

class ReversedViewList<E> extends AbstractList<E> implements RandomAccess {
    private final List<E> data;

    ReversedViewList(List<E> data) {
        this.data = data;
    }

    @Override
    public E get(int index) {
        return data.get(size() - index - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            final ListIterator<E> it = data.listIterator(size());

            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public E next() {
                return it.previous();
            }
        };
    }
}
