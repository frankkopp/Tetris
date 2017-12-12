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

import fko.tetris.Playfield;
import fko.tetris.TetrisColor;
import fko.tetris.tetriminos.Tetrimino;
import fko.tetris.util.Coordinates;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * 
 */
public class PlayfieldPane extends Pane {

	private static final double HEIGHT = 600;
	private static final double WIDTH = 300;

	private static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final Color GRID_COLOR = Color.LIGHTGRAY;
	private static final Color FRAME_COLOR = Color.LIGHTGRAY;

	private Playfield _playField; // handle to the playField to draw
	
	private TetrisGUI_Controller _controller; // handle to the UI controller to access options

	// helper for an efficient draw()
	private Line[] _hlines = new Line[Playfield.SKYLINE];
	private Line[] _vlines = new Line[Playfield.PLAYFIELD_WIDTH];
	private Rectangle[] _block = new Rectangle[(Playfield.BUFFERZONE+Playfield.SKYLINE)*Playfield.PLAYFIELD_WIDTH];
	private Rectangle[] _tblock = new Rectangle[16];

	/**
	 * Initialize the playfieldPanel
	 * @param tetrisGUI_Controller 
	 */
	public PlayfieldPane(TetrisGUI_Controller tetrisGUI_Controller) {
		super();

		this._controller = tetrisGUI_Controller;
		
		// set up the pane
		this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR,null,null)));
		// set size
		this.setMinWidth(WIDTH);
		this.setMinHeight(HEIGHT);
		this.setMaxWidth(WIDTH);
		this.setMaxHeight(HEIGHT);


		// prepare some elements and keep them to reference them
		for (int i=0; i<Playfield.SKYLINE; i++) {
			_hlines[i] = new Line();
		}
		for (int i=0; i<Playfield.PLAYFIELD_WIDTH; i++) {
			_vlines[i] = new Line();
		}
		for (int i=0; i<(Playfield.BUFFERZONE+Playfield.SKYLINE)*Playfield.PLAYFIELD_WIDTH; i++) {
			_block[i] = new Rectangle();
		}
		for (int i=0; i<16; i++) {
			_tblock[i] = new Rectangle();
		}

		// draw initial board
		draw();

	}

	/**
	 * @param _playField the _playField to set
	 */
	public void setPlayField(Playfield _playField) {
		// ToDo: Maybe a copy is needed
		this._playField = _playField;
	}

	/**
	 * Draws all elements in the panel. Lines and Tetriminos
	 */
	public void draw() {
		if (_playField == null) _playField=new Playfield(); // draw default field if no other playField is defined
		draw(_playField);
	}

	/**
	 * @param _playField2
	 */
	private void draw(Playfield playField) {

		// clear the node to redraw everything
		this.getChildren().clear();

		// draw frame
		Rectangle rectangle = new Rectangle();
		rectangle.setStroke(FRAME_COLOR);
		// here we position the rectangle (this depends on pane size as well)
		rectangle.setX(0);
		rectangle.setY(0);
		// here we bind rectangle size to pane size
		rectangle.setHeight(HEIGHT);
		rectangle.setWidth(WIDTH);
		this.getChildren().add(rectangle);

		// draw lines
		for (int c=1; c<Playfield.PLAYFIELD_WIDTH; c++) {
			// vertical lines
			double w = (WIDTH/Playfield.PLAYFIELD_WIDTH)*c;
			Line v_line =_vlines[c-1];
			v_line.setStroke(GRID_COLOR);
			v_line.setStartX(w);
			v_line.setStartY(0);
			v_line.setEndX(w);
			v_line.setEndY(HEIGHT);
			this.getChildren().add(v_line);	
		}
		for (int r=1; r<Playfield.SKYLINE; r++) {
			// horizontal lines
			double h = (HEIGHT/Playfield.SKYLINE)*r;
			Line h_line =_hlines[r-1];
			h_line.setStroke(GRID_COLOR);
			h_line.setStartX(0);
			h_line.setStartY(h);
			h_line.setEndX(WIDTH);
			h_line.setEndY(h);
			this.getChildren().add(h_line);
		}

		final double h = (HEIGHT/Playfield.SKYLINE);
		final double w = (WIDTH/Playfield.PLAYFIELD_WIDTH);

		// draw background cells
		// iterate through all cells a initialize with zero
		int cr = 0; // counter for the prepared rectangle objects
		for (int yi = 0; yi < Playfield.SKYLINE+1; yi++) { // we only draw the visible part therefore only to SKYLINE
			for (int xi = 0; xi < Playfield.PLAYFIELD_WIDTH; xi++) {
				final TetrisColor bc = _playField.getBackgroundColor(xi,yi);
				Color color = bc.toColor();
				if (bc != TetrisColor.EMPTY) {
					color = bc.toColor(); 
				} 
				final double offset_h = HEIGHT -(h*(yi+1)); // height is measured top down were as our playField is buttom up 
				final double offset_w = w * xi;
				Rectangle block = _block[cr++];
				block.setFill(color);
				block.setX(offset_w+1); // +1 to not overdraw the lines
				block.setY(offset_h+1);
				block.setWidth(w-1); // -1 to not overdraw the lines
				block.setHeight(h-1);
				this.getChildren().add(block);
			}
		}        

		// draw current Tetrimino
		// will draw over background so collision check needs to be done in model
		final Tetrimino t = playField.getCurrentTetrimino();
		if (t==null)
			return; // if now game is running there are no Tetriminos

		final int[][] tMatrix = t.getMatrix(t.getCurrentOrientation());
		final Coordinates c = t.getCurrentPosition();

		// set the max height we want to see Tetriminos
		int visibleHeight = Playfield.SKYLINE+1;
		if (!_controller.peekOption.isSelected()) {
			visibleHeight = Playfield.SKYLINE;
		} 
		
		cr = 0; // counter for the prepared rectangle objects
		
		// loop through the Tetrimino matrix
		for (int yi = 0; yi < tMatrix.length; yi++) {
			for (int xi = 0; xi < tMatrix[yi].length; xi++) {
				if (tMatrix[yi][xi] == 1) { // only draw when 1

					int bx = c.x + xi;
					int by = c.y - yi;
					
					// if not visible skip drawing
					if (by > visibleHeight) break;

					double offset_h = HEIGHT -(h*by); // height is measured top down were as our playField is buttom up 
					double offset_w = w * bx;
					Rectangle block = _tblock[cr++];
					block.setFill(t.getColor().toColor());
					block.setX(offset_w+1); // +1 to not overdraw the lines
					block.setY(offset_h+1);
					block.setWidth(w-1); // -1 to not overdraw the lines
					block.setHeight(h-1);
					this.getChildren().add(block);
				}
			}
		}
		
		if (_controller.ghostPieceOption.isSelected()) {
			Tetrimino ghost = t.clone();
			
			// drop the ghost as far as possible
			
			
		}


	}


}
