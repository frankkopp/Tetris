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
import fko.tetris.util.Coordinates;
import fko.tetris.util.SimpleIntList;

/**
 * This represent the internal playfield which basically consists of a background matrices of 10 columns and 40 rows
 * and a current Tetrimino.
 * The matrix coordinates start at 0,0 at the lower left cell and 9,39 with the upper right cell.
 * The background matrix contains all settled Tetriminos.
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
	// All Tetriminos which are locked in place are merged into the background as Minos.
	// A zero representing an empty field, a non-zero an occupied field.
	// Non-zeros also determine the color (defined in Tetrimino class)
	private TetrisColor[][] _backgroundMatrix;

	// The current Tetrimino
	private Tetrimino _currentTetrimino;
	
	// convenience field for SKYLINE+BUFFERZONE
	private int _playfieldHeight = SKYLINE + BUFFERZONE;

	// lines marked for clearing - call clearMarkedLines() to delete these lines
	private SimpleIntList _markedLineClears;

	/**
	 * Generates a new Playfield with default width and height
	 * @param _backgroundMatrix
	 */
	public Playfield() {
		this._backgroundMatrix = new TetrisColor[PLAYFIELD_WIDTH][_playfieldHeight];
		clearMatrix(_backgroundMatrix);
		
		// DEBUG
//		_backgroundMatrix[0][10] = TetrisColor.LBLUE;
//		_backgroundMatrix[1][10] = TetrisColor.LBLUE;
//		_backgroundMatrix[2][10] = TetrisColor.LBLUE;
//		_backgroundMatrix[3][10] = TetrisColor.LBLUE;
//		
//		_backgroundMatrix[6][19] = TetrisColor.LBLUE;
		
//		_backgroundMatrix[0][0] = TetrisColor.BLUE;
//		_backgroundMatrix[1][0] = TetrisColor.BLUE;
//		_backgroundMatrix[2][0] = TetrisColor.BLUE;
//		_backgroundMatrix[3][0] = TetrisColor.BLUE;
//		_backgroundMatrix[4][0] = TetrisColor.PURPLE;
//		_backgroundMatrix[5][0] = TetrisColor.PURPLE;
//		_backgroundMatrix[6][0] = TetrisColor.PURPLE;
//		_backgroundMatrix[7][0] = TetrisColor.YELLOW;
//		_backgroundMatrix[8][0] = TetrisColor.YELLOW;
//		_backgroundMatrix[9][0] = TetrisColor.YELLOW;
//		_backgroundMatrix[0][1] = TetrisColor.BLUE;
//		_backgroundMatrix[1][1] = TetrisColor.BLUE;
//		_backgroundMatrix[2][1] = TetrisColor.BLUE;
//		_backgroundMatrix[3][1] = TetrisColor.BLUE;
//		_backgroundMatrix[4][1] = TetrisColor.PURPLE;
//		_backgroundMatrix[5][1] = TetrisColor.PURPLE;
//		_backgroundMatrix[6][1] = TetrisColor.PURPLE;
//		_backgroundMatrix[7][1] = TetrisColor.YELLOW;
//		_backgroundMatrix[8][1] = TetrisColor.YELLOW;
//		_backgroundMatrix[9][1] = TetrisColor.YELLOW;
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

		int[][] tMatrix = next.getMatrix(Facing.NORTH);

		// define spawn point - Tetrimino have a defined starting point which should be placed on 5:21
		Coordinates startPoint = next.getCurrentPosition();

		//System.out.println(next.getShape().toString());
		
		// loop through the Tetrimino matrix and check for collosion 
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				//System.out.print(tMatrix[yi][xi]+" ");
				// check for collision
				if (tMatrix[yi][xi] == 1 
						&& _backgroundMatrix[startPoint.x+xi][startPoint.y-yi-1] != TetrisColor.EMPTY) {
					//System.out.println("COLLOSION: "+(startPoint.x+xi)+":"+(startPoint.y-yi-1)+" "+next.getShape());
					return true; // collision
				}
			}
			//System.out.println();
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
	 * Check if aTetrimino can move down one cell<br/>
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
	private void doMoveDown(Tetrimino tetrimino) {
		tetrimino.getCurrentPosition().y -= 1;
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
		if (!canMoveLeft(direction, tetrimino)) return true; // check if move is possible or return true for collision 
		doMoveSideways(direction, tetrimino); // do the actual move
		return false;
	}
	
	/**
	 * Check of the Tetrimino can move left one cell<br/>
	 * @param direction 
	 * @return true if move is possible, false if landed on surface
	 */
	public boolean canMoveLeft(int direction, Tetrimino tetrimino) {
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
						if (tetrimino.getCurrentPosition().x+xi  >= PLAYFIELD_WIDTH-1) {
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
	private void doMoveSideways(int direction, Tetrimino tetrimino) {
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
			tetrimino.turn(direction);
		}
		return false;
	}

	/**
	 * @param tmp 
	 * @param direction
	 * @return
	 */
	private boolean canTurn(Tetrimino tmp, int direction) {
		// turn the Tetrimino
		tmp.turn(direction);
		
		// check for collisions
		int[][] tMatrix = tmp.getMatrix(tmp.getCurrentOrientation());
		
		// loop through the Tetrimino matrix and check for collisions
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				// check for collision in cell left 
				if (tMatrix[yi][xi] == 1) { // check for all filled parts of the matrix
					if (tmp.getCurrentPosition().x+xi  < 0) { // outside left wall
						return false;
					}
					if (tmp.getCurrentPosition().x+xi  > PLAYFIELD_WIDTH-1) { // outside right wall
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
	 * Merges the current Tetrimino in play into the background
	 */
	public void merge() {
		merge(_currentTetrimino);
		_currentTetrimino = null; // erase the current tetrimino
	}

	/**
	 * Merges a Tetrimino in play into the background
	 */
	public void merge(Tetrimino tetrimino) {
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
	public int markLinesToBeCleared() {
		// loop through the Tetrimino matrix and check for full lines
		_markedLineClears = new SimpleIntList(_playfieldHeight);
		for (int yi = 0; yi < _playfieldHeight; yi++) {
			boolean foundHole = false;
			for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
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
	public int clearMarkedLines() {
		// iterate upwards through the marked lines and shift all Minos above the lines one down
		// as we created the list upwards the list should be correctly sorted
		int clearedCounter = 0; // as we delete rows the rows of minos shift down and the index in the 
								// _markedLinesCleares need to be decreased by 1
		for (int i : _markedLineClears) {
			for (int y=i-clearedCounter;y<_playfieldHeight-1;y++) {
				for(int x=0;x<PLAYFIELD_WIDTH;x++) {
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

	/*
	 * initializes all fields with EMPTY 
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
	 * @return the _currentTetrimino
	 */
	public Tetrimino getCurrentTetrimino() {
		return _currentTetrimino;
	}

	@SuppressWarnings("unused")
	private void debugPrintMatrix(TetrisColor[][] m) {
		for (int yi = _playfieldHeight-1; yi >= 0; yi--) {
			for (int xi = 0; xi < PLAYFIELD_WIDTH; xi++) {
				System.out.print(m[xi][yi].ordinal()+" ");
			}
			System.out.println();
		}
	}



}
