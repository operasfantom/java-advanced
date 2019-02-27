package ru.ifmo.rain.yatcheniy.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final Comparator<? super E> comparator;
    private final List<E> data;

    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        this.data = data;
        this.comparator = comparator;
    }

    public ArraySet() {
        this.data = Collections.emptyList();
        this.comparator = null;
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        Objects.requireNonNull(collection);
        this.comparator = comparator;
        this.data = sortedList(collection);
    }

    @SuppressWarnings("unchecked")
    private int comparableCompare(E a, E b) {
        if (comparator == null) {
            return ((Comparable<E>) a).compareTo(b);
        } else {
            return comparator.compare(a, b);
        }
    }

    private List<E> getListIfSorted(Collection<? extends E> collection, Comparator<? super E> comparator) {
        if (collection.parallelStream().anyMatch(Objects::isNull)) {
            return null;
        }

        if (comparator == null) {
            if (!collection.parallelStream().allMatch(Comparable.class::isInstance)) {
                return null;
            }
        }

        var iterator = collection.iterator();

        if (!iterator.hasNext()) {
            return Collections.emptyList();
        }
        List<E> result = new ArrayList<>();
        E prev = iterator.next();
        result.add(prev);
        while (iterator.hasNext()) {
            E next = iterator.next();
            int compare = comparableCompare(prev, next);
            switch (compare) {
                case +1:
                    return null;
                case 0:
                    break;
                case -1:
                    result.add(next);
                    break;
            }
            prev = next;
        }
        return Collections.unmodifiableList(result);
    }

    private List<E> sortedList(Collection<? extends E> collection) {
        List<E> sortedListOrNull = getListIfSorted(collection, comparator);
        if (sortedListOrNull != null) {
            return sortedListOrNull;
        } else {
            Set<E> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(collection);
            return new ArrayList<>(treeSet);
        }
    }

    private int binarySearch(final E e) {
        return Collections.binarySearch(data, e, comparator);
    }

    private int findLowerOrEquals(final E e, boolean inclusive) {
        int i = binarySearch(e);
        return (i < 0 ? -i - 2 : (inclusive ? i : i - 1));
    }

    private int findGreaterOrEquals(final E e, boolean inclusive) {
        int i = binarySearch(e);
        return (i < 0 ? -i - 1 : (inclusive ? i : i + 1));
    }

    @Override
    public E lower(E e) {
        Objects.requireNonNull(e);
        int i = findLowerOrEquals(e, false);
        return i >= 0 ? data.get(i) : null;
    }

    @Override
    public E floor(E e) {
        Objects.requireNonNull(e);
        int i = findLowerOrEquals(e, true);
        return i >= 0 ? data.get(i) : null;
    }

    @Override
    public E ceiling(E e) {
        Objects.requireNonNull(e);
        int i = findGreaterOrEquals(e, true);
        return i < data.size() ? data.get(i) : null;
    }

    @Override
    public E higher(E e) {
        Objects.requireNonNull(e);
        int i = findGreaterOrEquals(e, false);
        return i < data.size() ? data.get(i) : null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("pollFirst");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("pollLast");
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        Objects.requireNonNull(o);
        return binarySearch((E) o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableCollection(data).iterator();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.parallelStream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("addAll");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversedViewList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (Objects.compare(fromElement, toElement, comparator) == +1) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        Objects.requireNonNull(fromElement);
        Objects.requireNonNull(toElement);
        int l = findGreaterOrEquals(fromElement, fromInclusive);
        int r = findLowerOrEquals(toElement, toInclusive) + 1;
        if (r + 1 == l) {
            l = r;//degenerate segment
        }
        return new ArraySet<>(data.subList(l, r), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        Objects.requireNonNull(toElement);
        int i = findLowerOrEquals(toElement, inclusive) + 1;
        return new ArraySet<>(data.subList(0, i), comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        Objects.requireNonNull(fromElement);
        int i = findGreaterOrEquals(fromElement, inclusive);
        return new ArraySet<>(data.subList(i, data.size()), comparator);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (!isEmpty()) {
            return data.get(0);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E last() {
        if (!isEmpty()) {
            return data.get(data.size() - 1);
        } else {
            throw new NoSuchElementException();
        }
    }

}
