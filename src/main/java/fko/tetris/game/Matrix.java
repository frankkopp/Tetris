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

import fko.tetris.tetriminos.Tetrimino;
import fko.tetris.util.Coordinates;
import fko.tetris.util.SimpleIntList;

/**
 * This represent the internal playfield which basically consists of a background matrices of 10 columns and 40 rows
 * and a current Tetrimino.
 * The matrix coordinates start at 0,0 at the lower left cell and 9,39 with the upper right cell.
 * The background matrix contains all settled Tetriminos.
 * 
 * From Tetris Guideline
 * Playfield is 10:22, where rows above 20 are hidden or obstructed by the field frame to trick the player into thinking
 * it's 10:20. In 2002 Guideline, it could be at least 22 height. 
 */
public class Matrix {

	/**
	 * The line which separates he visible area from the hidden buffer zone
	 */
	public static final int SKYLINE = 20;

	/**
	 * The height of the buffer zone.
	 * Together with the SKYLINE it determines the height of the overall playfield matrix
	 */
	public static final int BUFFERZONE = 2;

	/**
	 * Playfield matrix width
	 */
	public static final int MATRIX_WIDTH = 10;

	// convenience field for SKYLINE+BUFFERZONE
	public static final int MATRIX_HEIGHT = SKYLINE + BUFFERZONE;

	// The internal data structure for the background matrix.
	// All Tetriminos which are locked in place are merged into the background as Minos.
	private TetrisColor[][] _backgroundMatrix;

	// The current Tetrimino
	private Tetrimino _currentTetrimino;

	// The last Tetrimino before the last merge
	private Tetrimino _lastTetrimino;

	// lines marked for clearing - call clearMarkedLines() to delete these lines
	private SimpleIntList _markedLineClears;

