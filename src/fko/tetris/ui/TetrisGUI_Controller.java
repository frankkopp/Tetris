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
/**
 * Sample Skeleton for 'TetrisGUI.fxml' Controller Class
 */

package fko.tetris.ui;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

import fko.tetris.Tetris;
import fko.tetris.TetrisControlEvents;
import fko.tetris.TetrisGame;
import fko.tetris.util.HelperTools;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * The Controller sets up additional ui elements after the FXML loader has done its initialization. The FXML loader
 * calls the Controller's initialize() method.<br/> 
 * The Controller also receives all input and events from the user interface and the model and executes the appropriate 
 * ui updates and model actions. The UI calls the actions methods directly. The model signals via the Observer Interface 
 * @see java.util.Observer#update(java.util.Observable, java.lang.Object) that the model has changed and the UI 
 * should update its views.
 * 
 * TODO: NextQueue show option
 */
public class TetrisGUI_Controller implements Observer {

	private static final WindowStateFX windowState = new WindowStateFX(); // to save and restore the last position of our window
	private Stage _primaryStage; // handle to primary stage
	private TetrisGame _tetrisGame; // holds a running tetrisGame
	private PlayfieldPane _playfieldPane; // handle to PlayfieldPane
	private NextQueuePane _nextQueuePane; // handle to NextQueuePane
	private HoldPane _holdPane; // handle to NextQueuePane

	/**
	 * This method is called by the FXMLLoader when initialization is complete
	 */
	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		_primaryStage = TetrisGUI.getPrimaryStage(); // set convenience reference to primary stage
		assertFXML(); // FXML auto checks

		addMemLabelUpdater(); // add constantly updated memory info into status panel
		addPlayfieldPane(); // add the playfield pane 
		addNextQueuePane(); // add the next queue pane
		addHoldPane(); // add the hold Tetrimino pane
		
		// howto Text
		Text howToText = new Text(String.format(
				  "L Arrow to move left.%n"
				+ "R Arrow to move right.%n"
				+ "DOWN Arrow to soft drop.%n"
				+ "SPACE to hard drop.%n"
				+ "A to turn left.%n"
				+ "S to turn right.%n"
				+ "UP Arrow to turn right."));
		howToText.setStyle("-fx-font-family: Comic Sans MS Bold; -fx-fill: red; -fx-font-size: 8pt");
		howtoText.getChildren().add(howToText);
		
