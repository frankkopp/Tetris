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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The JavaFX Application class
 */
public class TetrisGUI extends Application {

	/**
	 * The main controller for this JavaFX application
	 */
	public static TetrisGUI_Controller _controller;

	private static TetrisGUI _instance = null; 	// The singleton instance of this class
	private static Stage _primaryStage; 			// The primary stage
	private BorderPane _root;					// the root pane for the gui 

	/**
	 * Creates a JavaFX UI instance. Is usually called by the Application.launch() method.<br>
	 * It is not possible to instantiate this more than once - throws RunTime Exception.
	 */
	public TetrisGUI() {
		if (_instance != null) // singleton pattern 
			throw new RuntimeException("It is not possible to instantiate the GUI more than once!");
		TetrisGUI._instance = this; // save instance handle in static class
	}

	/**
	 * Standard way to start a JavaFX application. Is called from Application.launch().<br>
	 */
	@Override
	public void start(Stage primaryStage) {

		TetrisGUI._primaryStage = primaryStage;
		_primaryStage.setTitle("Tetris by Frank Kopp (c)");

		try {

			// read FXML file and setup UI
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TetrisGUI.fxml"));
			_root = (BorderPane)fxmlLoader.load();
			_controller = fxmlLoader.getController();

			// Create the Scene based on the FXML root pane
			Scene scene = new Scene(_root,_root.getPrefWidth(),_root.getPrefHeight());

			// configure stage
			_primaryStage.setScene(scene);
			_primaryStage.setResizable(false);

			// get last window position and size from window state file
			double windowLocX = Double.parseDouble(
					TetrisGUI_Controller.getWindowState().getProperty("windowLocationX", "100"));
			double windowLocY = Double.parseDouble(
					TetrisGUI_Controller.getWindowState().getProperty("windowLocationY", "200"));
			//double windowSizeX = Double.parseDouble(
			//	TetrisGUI_Controller.getWindowState().getProperty("windowSizeX", "785"));
			//double windowSizeY = Double.parseDouble(
			//	TetrisGUI_Controller.getWindowState().getProperty("windowSizeY", "795"));

			// position and resize the window
			_primaryStage.setX(windowLocX);
			_primaryStage.setY(windowLocY);
			//_primaryStage.setWidth(windowSizeX);
			//_primaryStage.setHeight(windowSizeY); 
			
			// add key handler
			_controller.addKeyEventHandler(); 
			
			// closeAction - close through close action
			primaryStage.setOnCloseRequest(event -> {
				_controller.close_action(event);
				event.consume();
			});

			// now show the window
			_primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the primary stage which has been stored as a static field
	 */
	public static Stage getPrimaryStage() {
		return _primaryStage;
	}

	/**
	 * @return the _instance
	 */
	public static TetrisGUI getInstance() {
		return _instance;
	}
}
