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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fko.tetris.tetriminos.I_Tetrimino;
import fko.tetris.tetriminos.J_Tetrimino;
import fko.tetris.tetriminos.L_Tetrimino;
import fko.tetris.tetriminos.O_Tetrimino;
import fko.tetris.tetriminos.S_Tetrimino;
import fko.tetris.tetriminos.T_Tetrimino;
import fko.tetris.tetriminos.Tetrimino;
import fko.tetris.tetriminos.Z_Tetrimino;

/**
 * This class holds each of the 7 Tetriminos once. The getNext() method retrieves a Tetrimino until each Tetrimino has
 * been retrieved. Then the Bag shuffles 7 new instances of each Tetrimino and returns the next of this newly shuffled
 * batch 
 */
public class Bag {
	
	private List<Tetrimino> _elements;
	
	/**
	 * Creates a bag with all 7 elements
	 */
	public Bag() {
		_elements = new ArrayList<>(7);
		fillAndShuffle();
	}
	
	/**
	 * Retrieves the next Tetrimino from the bag until all 7 Tetriminos have been retrieved. Then the Bag is re-filled 
	 * and shuffled for the next batch of 7 elements. 
	 * @return the next Tetrimino 
	 */
	public Tetrimino getNext() {
		if (_elements.isEmpty()) {
			fillAndShuffle();
		}
		return _elements.remove(0);
	}

	/*
	 * fills the bag with all 7 Tetriminos und shuffles the order 
	 */
	private void fillAndShuffle() {
		_elements.add(new O_Tetrimino());
		_elements.add(new I_Tetrimino());
		_elements.add(new T_Tetrimino());
		_elements.add(new L_Tetrimino());
		_elements.add(new J_Tetrimino());
		_elements.add(new S_Tetrimino());
		_elements.add(new Z_Tetrimino());
		Collections.shuffle(_elements);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		_elements.forEach((q) -> sb.append(q.toString()).append(" "));
		return sb.toString();
	}
	
	
	
}
