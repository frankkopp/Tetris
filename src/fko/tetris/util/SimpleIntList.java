/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package fko.tetris.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Simple and fast list class for integers.
 * Grows as needed.
 *
 * @author Frank Kopp
 */
public class SimpleIntList implements Iterable<Integer> {

    /**
     * Max entries of a MoveList
     */
    public static final int DEFAULT_MAX_ENTRIES = 64;

    /**
     * When growing the list this determines how many entries entries
     * shall be generated
     */
    public static final int DEFAULT_GROWTH_MARGIN = 10;

    protected int _arraySize = DEFAULT_MAX_ENTRIES;
    protected int[] _list;
    protected int _head = 0;
    protected int _tail = 0;

    /**
     * Creates a list with a maximum of <tt>DEFAULT_MAX_ENTRIES</tt> elements
     */
    public SimpleIntList() {
        this(DEFAULT_MAX_ENTRIES);
    }

    /**
     * Creates a list with a maximum of <tt>max_size</tt> elements
     * @param max_size
     */
    public SimpleIntList(int max_size) {
        this._arraySize = max_size;
        _list = new int[max_size];
    }

    /**
     * Creates a list as a copy of the provided list.
     * @param old
     */
    public SimpleIntList(SimpleIntList old) {
        _list = Arrays.copyOfRange(old._list, old._head, old._list.length);
        this._arraySize = old._arraySize;
        this._head = 0;
        this._tail = old._tail-old._head;
    }

    /**
     * Clear the list
     */
    public void clear() {
        _tail = _head = 0;
    }

    /**
     * Adds an element to the end of the list.
     * @param i
     */
    public void add(int i) {
        if (_tail>=_list.length) {
            grow(1);
        }
        _list[_tail++] = i;
    }

    /**
     * Adds an element to the end of the list.
     * @param newList
     */
    public void add(SimpleIntList newList) {
        if (_tail+newList.size()>_list.length) {
            grow(_tail+newList.size() - _list.length);
        }
        System.arraycopy(newList._list, newList._head, this._list, this._tail, newList.size());
        this._tail +=newList.size();
    }

    /**
     * @param extra_size
     */
    private void grow(int extra_size) {
        _arraySize = _arraySize + extra_size + DEFAULT_GROWTH_MARGIN;
        _list = Arrays.copyOfRange(_list, _head, _arraySize);
        this._tail = _tail-_head;
        this._head = 0;
    }

