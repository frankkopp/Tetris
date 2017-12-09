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

/**
 * This represent the internal playfield which basically consists of a matrix of 10 columns and 40 rows.
 * The matrix coordinates start at 0,0 at the lower left cell and 9,39 with the upper right cell.
 * 
 * 
 * From Tetris Guideline
 * Playfield is 10×40, where rows above 20 are hidden or obstructed by the field frame to trick the player into thinking
 * it's 10×20. In 2002 Guideline, it could be at least 22 height. 
 */
public class Playfield {
		
	/**
	 * The line which separates he visible area from the hidden buffer zone
	 */
	public static final int SKYLINE = 20;
	
	/**
	 * The height of the buffer zone.
	 * Together with the SKYLINE it determines the height of the overall playfield matrix
	 */
	public static final int BUFFERZONE = 20;
	
	/**
	 * Playfield matrix width
	 */
	public static final int PLAYFIELD_WIDTH = 10;
			
	// the internal data structure for the matrix
	// a zero representing an empty field, a non-zero an occupied field
	// non-zeros also determine the color (defined in Tetrimino class)
	private TetrisColor[][] _mainMatrix;
	
	// convenience field for SKYLINE+BUFFERZONE
	private int _playfieldHeight;

	/**
	 * Generates a new Playfield with default width and height
	 * @param _mainMatrix
	 */
	public Playfield() {
		this._playfieldHeight = SKYLINE+BUFFERZONE;
		this._mainMatrix = new TetrisColor[PLAYFIELD_WIDTH][_playfieldHeight];
		
		// iterate through all cells and initialize with zero
		for (int yi = 0; yi < _playfieldHeight; yi++) {
			for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
				_mainMatrix[xi][yi] = TetrisColor.EMPTY;
			}
		}
		
		// DEBUG
		_mainMatrix[0][0] = TetrisColor.BLUE;
		_mainMatrix[1][0] = TetrisColor.BLUE;
		_mainMatrix[2][0] = TetrisColor.BLUE;
		_mainMatrix[3][0] = TetrisColor.BLUE;
		_mainMatrix[4][0] = TetrisColor.PURPLE;
		_mainMatrix[5][0] = TetrisColor.PURPLE;
		_mainMatrix[5][1] = TetrisColor.PURPLE;
		_mainMatrix[6][0] = TetrisColor.PURPLE;
		_mainMatrix[7][0] = TetrisColor.YELLOW;
		_mainMatrix[7][1] = TetrisColor.YELLOW;
		_mainMatrix[8][0] = TetrisColor.YELLOW;
		_mainMatrix[8][1] = TetrisColor.YELLOW;

		_mainMatrix[0][20] = TetrisColor.YELLOW;
		_mainMatrix[0][21] = TetrisColor.YELLOW;
		_mainMatrix[1][20] = TetrisColor.YELLOW;
		_mainMatrix[1][21] = TetrisColor.YELLOW;
		
		_mainMatrix[2][18] = TetrisColor.YELLOW;
		_mainMatrix[2][19] = TetrisColor.YELLOW;
		_mainMatrix[3][18] = TetrisColor.YELLOW;
		_mainMatrix[3][19] = TetrisColor.YELLOW;

		_mainMatrix[4][19] = TetrisColor.YELLOW;
		_mainMatrix[4][20] = TetrisColor.YELLOW;
		_mainMatrix[5][19] = TetrisColor.YELLOW;
		_mainMatrix[5][20] = TetrisColor.YELLOW;
		
	}
	
	public int getColumns() {
		return PLAYFIELD_WIDTH;
	}

	public int getVisibleRows() {
		return SKYLINE;
	}

	/**
	 * @param xi
	 * @param yi
	 * @return
	 */
	public TetrisColor getColor(int xi, int yi) {
		return _mainMatrix[xi][yi];
	}

	
	
	

}
