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
import fko.tetris.util.Coordinates;

/**
 * Interface for Tetriminos 
 */
abstract public class Tetrimino {
	
	protected String 		_myName;
	protected TetrisColor 	_myColor;
	
	/**
	 * This matrix holds 4 [][] matrices - NORTH, EAST, SOUTH, WEST
	 * with y and x coordinates</br>
	 * E.g. <code>
	 * 		{ //NORTH </br>
				{0, 0, 0, 0},</br>
				{0, 1, 1, 0},</br>
				{0, 1, 1, 0},</br>
				{0, 0, 0, 0}</br>
			},</br>
			{ // EAST</br>
				{0, 0, 0, 0},</br>
				{0, 1, 1, 0},</br>
				{0, 1, 1, 0},</br>
				{0, 0, 0, 0}</br>
			},...</br>
			</code>
	 * 
	 */
	protected int[][][] _tMatrix;
	
	/**
	 * the current orientation of this Tetrimino 
	 */
	protected Facing _currentOrientation = Facing.NORTH;
	
	/**
	 * the start point for the left upper corner of the matrix
	 */
	protected Coordinates _startPoint;
	
	/**
	 * Retrieve the Matrix for a given facing
	 * @see java.lang.Object#toString()
	 */
	public int[][] getMatrix(Facing facing) {
		return _tMatrix[facing.ordinal()];
	}
	
	/**
	 * @return the _currentOrientation
	 */
	public Facing getCurrentOrientation() {
		return _currentOrientation;
	}

	/**
	 * Retrieves the color of the Tetrimino
	 * @return the _myColor
	 */
	public TetrisColor getColor() {
		return _myColor;
	}
	
	/**
	 * @return the _startPoint
	 */
	public Coordinates getStartPoint() {
		return _startPoint;
	}

	public abstract TetriminoShape getShape();
	
	/**
	 * Returns the size if the quadratic matrix (length of the array in each dimension)
	 * @return
	 */
	public int size() {
		return _tMatrix[0].length; 

	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return _myName;
		
	}
	
	/**
	 * All Tetrimino shapes
	 */
	public enum TetriminoShape {
		O,
		I,
		T,
		L,
		J,
		S,
		Z;
	}
	
	/**
	 * All for facings
	 */
	public enum Facing {
		NORTH,
		EAST,
		SOUTH,
		WEST;
	}

}