    /**
     * Removes the last entry and returns the value.
     * If the list is empty it throws a
     * @return removed element
     */
    public int removeLast() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[--_tail];
    }

    /**
     * Removes the first entry and returns the value.
     * If the list is empty it throws a
     * @return removed element
     */
    public int removeFirst() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[_head++];
    }

    /**
     * Gets entry at a specific index
     * @param index
     * @return element at index
     */
    public int get(int index) {
        if (index < 0 || _tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        if (_head+index > _tail)
            throw new ArrayIndexOutOfBoundsException("Index too high");
        return _list[_head+index];
    }

    /**
     * Sets entry at a specific index
     * @param index
     * @param value
     * @return old value at index
     */
    public int set(int index, int value) {
        if (index < 0 || _tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        if (_head+index > _tail)
            throw new ArrayIndexOutOfBoundsException("Index too high");
        int old = _list[_head+index];
        _list[_head+index] = value;
        return old;
    }

    /**
     * Gets entry at a last index
     * @return last element
     */
    public int getLast() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[_tail-1];
    }

    /**
     * Gets entry at a first index
     * @return first element
     */
    public int getFirst() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[_head];
    }

    /**
     * Swap the first occurrence of number with the first element.
     * If the number is not in the list nothing happens.
     *
     * @param number
     */
    public void pushToHead(int number) {
        int element = -1;
        // look for number in list
        for(int i=_head; i<_tail; i++) {
            if (_list[i] == number) {
                element = i;
                break;
            }
        }
        // if found swap with first
        if (element > -1) {
            final int tmp = _list[_head];
            _list[_head] = _list[element];
            _list[element] = tmp;
        }

    }

    /**
     * Returns the size of the list
     * @return number of elements
     */
    public int size() {
        return _tail-_head;
    }

    /**
     * Returns a number of how many elements can be added to this list before it is full.
     * @return number of available slots for elements to add
     */
    public int getAvailableCapacity() {
        return _list.length-size()-_head;
    }

    /**
     * Returns true is size==0
     * @return true if empty
     */
    public boolean empty() {
        return _tail-_head == 0;
    }

    /**
     * Returns a copy of the list.
     * @return copy of list as int[]
     *
     */
    public int[] toArray() {
        return Arrays.copyOfRange(_list, _head, _tail);
    }

    /**
     * Creates a stream of int from this list
     * @return IntStream from list
     */
    public IntStream stream() {
        return Arrays.stream(_list, _head, _tail);
    }

    /**
     * clones the list
     */
    @Override
    public SimpleIntList clone() {
        return new SimpleIntList(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = "List size="+size()+" available capacity="+getAvailableCapacity()+" [";
        for (int i=_head; i<_tail; i++) {
            s += _list[i];
            if (i<_tail-1) s += ",";
        }
        s+="]";
        return s;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.toArray());
        return result;
    }

    /**
     * A MoveList is equal to another MoveList when they have the same
     * elements in the same order independent from internal implementation.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof SimpleIntList)) { return false; }
        SimpleIntList other = (SimpleIntList) obj;
        if (!Arrays.equals(this.toArray(), other.toArray())) { return false; }
        return true;
    }

    /**
     * fast sort of list
     * @param comparator
     */
    public void sort(Comparator<Integer> comparator ) {
        if (this.empty()) return;
        sort(_head, _tail, comparator);
    }

    /**
     * Sort implementation to order the list according to the given comparator.<br/>
     * Uses insertionsort for smaller arrays and quicksort for larger arrays.
     *
     * @param head (including)
     * @param tail (excluding)
     * @param comparator
     */
    private void sort(int head, int tail, Comparator<Integer> comparator) {
        if (tail-head < 150 ) {
            insertionsort(head, tail, comparator);
        } else {
            quicksort(head, tail, comparator);
        }
    }

    /**
     * Insertionsort algorithm for smaller arrays.
     *
     * @param head
     * @param tail
     * @param comparator
     */
    private void insertionsort(int head, int tail, Comparator<Integer> comparator) {
        int temp;
        for (int i = head + 1; i < tail; i++) {
            for (int j = i; j > head; j--) {
                if (comparator.compare(_list[j],_list[j-1]) < 0) {
                    temp = _list[j];
                    _list[j] = _list[j-1];
                    _list[j-1] = temp;
                }
            }
        }
    }

    /**
     * Quicksort algorithm for larger arrays.
     *
     * @param head
     * @param tail
     * @param comparator
     */
    private void quicksort(int head, int tail, Comparator<Integer> comparator) {
        tail--; // tail is not included
        int low=head, high=tail;
        int midValue=_list[(head+tail)/2];
        while (low<=high) {
            while (comparator.compare(_list[low], midValue) < 0) low++;
            while (comparator.compare(_list[high],midValue) > 0) high--;
            if (low <= high) {
                exchange(low,high);
                low++; high--;
            }
            if (head<high) sort(head, high, comparator);
            if (low<tail) sort(low, tail, comparator);
        }
    }

    /**
     * @param i
     * @param j
     */
    private void exchange(int i, int j) {
        int t=_list[i];
        _list[i]=_list[j];
        _list[j]=t;
    }

    /**
     * Returns an iterator over the elements contained in this list.  The
     * iterator traverses the elements in their <i>natural order</i>.
     *
     * @return an iterator over the elements contained in this list
     */
    @Override
    public Iterator<Integer> iterator() {
        return new SimpleIntListIterator();
    }

    private class SimpleIntListIterator implements Iterator<Integer> {

        // detect modifications
        final int initialHead = _head;
        final int initialTail = _tail;

        int cursor = _head;

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            if (modified())
                throw new ConcurrentModificationException();
            return cursor < _tail;
        }

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return  _list[cursor++];

        }

        /**
         * @return
         */
        private boolean modified() {
            return initialTail != _tail || initialHead != _head;
        }

    }


}
