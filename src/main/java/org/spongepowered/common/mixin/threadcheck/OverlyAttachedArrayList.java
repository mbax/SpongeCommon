/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.threadcheck;

import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

final class OverlyAttachedArrayList<E> extends ArrayList<E> {
    private class OverlyAttachedIterator implements Iterator<E> {
        private final Iterator<E> iterator;

        public OverlyAttachedIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public E next() {
            return this.iterator.next();
        }

        @Override
        public void remove() {
            OverlyAttachedArrayList.this.check();
            this.iterator.remove();
        }
    }

    private class OverlyAttachedListIterator implements ListIterator<E> {
        private final ListIterator<E> iterator;

        public OverlyAttachedListIterator(ListIterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public void add(E e) {
            OverlyAttachedArrayList.this.check();
            this.iterator.add(e);
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return this.iterator.hasPrevious();
        }

        @Override
        public E next() {
            return this.iterator.next();
        }

        @Override
        public int nextIndex() {
            return this.iterator.nextIndex();
        }

        @Override
        public E previous() {
            return this.iterator.previous();
        }

        @Override
        public int previousIndex() {
            return this.iterator.previousIndex();
        }

        @Override
        public void remove() {
            OverlyAttachedArrayList.this.check();
            this.iterator.remove();
        }

        @Override
        public void set(E e) {
            OverlyAttachedArrayList.this.check();
            this.iterator.set(e);
        }
    }

    private static final long serialVersionUID = 0L;
    private final Thread thread;

    OverlyAttachedArrayList() {
        System.out.println("Doot doot");
        this.thread = Thread.currentThread();
    }

    @Override
    public boolean add(E e) {
        this.check();
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        this.check();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.check();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        this.check();
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        this.check();
        super.clear();
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return super.containsAll(c);
    }

    @Override
    public E get(int index) {
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return super.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return new OverlyAttachedIterator(super.iterator());
    }

    @Override
    public int lastIndexOf(Object o) {
        return super.lastIndexOf(o);
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator() {
        return new OverlyAttachedListIterator(super.listIterator());
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator(int index) {
        return new OverlyAttachedListIterator(super.listIterator(index));
    }

    @Override
    public E remove(int index) {
        this.check();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        this.check();
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.check();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    @Override
    public E set(int index, E element) {
        return super.set(index, element);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    private void check() {
        if (!Thread.currentThread().equals(this.thread)) {
            SpongeImpl.getLogger().warn("Asynchronous thread playing where it shouldn't!", new SpongeThreadSafetyException());
        }
    }
}
