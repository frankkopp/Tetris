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
 * 
 */
public class Z_Tetrimino extends Tetrimino {
	
	static int [][][] tMatrix = new int[][][] {
		{ //NORTH
			{1, 1, 0},
			{0, 1, 1},
			{0, 0, 0}
		},
		{ // EAST
			{0, 0, 1},
			{0, 1, 1},
			{0, 1, 0}
		},
		{ // SOUTH
			{0, 0, 0},
			{1, 1, 0},
			{0, 1, 1}
		},
		{ // WEST
			{0, 1, 0},
			{1, 1, 0},
			{1, 0, 0}
		}
	};
	
	public Z_Tetrimino() {
		super ("Z", TetrisColor.RED, tMatrix, new Coordinates(3,22));
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#getShape()
	 */
	@Override
	public TetriminoShape getShape() {
		return TetriminoShape.Z;
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#clone()
	 */
	@Override
	public Tetrimino clone() {
		Tetrimino tnew = new Z_Tetrimino();
		// this field can be change from public
		tnew._currentOrientation = this._currentOrientation;
		return tnew;
	}
	
	

}
