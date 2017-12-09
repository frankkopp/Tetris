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
 * 
 */
public class J_Tetrimino extends Tetrimino {
	
	public J_Tetrimino() {
		_myName = "J";
		_myColor = TetrisColor.BLUE;
		
		_tMatrix = new int[][][] {
			{ //NORTH
				{0, 0, 0, 0},
				{1, 0, 0, 0},
				{1, 1, 1, 0},
				{0, 0, 0, 0}
			},
			{ // EAST
				{0, 0, 0, 0},
				{0, 1, 1, 0},
				{0, 1, 0, 0},
				{0, 1, 0, 0}
			},
			{ // SOUTH
				{0, 0, 0, 0},
				{0, 0, 0, 0},
				{1, 1, 1, 0},
				{0, 0, 1, 0}
			},
			{ // WEST
				{0, 0, 0, 0},
				{0, 1, 0, 0},
				{0, 1, 0, 0},
				{1, 1, 0, 0}
			}
		};
		
		_startPoint = new int[] {2,1};
		
		_tRotationPoint1 = new int[][] {
				{0, 0, 0, 0},
				{0, 0, 0, 0},
				{0, 1, 0, 0},
				{0, 0, 0, 0}
		};
	}

}
