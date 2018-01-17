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
package fko.tetris.ui;

import java.util.ListIterator;

import fko.tetris.game.NextQueue;
import fko.tetris.tetriminos.Tetrimino;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The pane presenting the Next Queue for Tetriminos.<br/>
 * 
 */
public class NextQueuePane extends Pane {
	private static final Color BACKGROUND_COLOR = Color.DARKGRAY;

	private NextQueue _nextQueue; // handle to NextQueue object
	private double _offSetY;
	private double _minoHeight;

	/**
	 * Initialize the nextQueuedPanel
	 */
	public NextQueuePane() {
		super();

		// set up the pane
		this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR,null,null)));

		// draw initial queue
		draw();
	}

	/**
	 * @param _nextQueue the _nextQueue to set
	 */
	public void setNextQueue(NextQueue _nextQueue) {
		this._nextQueue = _nextQueue;
	}

	/**
	 * 
	 */
	public void draw() {
		if (_nextQueue == null) {
			// clear the node to redraw everything
			this.getChildren().clear();
			return;
		}
		draw(_nextQueue);
	}

	/**
	 * Draw all Tetriminos in NextQueue
	 * @param _nextQueue
	 */
	private void draw(NextQueue nextQueue) {

		// clear the node to redraw everything
		this.getChildren().clear();
		
		_minoHeight = (this.getWidth()/6); // height and width of cells based on pane width
		_offSetY = _minoHeight; // start with one mino height below the top

		ListIterator<Tetrimino> iterator = _nextQueue.getListIterator();

		while (iterator.hasNext()) {
			draw(iterator.next());
			_offSetY += _minoHeight;
		}

	}

	/**
	 * Draw a single Tetrimino at specified position 
	 * @param next
	 */
	private void draw(Tetrimino next) {

		int[][] tMatrix = next.getMatrix(Tetrimino.Facing.NORTH); // get north facing matrix

		Color color = next.getColor().toColor();

		// determine were to draw horizontally
		double start_x;
		switch (next.toString()) {
		case "O": {
			start_x = this.getWidth()/2 - _minoHeight; // 1 mino left of middle
			break;
		}
		case "I": {
			start_x = this.getWidth()/2 - (2*_minoHeight); // 2 minos left of middle
			break;
		}
		default: {
			start_x = this.getWidth()/2 - (_minoHeight+(_minoHeight/2)); // 1.5 minos left of middle
			break;
		}
		}
		
		for(int y=0; y<tMatrix.length;y++) {
			boolean hadMino = false;
			for(int x=0; x<tMatrix[y].length;x++) {
				if (tMatrix[y][x] == 1 && _offSetY < this.getHeight() - _minoHeight) {
					hadMino=true;
					// at least one mino in this row
					double relX = x*_minoHeight;
					Rectangle block = new Rectangle();
					block.setFill(color);
					block.setArcHeight(5.0);
					block.setArcWidth(5.0);
					block.setX(start_x+relX);
					block.setY(_offSetY);
					block.setWidth(_minoHeight); 
					block.setHeight(_minoHeight);
					this.getChildren().add(block);
				}
			}
			if (hadMino) {
				_offSetY += _minoHeight; // remember position for next Tetrimino
			}
		}
	}


}
