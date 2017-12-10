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

import fko.tetris.tetriminos.Tetrimino;
import fko.tetris.tetriminos.Tetrimino.Facing;

/**
 * This represent the internal playfield which basically consists of two matrices of 10 columns and 40 rows.
 * The matrix coordinates start at 0,0 at the lower left cell and 9,39 with the upper right cell.
 * 
 * From Tetris Guideline
 * Playfield is 10:40, where rows above 20 are hidden or obstructed by the field frame to trick the player into thinking
 * it's 10:20. In 2002 Guideline, it could be at least 22 height. 
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

	// the internal data structure for the background matrix.
	// All Tetriminos which are locked in place are merged into the background.
	// A zero representing an empty field, a non-zero an occupied field.
	// Non-zeros also determine the color (defined in Tetrimino class)
	private TetrisColor[][] _backgroundMatrix;

	// the internal data structure for the foreground matrix.
	// Tetriminos which are still in play stored here. 
	// The foreground checks also for collisions against the background. 
	// A zero representing an empty field, a non-zero an occupied field.
	// Non-zeros also determine the color (defined in Tetrimino class)
	private TetrisColor[][] _foregroundMatrix;


	// convenience field for SKYLINE+BUFFERZONE
	private int _playfieldHeight = SKYLINE + BUFFERZONE;

	/**
	 * Generates a new Playfield with default width and height
	 * @param _backgroundMatrix
	 */
	public Playfield() {
		this._backgroundMatrix = new TetrisColor[PLAYFIELD_WIDTH][_playfieldHeight];
		this._foregroundMatrix = new TetrisColor[PLAYFIELD_WIDTH][_playfieldHeight];

		clearMatrix(_backgroundMatrix);
		clearMatrix(_foregroundMatrix);

		// DEBUG
		//		_backgroundMatrix[3][18] = TetrisColor.BLUE;
		//		_backgroundMatrix[4][18] = TetrisColor.BLUE;
		//		_backgroundMatrix[5][18] = TetrisColor.BLUE;
		//		_backgroundMatrix[6][18] = TetrisColor.BLUE;

		_backgroundMatrix[0][1] = TetrisColor.BLUE;
		_backgroundMatrix[1][1] = TetrisColor.BLUE;
		_backgroundMatrix[2][1] = TetrisColor.BLUE;
		_backgroundMatrix[3][1] = TetrisColor.BLUE;

		/*
		_backgroundMatrix[0][0] = TetrisColor.BLUE;
		_backgroundMatrix[1][0] = TetrisColor.BLUE;
		_backgroundMatrix[2][0] = TetrisColor.BLUE;
		_backgroundMatrix[3][0] = TetrisColor.BLUE;
		_backgroundMatrix[4][0] = TetrisColor.PURPLE;
		_backgroundMatrix[5][0] = TetrisColor.PURPLE;
		_backgroundMatrix[5][1] = TetrisColor.PURPLE;
		_backgroundMatrix[6][0] = TetrisColor.PURPLE;
		_backgroundMatrix[7][0] = TetrisColor.YELLOW;
		_backgroundMatrix[7][1] = TetrisColor.YELLOW;
		_backgroundMatrix[8][0] = TetrisColor.YELLOW;
		_backgroundMatrix[8][1] = TetrisColor.YELLOW;
		_foregroundMatrix[4][19] = TetrisColor.YELLOW;
		_foregroundMatrix[4][20] = TetrisColor.YELLOW;
		_foregroundMatrix[5][19] = TetrisColor.YELLOW;
		_foregroundMatrix[5][20] = TetrisColor.YELLOW;
		 */

	}

	/**
	 * Spawns a new Tetrimino.<br/>
	 * Tetriminos are all generated North Facing (just as they appear in the Next Queue) on the 
	 * 21st and 22nd rows, just above the Skyline. Every Tetrimino that is three Minos wide is generated on the 4th 
	 * cell across (4, 21) and stretches to the 6th (6, 21). This includes the T-Tetrimino, L-Tetrimino, J-Tetrimino, 
	 * S-Tetrimino and Z-Tetrimino. The I-Tetrimino and O-Tetrimino are exactly centered horizontally at generation. 
	 * The I-Tetrimino is generated on the 21st row (not 22nd), stretching from the 4th to 7th cells (4, 21) - (7, 21),
	 * and the O-Tetrimino is generated on the 5th and 6th cell (5, 21) - (6, 21).
	 * @param next
	 * @return true if collision detected - false otherwise
	 */
	public boolean spawn(Tetrimino next) {

		clearMatrix(_foregroundMatrix);
		
		int[][] tMatrix = next.getMatrix(Facing.NORTH);

		// define spawn point - Tetrimino have a defined starting point which should be placed on 5:21
		int spawnrow    = 17;// SKYLINE; // SKYLINE = 20
		int spawncolumn = 4; // 5 when 1..10 

		// shift for starting point defined in Tetrimino
		int startPointX = spawncolumn - next.get_startPoint()[1]; 
		int startPointy = spawnrow - (tMatrix.length-1-next.get_startPoint()[0]);

		// loop through the Tetrimino matrix and write to foreground 
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {

				int yi_reversed = tMatrix.length-1 - yi; // the Tetrimino matrix is reverse to the foreground matrix

				// check for collision
				if (_backgroundMatrix[startPointX+xi][startPointy+yi_reversed] != TetrisColor.EMPTY) {
					return true; // collision
				}

				// place color of Tetrimino on foreground matrix or set empty. 
				_foregroundMatrix[startPointX+xi][startPointy+yi_reversed] 
						= tMatrix[yi][xi] == 1 ? next.getColor() : TetrisColor.EMPTY;

			}
		}
		return false; // no collision
	}

	/**
	 * Move the Tetrimino down one cell and checks if it has landed on a surface.<br/>
	 * Throws exception if collision.
	 * @return if landed on surface
	 * @throws RuntimeException if collision occurs
	 */
	public boolean moveDown() {

		// remember the cells we want to move after we have checked all cells if they can move
		// Max 4 cells should occupied as all Tetriminos have for cells
		// 2 values [0] for x, [1] for y
		// IMPORTANT: the order in this array determines the order of the actual move. Make
		// sure that moving the cells does not overwrite other Tetrimino cells
		// control the order via the loop adding cells.
		int[][] markedForMove = new int[4][2];
		int cellNumber = 0;

		// we do not know where the Tetrimino is so we scan the matrix and mark occupied cell for move down
		// loop through the Tetrimino matrix 
		for (int yi = 0; yi < _playfieldHeight; yi++) {
			for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
				if (_foregroundMatrix[xi][yi] != TetrisColor.EMPTY) { // found occupied cell
					if (canMoveDown(xi,yi)) { // check for collision 
						markedForMove[cellNumber][0] = xi;
						markedForMove[cellNumber][1] = yi;
						cellNumber++;
					} else { // on surface - move would be collision
						return true;
					}
				}
			}
		}

		// actual move
		for(int i=0;i<4;i++) {
			int x = markedForMove[i][0];
			int y = markedForMove[i][1];
			if (y!=0) { // only when a useful value is in y
				TetrisColor temp = _foregroundMatrix[x][y];
				_foregroundMatrix[x][y] = TetrisColor.EMPTY;
				_foregroundMatrix[x][y-1] = temp;
			}
		}
		return false;
	}

	/**
	 * Checks if cell below is free to move into.
	 * @param xi
	 * @param yi
	 * @return true if free, false if blocked or edge
	 */
	private boolean canMoveDown(int xi, int yi) {
		if (yi<=0) return false; // last row
		if (_backgroundMatrix[xi][yi-1] != TetrisColor.EMPTY) { // check cell below
			return false;
		}
		return true;
	}

	public boolean moveLeft() {

		// remember the cells we want to move after we have checked all cells if they can move
		// Max 4 cells should occupied as all Tetriminos have for cells
		// 2 values [0] for x, [1] for y
		// IMPORTANT: the order in this array determines the order of the actual move. Make
		// sure that moving the cells does not overwrite other Tetrimino cells
		// control the order via the loop adding cells.
		int[][] markedForMove = new int[4][2];
		int cellNumber = 0;

		// we do not know where the Tetrimino is so we scan the matrix and mark occupied cell for move down
		// loop through the Tetrimino matrix 
		for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
			for (int yi = 0; yi < _playfieldHeight; yi++) {
				if (_foregroundMatrix[xi][yi] != TetrisColor.EMPTY) { // found occupied cell
					if (canMoveLeft(xi,yi)) { // check for collision 
						markedForMove[cellNumber][0] = xi;
						markedForMove[cellNumber][1] = yi;
						cellNumber++;
					} else { // on surface - move would be collision
						return true;
					}
				}
			}
		}

		// actual move
		for(int i=0;i<4;i++) {
			int x = markedForMove[i][0];
			int y = markedForMove[i][1];
			if (x!=0) { // only when a useful value is in x
				TetrisColor temp = _foregroundMatrix[x][y];
				_foregroundMatrix[x][y] = TetrisColor.EMPTY;
				_foregroundMatrix[x-1][y] = temp;
			}
		}
		return false;

	}

	private boolean canMoveLeft(int xi, int yi) {
		if (xi<=0) return false; // most left column
		if (_backgroundMatrix[xi-1][yi] != TetrisColor.EMPTY) { // check cell below
			return false;
		}
		return true;
	}

	public boolean moveRight() {

		// remember the cells we want to move after we have checked all cells if they can move
		// Max 4 cells should occupied as all Tetriminos have for cells
		// 2 values [0] for x, [1] for y
		// IMPORTANT: the order in this array determines the order of the actual move. Make
		// sure that moving the cells does not overwrite other Tetrimino cells
		// control the order via the loop adding cells.
		int[][] markedForMove = new int[4][2];
		int cellNumber = 0;

		// we do not know where the Tetrimino is so we scan the matrix and mark occupied cell for move down
		// loop through the Tetrimino matrix 
		for (int xi = PLAYFIELD_WIDTH-1; xi >= 0; xi--) {
			for (int yi = 0; yi < _playfieldHeight; yi++) {
				if (_foregroundMatrix[xi][yi] != TetrisColor.EMPTY) { // found occupied cell
					if (canMoveRight(xi,yi)) { // check for collision 
						markedForMove[cellNumber][0] = xi;
						markedForMove[cellNumber][1] = yi;
						cellNumber++;
					} else { // on surface - move would be collision
						return true;
					}
				}
			}
		}

		// actual move
		for(int i=0;i<4;i++) {
			int x = markedForMove[i][0];
			int y = markedForMove[i][1];
			if (x!=PLAYFIELD_WIDTH-1) { // only when a useful value is in x
				TetrisColor temp = _foregroundMatrix[x][y];
				_foregroundMatrix[x][y] = TetrisColor.EMPTY;
				_foregroundMatrix[x+1][y] = temp;
			}
		}
		return false;
	}

	private boolean canMoveRight(int xi, int yi) {
		if (xi>=PLAYFIELD_WIDTH-1) return false; // most right column
		if (_backgroundMatrix[xi+1][yi] != TetrisColor.EMPTY) { // check cell below
			return false;
		}
		return true;
	}
	
	public void turnRight() {

	}

	public void turnLeft() {

	}

	/**
	 * 
	 */
	private void clearMatrix(TetrisColor[][] m) {
		// iterate through all cells and initialize with zero
		for (int yi = 0; yi < _playfieldHeight; yi++) {
			for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
				m[xi][yi] = TetrisColor.EMPTY;
			}
		}
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
	public TetrisColor getBackgroundColor(int xi, int yi) {
		return _backgroundMatrix[xi][yi];
	}

	/**
	 * @param xi
	 * @param yi
	 * @return
	 */
	public TetrisColor getForegroundColor(int xi, int yi) {
		return _foregroundMatrix[xi][yi];
	}

	private void debugPrintMatrix(TetrisColor[][] m) {
		for (int yi = _playfieldHeight-1; yi >= 0; yi--) {
			for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
				System.out.print(m[xi][yi].ordinal()+" ");
			}
			System.out.println();
		}
	}



}
