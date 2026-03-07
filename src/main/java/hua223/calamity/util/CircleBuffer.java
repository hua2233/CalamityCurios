package hua223.calamity.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class CircleBuffer<E> implements Iterable<E> {
    public final int maxIndex;
    public final int size;
    private final E[] elements;
    private BufferIterator iterator;
    private boolean connected;
    private int tail;
    private int head = -1;

    @SuppressWarnings("unchecked")
    public CircleBuffer(int size) {
        if (size < 0) throw new IllegalArgumentException("Max index must be positive");
        elements = (E[]) new Object[size];
        this.size = size;
        this.maxIndex = size - 1;
    }

    public void push(E e) {
        if (++head > maxIndex) {
            head = 0;
            connected = true;
        }

        if (connected && --tail < 0) tail = maxIndex;

        elements[head] = e;
    }

    public void forAll(Function<E, E> function) {
        for (int i = 0; i < getCount(); i++)
            elements[i] = function.apply(elements[i]);
    }

    public E get(int index) {
        return elements[Math.floorMod(head - index, size)];
    }

    public E getLast() {
        return elements[connected ? tail : 0];
    }

    public E getHead() {
        return elements[head];
    }

    /**
     * Convert elements in the current data structure to a List collection
     *
     * @return A List collection containing all the current elements
     */
    public List<E> toList() {
        List<E> list = new ArrayList<>(getCount());
        if (!connected) {
            for (int i = head; i >= 0; i--) {
                list.add(elements[i]);
            }
        } else {
            int currentIndex = head;
            do {
                list.add(elements[currentIndex]);
                if (currentIndex == tail) {
                    break;
                }
                currentIndex = (currentIndex - 1 + size) % size;
            } while (true);
        }

        return list;
    }

    public int getCount() {
        return connected ? size : head + 1;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        if (!connected && head == 0) throw new IllegalStateException("empty list cannot be traversed");
        if (iterator != null) {
            iterator.count = 0;
            iterator.currentIndex = head;
            return iterator;
        }
        return iterator = new BufferIterator();
    }

    private class BufferIterator implements Iterator<E> {
        private int currentIndex;
        private int count;

        private BufferIterator() {
            currentIndex = head;
        }

        @Override
        public boolean hasNext() {
            if (connected) {
                return count < size;
            } else {
                return count <= head;
            }
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }

            count++;
            if (connected) {
                if (currentIndex != tail) {
                    currentIndex = (currentIndex - 1 + size) % size;
                }
            } else {
                if (currentIndex > 0) {
                    currentIndex--;
                }
            }

            return elements[currentIndex];
        }
    }
}
