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

package fko.tetris.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author fkopp
 *
 */
public class AboutDialog extends Stage {

    /**
     * Creates a new window (stage) in form of the About Dialog.
     * This creates the window. Needs to be display with
     */
    public AboutDialog() {
        super();

        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TetrisGUI.class.getResource("AboutDialog.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            Scene scene = new Scene(pane);

            this.setTitle("About");
            this.initModality(Modality.WINDOW_MODAL);
            Stage stage = TetrisGUI.getPrimaryStage();
            this.initOwner(stage);
            this.setScene(scene);

            this.setOnShown((e) -> centerOnPrimaryStage());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Centers this dialog on the center of the primary stage.
     */
    private void centerOnPrimaryStage() {
        Stage stage = TetrisGUI.getPrimaryStage();
        this.setX(stage.getX() + stage.getWidth() / 2 - this.getWidth() / 2);
        this.setY(stage.getY() + stage.getHeight() / 2 - this.getHeight() / 2);
    }

}
