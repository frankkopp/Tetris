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
import fko.tetris.TetrisGame;
import fko.tetris.util.HelperTools;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TetrisGUI_Controller implements Observer {
	
	private static final WindowStateFX windowState = new WindowStateFX(); // to save and restore the last position of our window
    private Stage _primaryStage; // handle to primary stage
    
	private TetrisGame _tetrisGame; // holds a running tetrisGame
        
    /**
     * This method is called by the FXMLLoader when initialization is complete
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

    	// set convenience reference to primary stage
        _primaryStage = TetrisGUI.getInstance().getPrimaryStage();
    	
    	// FXML auto checks
    	assertFXML();
        
        // add constantly updated memory info into status panel
        addMemLabelUpdater();
    }
	
    /**
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
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		
		System.out.println("update from "+o+" with args: "+arg);
		
		if (_tetrisGame != null && _tetrisGame.isRunning()) { // game is running
			 PlatformUtil.platformRunAndWait(() -> setUItoGameRunning());
		} else { // no game 
			 PlatformUtil.platformRunAndWait(() -> setUItoGameNotRunning());
		}
		
	}

	/**
	 * 
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

    /**
	 * 
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

	/**
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
     * @return the windowstate
     */
    public static WindowStateFX getWindowState() {
        return windowState;
    }
    
    // #######################################################################
    // Actions
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
    	_tetrisGame = new TetrisGame();
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
    // FXML
    // #######################################################

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="newGame_button"
    private Button newGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_mem_text"
    private Label statusbar_mem_text; // Value injected by FXMLLoader

    @FXML // fx:id="newGame_menu"
    private MenuItem newGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_copyright_test"
    private Label statusbar_copyright_test; // Value injected by FXMLLoader

    @FXML // fx:id="resumeGame_menu"
    private MenuItem resumeGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_game"
    private Menu menu_game; // Value injected by FXMLLoader

    @FXML // fx:id="stopGame_menu"
    private MenuItem stopGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_status_text"
    private Label statusbar_status_text; // Value injected by FXMLLoader

    @FXML // fx:id="about_menu"
    private MenuItem about_menu; // Value injected by FXMLLoader

    @FXML // fx:id="rootPanel"
    private BorderPane rootPanel; // Value injected by FXMLLoader

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

    @FXML // fx:id="resumeGame_button"
    private Button resumeGame_button; // Value injected by FXMLLoader


	/**
	 * FXML checks
	 */
	private void assertFXML() {
		assert newGame_button != null : "fx:id=\"newGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert statusbar_mem_text != null : "fx:id=\"statusbar_mem_text\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert newGame_menu != null : "fx:id=\"newGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert statusbar_copyright_test != null : "fx:id=\"statusbar_copyright_test\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert resumeGame_menu != null : "fx:id=\"resumeGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert menu_game != null : "fx:id=\"menu_game\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert stopGame_menu != null : "fx:id=\"stopGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert statusbar_status_text != null : "fx:id=\"statusbar_status_text\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert about_menu != null : "fx:id=\"about_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert rootPanel != null : "fx:id=\"rootPanel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert menu_help != null : "fx:id=\"menu_help\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert stopGame_button != null : "fx:id=\"stopGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert pauseGame_button != null : "fx:id=\"pauseGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert pauseGame_menu != null : "fx:id=\"pauseGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert menu_level != null : "fx:id=\"menu_level\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert close_menu != null : "fx:id=\"close_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
        assert resumeGame_button != null : "fx:id=\"resumeGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
	}

}
