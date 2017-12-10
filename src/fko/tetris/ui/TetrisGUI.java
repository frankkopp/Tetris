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

import java.util.Locale;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Application;
import javafx.application.Platform;
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
	private static Stage _primaryStage; 		// The primary stage
	private BorderPane _root;					// the root pane for the gui 

	/**
	 * Creates the JavaFX UI and starts the JavaFX application and waits until the Stage is shown.<br/>
	 * It is not possible to instantiate this more than once - throws RunTime Exception.
	 */
	public TetrisGUI() {
		if (_instance != null) // singleton pattern 
			throw new RuntimeException("It is not possible to instantiate the GUI more than once!");

		// Startup the JavaFX platform
		Platform.setImplicitExit(false);
		
		Locale.setDefault(Locale.GERMANY);
		
		PlatformImpl.startup(() -> {
			final Stage primaryStage = new Stage();
			primaryStage.setTitle("Tetris by Frank Kopp (c)");
			start(primaryStage);
		});
		
		TetrisGUI._instance = this; // save instance handle in static class
		waitForUI(); // wait until primary stage is shown
	}

	/**
	 * Standard way to start a JavaFX application. Is called in the constructor.
	 */
	@Override
	public void start(Stage primaryStage) {

		TetrisGUI._primaryStage = primaryStage;

		try {

			// read FXML file and setup UI
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TetrisGUI.fxml"));
			_root = (BorderPane)fxmlLoader.load();
			_controller = fxmlLoader.getController();

			// Create the Sceen based on the FXML root pane
			Scene scene = new Scene(_root,_root.getPrefWidth(),_root.getPrefHeight());
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			_primaryStage.setScene(scene);

			// set the minimum size
			_primaryStage.setMinWidth(785);
			_primaryStage.setMinHeight(795);

            // get last window position and size from window state file
            double windowLocX = Double.parseDouble(
                    TetrisGUI_Controller.getWindowState().getProperty("windowLocationX", "100"));
            double windowLocY = Double.parseDouble(
            		TetrisGUI_Controller.getWindowState().getProperty("windowLocationY", "200"));
            double windowSizeX = Double.parseDouble(
            		TetrisGUI_Controller.getWindowState().getProperty("windowSizeX", "785"));
            double windowSizeY = Double.parseDouble(
            		TetrisGUI_Controller.getWindowState().getProperty("windowSizeY", "795"));

			// position and resize the window
			_primaryStage.setX(windowLocX);
			_primaryStage.setY(windowLocY);
			_primaryStage.setWidth(windowSizeX);
			_primaryStage.setHeight(windowSizeY);

			// add key handler
			_controller.addKeyEventHandler(); 
			
			// now show the window
			_primaryStage.show();

			// closeAction - close through close action
			scene.getWindow().setOnCloseRequest(event -> {
				_controller.close_action(event);
				event.consume();
			});

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits for the UI to show
	 */
	public void waitForUI() {
		// wait for the UI to show before returning
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//return;
			}
		} while (_primaryStage == null || !_primaryStage.isShowing());
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
