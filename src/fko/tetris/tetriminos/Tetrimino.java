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

import fko.tetris.game.TetrisColor;
import fko.tetris.util.Coordinates;

/**
 * Interface for Tetriminos 
 */
abstract public class Tetrimino {
	
	final String _myName;
	final TetrisColor _myColor;
	
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
	 *IMPORTANT: Do not change the matrix arrays
	 */
	final int[][][] _tMatrix;
	
	/**
	 * the start point for the left upper corner of the matrix
	 */
	Coordinates _currentPosition;
	
	/**
	 * the current orientation of this Tetrimino 
	 */
	Facing _currentOrientation = Facing.NORTH;
	
	/**
	 * Constructor for sub classes to use final fields
	 */
	protected Tetrimino(String myName, TetrisColor myColor, int[][][] tMatrix, Coordinates startPoint) {
		this._myName = myName;
		this._myColor = myColor;
		this._tMatrix = tMatrix;
		this._currentPosition = startPoint;
	}
	
	/**
	 * Retrieve the Matrix for a given facing
	 * @see java.lang.Object#toString()
	 */
	public int[][] getMatrix(Facing facing) {
		return _tMatrix[facing.ordinal()];
	}
	
	/**
	 * Turns the Tetrimino in the given direction.<br/>
	 * direction >0 turn right/clockwise, <0 left/counter clockwise
	 * @return the new facing
	 */
	public Facing turn(int direction) {
		if (direction < 0) {
			switch (_currentOrientation) {
			case NORTH: _currentOrientation=Facing.WEST; break;
			case EAST: _currentOrientation=Facing.NORTH; break;
			case SOUTH: _currentOrientation=Facing.EAST; break;
			case WEST: _currentOrientation=Facing.SOUTH; break;
			}
		} else if (direction > 0) {
			switch (_currentOrientation) {
			case NORTH: _currentOrientation=Facing.EAST; break;
			case EAST: _currentOrientation=Facing.SOUTH; break;
			case SOUTH: _currentOrientation=Facing.WEST; break;
			case WEST: _currentOrientation=Facing.NORTH; break;
			}
		}
		return _currentOrientation;
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
	public Coordinates getCurrentPosition() {
		return this._currentPosition;
	}
	
	/**
	 * @param _currentPosition the _currentPosition to set
	 */
	public void setCurrentPosition(Coordinates _currentPosition) {
		this._currentPosition = _currentPosition;
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
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract Tetrimino clone();

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
