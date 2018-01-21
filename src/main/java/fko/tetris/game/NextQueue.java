/**
MIT License

Copyright (c) 2017 Frank Kopp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fko.tetris.game;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import fko.tetris.tetriminos.Tetrimino;

/**
 * Simple FIFO Queue which is always back-filled with elements from a given Bag.
 */
public class NextQueue {

	private List<Tetrimino> _queue;
	private Bag _bag; // the bag object to refill the queue
	private int _length; // the length of the queue

	/**
	 * Creates a queue with length elements which is back-filled from the given Bag
	 * @param bag
	 * @param length
	 */
	public NextQueue(Bag bag, int length) {
		this._bag = bag;
		this._length = length;
		_queue = new ArrayList<>(_length);
		fillQueue();
	}
	
	/**
	 * Retrieves the next Tetrimino from the queue and back-fills the queue from the Bag.
	 * @return the next Tetrimino
	 */
	public Tetrimino getNext() {
		Tetrimino next = _queue.remove(0);
		fillQueue();
		return next;
	}
	
	/**
	 * Retrieves the Tetrimino at position i 
	 * @return the next Tetrimino
	 */
	public Tetrimino get(int i) {
		return _queue.get(i);
	}
	
	/**
	 * Iterator over all elements in correct order.
	 * @return the ListIterator for the queued elements
	 */
	public ListIterator<Tetrimino> getListIterator() {
		return _queue.listIterator();
	}

	/*
	 *	back-fills the queue from the Bag until it is full 
	 */
	private void fillQueue() {
		while (_queue.size() < _length) {
			_queue.add(_bag.getNext());
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		_queue.forEach((q) -> sb.append(q.toString()).append(" "));
		return sb.toString();
	}
	
	

}
