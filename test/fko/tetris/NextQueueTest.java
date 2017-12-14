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
package fko.tetris;

import java.util.ListIterator;

import org.junit.Test;

import fko.tetris.tetriminos.Tetrimino;

/**
 * 
 */
public class NextQueueTest {

	/**
	 * Test method for {@link fko.tetris.NextQueue#getNext()}.
	 */
	@Test
	public final void testGetNext() {
		Bag bag = new Bag();
		NextQueue nq = new NextQueue(bag, 3);
		
		for (int i=0; i<100; i++) {
			nq.getNext();
		}
		
		
	}

	/**
	 * Test method for {@link fko.tetris.NextQueue#getListIterator()}.
	 */
	@Test
	public final void testGetListIterator() {
		Bag bag = new Bag();
		NextQueue nq = new NextQueue(bag, 3);
		
		for (int i=0; i<100; i++) {
			ListIterator<Tetrimino> li = nq.getListIterator();
			while(li.hasNext()) {
				System.out.print(li.next().toString()+" ");
			}
			System.out.println();
			System.out.println(nq.getNext() + " <-- " + nq + " <-- " + bag);
		}
	}

}
