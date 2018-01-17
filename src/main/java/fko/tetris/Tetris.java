/*
 * The MIT License (MIT)
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2017 Frank Kopp
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
import javafx.application.Application;

/**
 * Main class for Tetris app. Starts up the JavaFX ui and exits.
 * 
 * TODO: Use resource files for internationalization
 */
public class Tetris {

	// VERSION
	public static final String VERSION = "1.2.2"; 

	/**
	 * Main starts the JavaFX Application and waits until this Applications ends. 
	 * @param args - not yet used
	 */
	public static void main(String[] args) {
		Application.launch(TetrisGUI.class,args);
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

	/**
	 * Called when there is an unexpected unrecoverable error.<br/>
	 * Prints a stack trace together with a provided message.<br/>
	 * Terminates with <tt>exit(1)</tt>.
	 * @param message to be displayed with the exception message
	 */
	public static void fatalError(String message) {
		Exception e = new Exception(message);
		e.printStackTrace();
		exitTetris(1);
	}

	/**
	 * Called when there is an unexpected but recoverable error.<br/>
	 * Prints a stack trace together with a provided message.<br/>
	 * @param message to be displayed with the exception message
	 */
	public static void criticalError(String message) {
		Exception e = new Exception(message);
		e.printStackTrace();
	}

	/**
	 * Called when there is an unexpected minor error.<br/>
	 * Prints a provided message.<br/>
	 * @param message to be displayed
	 */
	public static void minorError(String message) {
		System.err.println(message);
	}


}