	/**
	 * Generates a new Playfield with default width and height
	 * @param _backgroundMatrix
	 */
	public Matrix() {
		this._backgroundMatrix = new TetrisColor[MATRIX_WIDTH][MATRIX_HEIGHT];
		clearMatrix(_backgroundMatrix);
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
	public synchronized boolean spawn(Tetrimino next) {
		int[][] tMatrix = next.getMatrix(next.getCurrentOrientation());
		// define spawn point - Tetrimino have a defined starting point which should be placed on 5:21
		Coordinates startPoint = next.getCurrentPosition();
		// loop through the Tetrimino matrix and check for collision 
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				// check for collision
				if (tMatrix[yi][xi] == 1 
						&& _backgroundMatrix[startPoint.x+xi][startPoint.y-yi-1] != TetrisColor.EMPTY) {
					return true; // collision
				}
			}
		}
		// no collision so set this as new current Tetrimino
		_currentTetrimino = next;
		return false; // no collision
	}

	/**
	 * Move the current Tetrimino down one cell and checks if it has landed on a surface.<br/>
	 * @return true if landed on surface
	 */
	public boolean moveDown() {
		return moveDown(_currentTetrimino);
	}
	
	/**
	 * Move a Tetrimino down one cell and checks if it has landed on a surface.<br/>
	 * @return true if landed on surface
	 */
	public boolean moveDown(Tetrimino tetrimino) {
		if (!canMoveDown(tetrimino)) return true; // check if move is possible or return true for collision 
		doMoveDown(tetrimino); // do the actual move
		return false;
	}

	/**
	 * Check if the current Tetrimino can move down
	 * @return
	 */
	public boolean canMoveDown() {
		return canMoveDown(_currentTetrimino);
	}

	/**
	 * Check if a Tetrimino can move down one cell<br/>
	 * @param _currentTetrimino2 
	 * @return true if move is possible, false if landed on surface
	 */
	public boolean canMoveDown(Tetrimino tetrimino) {
		int[][] tMatrix = tetrimino.getMatrix(tetrimino.getCurrentOrientation());
		// loop through the Tetrimino matrix and check for surface below 
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				// check for collision in cell below => -2 instead of -1
				if (tMatrix[yi][xi] == 1 
						&& (tetrimino.getCurrentPosition().y-yi-2 < 0 
						|| _backgroundMatrix[tetrimino.getCurrentPosition().x+xi][tetrimino.getCurrentPosition().y-yi-2] != TetrisColor.EMPTY)) {
					return false; // collision
				}
			}
		}
		return true; // no collisions so we can move down
	}

	/*
	 * actually commit the move
	 */
	private synchronized void doMoveDown(Tetrimino tetrimino) {
		tetrimino.getCurrentPosition().y -= 1;
	}

	/**
	 * Drops the Tetrimino and returns the lines dropped.
	 * @return number of lines the Tetrimino was dropped
	 */
	public int drop() {
		int counter = 0;
		while (!moveDown()) {
			counter++;
		}
		return counter;
	}

	/**
	 * Move the current Tetrimino left (-1) or right (1) one cell, If blocked does not move and returns true<br/>
	 * @return true if surface on the left or right
	 * @param direction
	 */
	public boolean moveSideway(int direction) {
		return moveSideway(direction, _currentTetrimino);
	}

	/**
	 * Move a Tetrimino left (-1) or right (1) one cell, If blocked does not move and returns true<br/>
	 * @param direction
	 * @param tetrimino
 	 * @return true if surface on the left or right
	 */
	public boolean moveSideway(int direction, Tetrimino tetrimino) {
		if (!canMoveSideways(direction, tetrimino)) return true; // check if move is possible or return true for collision 
		doMoveSideways(direction, tetrimino); // do the actual move
		return false;
	}
	
	/**
	 * Check of the Tetrimino can move left or right one cell<br/>
	 * @param direction 
	 * @return true if move is possible, false if landed on surface
	 */
	public boolean canMoveSideways(int direction, Tetrimino tetrimino) {
		int[][] tMatrix = tetrimino.getMatrix(tetrimino.getCurrentOrientation());
		// loop through the Tetrimino matrix and check for surface on the left
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				// check for collision in cell left 
				if (tMatrix[yi][xi] == 1) { // check for all filled parts of the matrix
					if (direction < 0) { // left wall
						if (tetrimino.getCurrentPosition().x+xi  <= 0) {
							return false;
						}
					} else if (direction > 0) { // right wall
						if (tetrimino.getCurrentPosition().x+xi  >= MATRIX_WIDTH-1) {
							return false;
						}
					}
					// other piece is blocking the way
					if (_backgroundMatrix[tetrimino.getCurrentPosition().x+xi+direction][tetrimino.getCurrentPosition().y-yi-1] != TetrisColor.EMPTY) {
						return false;
					}
				}
			}
		}
		return true; // no collisions so we can move down
	}
	
	/*
	 * actually commit the move
	 */
	private synchronized void doMoveSideways(int direction, Tetrimino tetrimino) {
		tetrimino.getCurrentPosition().x += direction;
	}
	
	/**
	 * Turns a Tetrimino after collision checks.<br/> 
	 * Uses Classic Rotation System. 
	 * @param direction
	 * @return true if turn would cause collision - Tetrimino is then not turned
	 */
	public boolean turnMove(int direction) {
		return turnMove(direction, _currentTetrimino);
	}

	/**
	 * Turns a Tetrimino after collision checks.<br/> 
	 * Uses Classic Rotation System. 
	 * TODO: Implement Super Rotation System
	 * @param direction
	 * @param tetrimino
	 * @return true if turn would cause collision - Tetrimino is then not turned
	 */
	public boolean turnMove(int direction, Tetrimino tetrimino) {
		// first create a temp copy of the current Tetrimino we can test turns with
		Tetrimino tmp = tetrimino.clone();
		if (canTurn(tmp, direction)) {
			doTurn(direction, tetrimino);
		}
		return false;
	}

	/**
	 * @param tmp 
	 * @param direction
	 * @return
	 */
	private boolean canTurn(Tetrimino tmp, int direction) {
		doTurn(direction, tmp);
		// check for collisions
		int[][] tMatrix = tmp.getMatrix(tmp.getCurrentOrientation());
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				// check for collision in cell left 
				if (tMatrix[yi][xi] == 1) { // check for all filled parts of the matrix
					if (tmp.getCurrentPosition().x+xi  < 0) { // outside left wall
						return false;
					}
					if (tmp.getCurrentPosition().x+xi  > MATRIX_WIDTH-1) { // outside right wall
						return false;
					}
					if (tmp.getCurrentPosition().y-yi-1  < 0) { // below base line
						return false;
					}
					// other piece is blocking the cell
					if (_backgroundMatrix[tmp.getCurrentPosition().x+xi][tmp.getCurrentPosition().y-yi-1] != TetrisColor.EMPTY) {
						return false;
					}
				}
			}
		}
		return true; // no collisions so we can move down
	}

	/**
	 * @param direction
	 * @param tetrimino
	 */
	private synchronized void doTurn(int direction, Tetrimino tetrimino) {
		tetrimino.turn(direction);
	}

	/**
	 * Merges the current Tetrimino in play into the background
	 */
	public void merge() {
		merge(_currentTetrimino);
		_lastTetrimino = _currentTetrimino; // save the last 
		_currentTetrimino = null; // erase the current tetrimino
	}

	/**
	 * Merges a Tetrimino in play into the background
	 */
	public synchronized void merge(Tetrimino tetrimino) {
		int[][] tMatrix = tetrimino.getMatrix(tetrimino.getCurrentOrientation());
		// loop through the Tetrimino matrix and check for surface on the left
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				// check for collision in cell left 
				if (tMatrix[yi][xi] == 1) { // check for all filled parts of the matrix
					assert _backgroundMatrix[tetrimino.getCurrentPosition().x+xi][tetrimino.getCurrentPosition().y-yi-1] == TetrisColor.EMPTY;
					// write to background
					_backgroundMatrix[tetrimino.getCurrentPosition().x+xi][tetrimino.getCurrentPosition().y-yi-1] = tetrimino.getColor(); 
				}
			}
		}
	}

	/**
	 * This marks all lines which are full to be cleared later
	 * @return number of lines which have been marked
	 */
	public synchronized int markLinesToBeCleared() {
		// loop through the Tetrimino matrix and check for full lines
		_markedLineClears = new SimpleIntList(MATRIX_HEIGHT);
		for (int yi = 0; yi < MATRIX_HEIGHT; yi++) {
			boolean foundHole = false;
			for (int xi = 0; xi < MATRIX_WIDTH; xi++) {
				if (_backgroundMatrix[xi][yi] == TetrisColor.EMPTY) {
					foundHole = true;
					break;
				}
			}
			if (!foundHole) _markedLineClears.add(yi);
		}
		return _markedLineClears.size();
	}
	
	/**
	 * This actually deletes all lines which are marked to be cleared.
	 * @return number of lines which have been deleted
	 */
	public synchronized int clearMarkedLines() {
		// iterate upwards through the marked lines and shift all Minos above the lines one down
		// as we created the list upwards the list should be correctly sorted
		int clearedCounter = 0; // as we delete rows the rows of minos shift down and the index in the 
								// _markedLinesCleares need to be decreased by 1
		for (int i : _markedLineClears) {
			for (int y=i-clearedCounter;y<MATRIX_HEIGHT-1;y++) {
				for(int x=0;x<MATRIX_WIDTH;x++) {
					// copy the cell above to the current cell
					_backgroundMatrix[x][y] = _backgroundMatrix[x][y+1];
				}
			}
			clearedCounter++;
		}
		int counter = _markedLineClears.size();
		_markedLineClears.clear();
		return counter;		
	}

	/**
	 * Initializes all fields with EMPTY 
	 */
	private synchronized void clearMatrix(TetrisColor[][] m) {
		// iterate through all cells and initialize with zero
		for (int yi = 0; yi < MATRIX_HEIGHT; yi++) {
			for (int xi = 0; xi < MATRIX_WIDTH; xi++) {
				m[xi][yi] = TetrisColor.EMPTY;
			}
		}
	}

	/**
	 * Returns the TetrisColor of a specified cell.
	 * @param x - starts at 0 to width-1
	 * @param y - starts at 0 to height-1
	 * @return TetrisCOlor of cell
	 */
	public TetrisColor getCell(int x, int y) {
		return _backgroundMatrix[x][y];
	}

	/**
	 * @return matrix width
	 */
	public int getColumns() {
		return MATRIX_WIDTH;
	}

	/**
	 * @return number of rows visible (to skyline)
	 */
	public int getVisibleRows() {
		return SKYLINE;
	}

	/**
	 * @param xi
	 * @param yi
	 * @return returns the <code>TetrisColor</code> of the given cell
	 */
	public TetrisColor getBackgroundColor(int xi, int yi) {
		return _backgroundMatrix[xi][yi];
	}

	/**
	 * @return the currentTetrimino
	 */
	public Tetrimino getCurrentTetrimino() {
		return _currentTetrimino;
	}
	
	/**
	 * @return the Tetrimino before the last merge
	 */
	public Tetrimino getLastTetrimino() {
		return _lastTetrimino;
	}
	
	
	
	/** 
	 * Returns the matrix a string representation
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int yi = MATRIX_HEIGHT-1; yi >= 0; yi--) {
			for (int xi = 0; xi < MATRIX_WIDTH; xi++) {
				if (_backgroundMatrix[xi][yi] == TetrisColor.EMPTY) {
					sb.append("- ");
				} else {
					sb.append(_backgroundMatrix[xi][yi].ordinal()+" ");
				}
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	/**
	 * Prints the Matrix to System.out for debugging 
	 */
	public void debugPrintMatrix() {
		System.out.println(this.toString());
	}

	/**
	 * Deep copy of the Matrix. 
	 */
	@Override
	public synchronized Matrix clone() {
		Matrix newP = new Matrix();
		// copy the matrix
		TetrisColor[][] newM = new TetrisColor[MATRIX_WIDTH][MATRIX_HEIGHT];
		for(int x=0; x < _backgroundMatrix.length;x++) {
			System.arraycopy(_backgroundMatrix[x], 0, newM[x], 0, _backgroundMatrix[x].length);
		}
		newP._backgroundMatrix = newM;
		newP._currentTetrimino = _currentTetrimino == null ? null : _currentTetrimino.clone();
		newP._lastTetrimino = _lastTetrimino == null ? null : _lastTetrimino.clone();
		newP._markedLineClears = _markedLineClears == null ? null : _markedLineClears.clone();
		return newP;
	}
}
