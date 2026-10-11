package hua223.calamity.render;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CircleBuffer<E> {
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

    //similar to list
    public static <E>  CircleBuffer<E> fillFull(int size, Supplier<E> fill) {
        CircleBuffer<E> buffer = ofFill(size, fill);
        buffer.head = buffer.maxIndex;
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

        if (connected) tail = (head + 1) % size;
    }

    public E fillNext() {
        if (!fill) throw new UnsupportedOperationException("Loop mode does not allow this operation");
        next();
        return getHead();
    }

    //This method is executed in array order rather than in chronological order.
    public void forAll(Consumer<E> consumer) {
        for (int i = 0; i < getCount(); i++) consumer.accept(elements[i]);
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
}
