package hua223.calamity.render;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class CircleBuffer<E> implements Iterable<E> {
    public final int maxIndex;
    private boolean fill;
    public final int size;
    private final E[] elements;
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

    public static <E>  CircleBuffer<E> ofFill(int size, Supplier<E> fill) {
        CircleBuffer<E> buffer = new CircleBuffer<>(size);
        for (int i = 0; i < buffer.elements.length; i++)
            buffer.elements[i] = fill.get();
        buffer.fill = true;
        return buffer;
    }

    public void push(E e) {
        if (fill) throw new UnsupportedOperationException("New elements are not allowed to be stacked in fill mode!");
        next();
        elements[head] = e;
    }

    private void next() {
        if (++head > maxIndex) {
            head = 0;
            connected = true;
        }

        if (connected && --tail < 0) tail = maxIndex;
    }

    public E fillNext() {
        if (!fill) throw new UnsupportedOperationException("Loop mode does not allow this operation");
        next();
        return getHead();
    }

    //This method is executed in array order rather than in chronological order.
    public void forAll(Function<E, E> function) {
        for (int i = 0; i < getCount(); i++)
            elements[i] = function.apply(elements[i]);
    }

    public boolean isFull() {
        return connected;
    }

    public E get(int index) {
        return elements[Math.floorMod(head - index, getCount())];
    }

    public E getLast() {
        return elements[connected ? tail : 0];
    }

    public E getHead() {
        return elements[head];
    }

    public int getCount() {
        return connected ? size : head + 1;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        if (!connected && head == -1)
            throw new IllegalStateException("empty list cannot be traversed");
        return new BufferIterator();
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
            if (hasNext()) {
                count++;
                if (connected) {
                    if (currentIndex != tail)
                        currentIndex = (currentIndex - 1 + size) % size;
                } else if (currentIndex > 0) currentIndex--;

                return elements[currentIndex];
            }

            throw new java.util.NoSuchElementException();
        }
    }
}
