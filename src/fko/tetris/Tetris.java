/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fko.tetris;

import fko.tetris.ui.TetrisGUI;

/**
 * Main class for Tetris app.
 */
public class Tetris {

	/**
	 * The handle to the user interface class
	 */
	public static TetrisGUI _ui;
	
	/**
	 * The handle to the model
	 */
	public static TetrisGame _tetrisModel;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		_ui = new TetrisGUI();
		_ui.waitForUI();
		
	}
	
    /**
     * Clean up and exit the application
     */
    public static void exitTetris() {
    	exitTetris(0);
    }

    /**
     * Clean up and exit the application
     */
    private static void exitTetris(int returnCode) {
        // nothing to clean up yet
        System.exit(returnCode);
    }
	

}
