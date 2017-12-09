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

import fko.tetris.NextQueue;
import fko.tetris.tetriminos.Tetrimino;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 
 */
public class NextQueuePane extends Pane {
	private static final double HEIGHT = 400;
	private static final double WIDTH = 150;

	private static final Color BACKGROUND_COLOR = Color.DARKGRAY;
	
	private NextQueue _nextQueue; // handle to NextQueue object
	
	/**
	 * Initialize the nextQueuedPanel
	 */
	public NextQueuePane() {
		super();
		
		// set up the pane
        this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR,null,null)));
        // set size
        this.setMinWidth(WIDTH);
        this.setMinHeight(HEIGHT);
        this.setMaxWidth(WIDTH);
        this.setMaxHeight(HEIGHT);
        
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
		
		ListIterator<Tetrimino> iterator = _nextQueue.getListIterator();
		
		int position = 0; // draw Tetriominos from top downwards
		while (iterator.hasNext()) {
			draw(position, iterator.next());
			position++;
		}
		
	}

	/**
	 * Draw a single Tetrimino at specified position 
	 * @param position
	 * @param next
	 */
	private void draw(int position, Tetrimino next) {

		int[][] tMatrix = next.getMatrix(Tetrimino.Facing.NORTH); // get north facing matrix
		
		Color color = next.getColor().toColor();
		
		double h = (WIDTH/6); // height and width of cells based on pane width
		double start_x = h;	// start with a offset of one cell width
		double start_y = h + 5*h*position; 	// start with an offset of one cell height
											// position draws the n-th cell lower
		
		for(int y=0; y<tMatrix.length;y++) {
			for(int x=0; x<tMatrix[y].length;x++) {
				// simply draw all cells but fill with background color when cell not part of Terinmino
				Color c = tMatrix[y][x] == 1 ? color : BACKGROUND_COLOR;
				double relX = x*h;
				double relY = y*h;
				Rectangle block = new Rectangle();
				block.setFill(c);
				block.setX(start_x+relX);
				block.setY(start_y+relY);
				block.setWidth(h); 
				block.setHeight(h);
				this.getChildren().add(block);
			}
		}
	}
	
	
}
