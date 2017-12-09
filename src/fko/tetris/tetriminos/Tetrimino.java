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
package fko.tetris.tetriminos;

import fko.tetris.TetrisColor;

/**
 * Interface for Tetriminos 
 */
abstract public class Tetrimino {
	
	public final static int NORTH = 0;
	public final static int EAST  = 1;
	public final static int SOUTH = 2;
	public final static int WEST  = 3;
	
	protected String 		_myName;
	protected TetrisColor 	_myColor;
	
	/**
	 * This matrix holds 4 [][] matrices - NORTH, EAST, SOUTH, WEST
	 * with y and x coordinates
	 */
	protected int[][][] _tMatrix;
	
	/**
	 * the start point is placed in cell 5:21 for each Tetrimino
	 */
	protected int[] 	_startPoint;
	
	/**
	 * The rotation point is place in cell 5:21 for each Tetrimino
	 * It also helps with rotation
	 */
	protected int[][]   _tRotationPoint1;
	
	@Override
	public String toString() {
		return _myName;
		
	}

}