		// change the startLevelLabel when the slider changes
		startLevelLabel.textProperty().bind(
	            Bindings.format(
	                "%.0f",
	                startLevelSlider.valueProperty()
	            )
	        );
	}

	/**
	 * Handles keyboard events - call from Main gui class
	 */
	protected void addKeyEventHandler() {
		_primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				//System.out.println("Key Pressed: "+event.getCode().toString());
				if (_tetrisGame == null) return; // only when game is available

				switch (event.getCode()) {
				case ESCAPE: _tetrisGame.setPaused(_tetrisGame.isPaused() ? false : true); break;
				case LEFT:	_tetrisGame.controlQueueAdd(TetrisControlEvents.LEFT); break;
				case RIGHT:	_tetrisGame.controlQueueAdd(TetrisControlEvents.RIGHT); break;
				case X:
				case UP:	_tetrisGame.controlQueueAdd(TetrisControlEvents.RTURN); break;
				case Y: // HACK_ in case of different keyboard layout
				case Z:		
				case CONTROL: _tetrisGame.controlQueueAdd(TetrisControlEvents.LTURN); break; 
				case DOWN:	_tetrisGame.controlQueueAdd(TetrisControlEvents.SOFTDOWN); break;
				case SPACE:	_tetrisGame.controlQueueAdd(TetrisControlEvents.HARDDOWN); break;
				case SHIFT:
				case C:		_tetrisGame.controlQueueAdd(TetrisControlEvents.HOLD); break;
				default:
				}
			}
		}); 
	}

	/*
	 * Creates a PlayfieldPane and adds it to the root panel  
	 */
	private void addPlayfieldPane() {
		_playfieldPane = new PlayfieldPane(this);
		rootPanel.setCenter(_playfieldPane);
	}

	/*
	 * Creates a NextQueuePane and adds it to the root panel  
	 */
	private void addNextQueuePane() {
		_nextQueuePane = new NextQueuePane();
		AnchorPane.setTopAnchor(_nextQueuePane, 0.0);
		AnchorPane.setBottomAnchor(_nextQueuePane, 0.0);
		AnchorPane.setLeftAnchor(_nextQueuePane, 0.0);
		AnchorPane.setRightAnchor(_nextQueuePane, 0.0);
		nextQueueBox.getChildren().add(_nextQueuePane);
		
	}

	/*
	 * Creates a NextQueuePane and adds it to the root panel  
	 */
	private void addHoldPane() {
		_holdPane = new HoldPane();
		AnchorPane.setTopAnchor(_holdPane, 0.0);
		AnchorPane.setBottomAnchor(_holdPane, 0.0);
		AnchorPane.setLeftAnchor(_holdPane, 0.0);
		AnchorPane.setRightAnchor(_holdPane, 0.0);
		holdBox.getChildren().add(_holdPane);
		
	}
	
	/*
	 * Adds an updater to the mem label in the status bar
	 */
	private void addMemLabelUpdater() {
		Task<Void> dynamicTimeTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				while (true) {
					updateMessage(HelperTools.getMBytes(Runtime.getRuntime().freeMemory()) + " MB / "
							+ HelperTools.getMBytes(Runtime.getRuntime().totalMemory()) + " MB");
					try {Thread.sleep(500);} catch (InterruptedException ex) {break;}
				}
				return null;
			}
		};
		statusbar_mem_text.textProperty().bind(dynamicTimeTask.messageProperty());
		Thread t2 = new Thread(dynamicTimeTask);
		t2.setName("Statusbar Mem Labal Updater");
		t2.setDaemon(true);
		t2.start();
	}

	/**
	 * This is called by model explicitly whenever the model changes
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {

		/* IMPORTANT:
		 * This is usually called by another thread and not the FAT thread (FX Application Thread).
		 * Other threads cannot change to UI - this would cause an exception. Therefore Platform.runLater(r) 
		 * is used to hand over a Runnable to the FAT. Here this is encapsulated by a util-Class PlatformUtil which
		 * adds a method platformRunAndWait(r) to wait for the UI to finish the task before is returns to the non-FAT 
		 * thread.
		 * If you don't need to wait for the UI use runFutureTask(r). 
		 * 
		 * Waiting for the UI helps when the model changes very quickly otherwise and the UI would have trouble showing 
		 * meaningful output. It also prevents exceptions for when the UI tries to access something in the model which
		 * has already changed again, e.g. does not exist any more.
		 * Do not use this if you do not want calculations in the model to be slowed down.
		 * 
		 * The update() call from another thread quite often is the cause for deadlocks as the update of the UI in 
		 * the FAT often uses model objects to update the UI. E.g. if a call to a model object's method in turn updates the 
		 * model and calls update() we have a deadlock. 
		 */

		//System.out.println("update from "+o+" with args: "+arg);

		if (_tetrisGame != null && _tetrisGame.isRunning()) { // game is running
			_playfieldPane.setPlayField(_tetrisGame.getPlayfield());
			_nextQueuePane.setNextQueue(_tetrisGame.getNextQueue());
			_holdPane.setHoldTetrimino(_tetrisGame.getHoldTetrimino());
			PlatformUtil.platformRunAndWait(() -> setUItoGameRunning()); // setup ui
			PlatformUtil.platformRunAndWait(() -> draw()); // draw panes
		} else { // no game 
			_playfieldPane.setPlayField(null);
			_nextQueuePane.setNextQueue(null);
			_holdPane.setHoldTetrimino(null);
			PlatformUtil.platformRunAndWait(() -> setUItoGameNotRunning()); // setup ui 
			PlatformUtil.platformRunAndWait(() -> draw()); // draw panes
		}
	}

	/*
	 * calls draw for all panes  
	 */
	private void draw() {
		_playfieldPane.draw();
		_nextQueuePane.draw();
		_holdPane.draw();
		_updateInfoDraw();
	}

	/*
	 * Updates all info fields. E.g. score, etc. 
	 */
	private void _updateInfoDraw() {
		if (_tetrisGame == null) {
			scoreLabel.setText("0");
			levelLabel.setText("1");
			linecountLabel.setText("0");
			tetrisCountLabel.setText("0");
			startLevelLabel.setText("not yet implemented"); // this is kept in UI as a property to menu or so
		} else {
			scoreLabel.setText(Integer.toString(_tetrisGame.getScore()));
			levelLabel.setText(Integer.toString(_tetrisGame.getCurrentLevel()));
			linecountLabel.setText(Integer.toString(_tetrisGame.getLineCount()));
			tetrisCountLabel.setText(Integer.toString(_tetrisGame.getTetrisesCount()));
		}
	}

	/*
	 * Setup controls (menu, buttons, etc.) for running game 
	 */
	private void setUItoGameRunning() {
		// -- set possible actions (menu) --
		newGame_menu.setDisable(true);
		newGame_button.setDisable(true);
		stopGame_menu.setDisable(false);
		stopGame_button.setDisable(false);
		if (_tetrisGame.isPaused()) {
			pauseGame_menu.setDisable(true);
			pauseGame_button.setDisable(true);
			resumeGame_menu.setDisable(false);
			resumeGame_button.setDisable(false);
		} else {
			pauseGame_menu.setDisable(false);
			pauseGame_button.setDisable(false);
			resumeGame_menu.setDisable(true);
			resumeGame_button.setDisable(true);
		}
		close_menu.setDisable(false);
		about_menu.setDisable(false);
		statusbar_status_text.setText("Game running");
	}

	/*
	 * Setup controls (menu, buttons, etc.) for game not running
	 */
	private void setUItoGameNotRunning() {
		// -- set possible actions (menu) --
		newGame_menu.setDisable(false);
		newGame_button.setDisable(false);
		stopGame_menu.setDisable(true);
		stopGame_button.setDisable(true);
		pauseGame_menu.setDisable(true);
		pauseGame_button.setDisable(true);
		resumeGame_menu.setDisable(true);
		resumeGame_button.setDisable(true);
		close_menu.setDisable(false);
		about_menu.setDisable(false);
		statusbar_status_text.setText("No Game");
	}

	/*
	 * Save the current sizes and coordinates of all windows to restore them
	 * when starting up the next time.
	 */
	private void saveWindowStates() {
		windowState.setProperty("windowLocationX", String.valueOf(this._primaryStage.getX()));
		windowState.setProperty("windowLocationY", String.valueOf(this._primaryStage.getY()));
		windowState.setProperty("windowSizeX", String.valueOf(this._primaryStage.getWidth()));
		windowState.setProperty("windowSizeY", String.valueOf(this._primaryStage.getHeight()));
		windowState.save();
	}

	/**
	 * Return the window state object.
	 * @return the window state
	 */
	public static WindowStateFX getWindowState() {
		return windowState;
	}



	// #######################################################################
	// Actions
	// IMPORTANT: Watch out for deadlocks - JavaFX app thread calling model
	// 			  is always dangerous.	
	// #######################################################################

	/**
	 * Called when window is closed
	 * @param event
	 */
	@FXML
	void close_action(Event event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(_primaryStage);
		alert.setTitle("Close Tetris");
		alert.setHeaderText("Close Tetris");
		alert.setContentText("Do your really want to quit Tetris?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			saveWindowStates();
			Tetris.exitTetris();
		} else {
			// ... user chose CANCEL or closed the dialog
		}
	}

	@FXML
	void newGame_Action(ActionEvent event) {
		_playfieldPane.requestFocus();
		_tetrisGame = new TetrisGame((int)startLevelSlider.getValue());
		_tetrisGame.addObserver(this);
		_tetrisGame.startTetrisGame();
	}

	@FXML
	void stopGame_action(ActionEvent event) {
		_tetrisGame.stopTetrisGame();
	}

	@FXML
	void pauseGame_action(ActionEvent event) {
		_tetrisGame.setPaused(true);
	}

	@FXML
	void resumeGame_action(ActionEvent event) {
		_tetrisGame.setPaused(false);
	}

	@FXML
	void aboutDialogOpen_action(ActionEvent event) {
		AboutDialog aboutDialogStage = new AboutDialog();
		aboutDialogStage.showAndWait();
	}

	// #######################################################
	// FXML Setup
	// #######################################################

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="newGame_button"
	private Button newGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="newGame_menu"
	private MenuItem newGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="playfieldPane"
	private Pane playfieldPane; // Value injected by FXMLLoader

	@FXML // fx:id="statusbar_copyright_test"
	private Label statusbar_copyright_test; // Value injected by FXMLLoader

	@FXML // fx:id="linecountLabel"
	private Label linecountLabel; // Value injected by FXMLLoader

	@FXML // fx:id="about_menu"
	private MenuItem about_menu; // Value injected by FXMLLoader

	@FXML // fx:id="scoreLabel"
	private Label scoreLabel; // Value injected by FXMLLoader

	@FXML // fx:id="levelLabel"
	private Label levelLabel; // Value injected by FXMLLoader

	@FXML // fx:id="startLevelLabel"
	private Label startLevelLabel; // Value injected by FXMLLoader

	@FXML // fx:id="menu_help"
	private Menu menu_help; // Value injected by FXMLLoader

	@FXML // fx:id="stopGame_button"
	private Button stopGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="pauseGame_button"
	private Button pauseGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="pauseGame_menu"
	private MenuItem pauseGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="menu_level"
	private Menu menu_level; // Value injected by FXMLLoader

	@FXML // fx:id="close_menu"
	private MenuItem close_menu; // Value injected by FXMLLoader

	@FXML // fx:id="holdBox"
	private Pane holdBox; // Value injected by FXMLLoader

	@FXML // fx:id="statusbar_mem_text"
	private Label statusbar_mem_text; // Value injected by FXMLLoader

	@FXML // fx:id="resumeGame_menu"
	private MenuItem resumeGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="menu_game"
	private Menu menu_game; // Value injected by FXMLLoader

	@FXML // fx:id="stopGame_menu"
	private MenuItem stopGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="statusbar_status_text"
	private Label statusbar_status_text; // Value injected by FXMLLoader

	@FXML // fx:id="rootPanel"
	private BorderPane rootPanel; // Value injected by FXMLLoader

	@FXML // fx:id="tetrisCountLabel"
	private Label tetrisCountLabel; // Value injected by FXMLLoader

	@FXML // fx:id="resumeGame_button"
	private Button resumeGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="startLevelSlider"
    private Slider startLevelSlider; // Value injected by FXMLLoader
	
    @FXML // fx:id="nextQueueBox"
    private Pane nextQueueBox; // Value injected by FXMLLoader
    
    @FXML // fx:id="howtoText"
    private Pane howtoText; // Value injected by FXMLLoader
    
    @FXML // fx:id="peekOption"
    protected CheckMenuItem peekOption; // Value injected by FXMLLoader
    
    @FXML // fx:id="ghostPieceOption"
    protected CheckMenuItem ghostPieceOption; // Value injected by FXMLLoader
    
	/*
	 * FXML checks
	 */
	private void assertFXML() {
		assert newGame_button != null : "fx:id=\"newGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert newGame_menu != null : "fx:id=\"newGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert playfieldPane != null : "fx:id=\"playfieldPane\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert statusbar_copyright_test != null : "fx:id=\"statusbar_copyright_test\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert linecountLabel != null : "fx:id=\"linecountLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert about_menu != null : "fx:id=\"about_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert scoreLabel != null : "fx:id=\"scoreLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert levelLabel != null : "fx:id=\"levelLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert startLevelLabel != null : "fx:id=\"startLevelLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert menu_help != null : "fx:id=\"menu_help\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert stopGame_button != null : "fx:id=\"stopGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert pauseGame_button != null : "fx:id=\"pauseGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert pauseGame_menu != null : "fx:id=\"pauseGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert menu_level != null : "fx:id=\"menu_level\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert close_menu != null : "fx:id=\"close_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert holdBox != null : "fx:id=\"holdBox\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert statusbar_mem_text != null : "fx:id=\"statusbar_mem_text\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert resumeGame_menu != null : "fx:id=\"resumeGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert menu_game != null : "fx:id=\"menu_game\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert stopGame_menu != null : "fx:id=\"stopGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert statusbar_status_text != null : "fx:id=\"statusbar_status_text\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert rootPanel != null : "fx:id=\"rootPanel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert tetrisCountLabel != null : "fx:id=\"tetrisCountLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert resumeGame_button != null : "fx:id=\"resumeGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert startLevelSlider != null : "fx:id=\"startLevelSlider\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert nextQueueBox != null : "fx:id=\"nextQueueBox\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert howtoText != null : "fx:id=\"howtoText\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert peekOption != null : "fx:id=\"peekOption\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert ghostPieceOption != null : "fx:id=\"ghostPieceOption\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
	}

}
