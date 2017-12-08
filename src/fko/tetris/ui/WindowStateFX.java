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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a properties file with the last window states.
 */
public class WindowStateFX extends java.util.Properties {

    private static final long serialVersionUID = -1824390546690585056L;

    private final static String propertiesFile = '/' + "var/windowstate.guid";
    private final static String userDir = System.getProperty("user.dir");

    /**
     * Creates window state object.
     * Opens the window state file and reads it as properties.
     */
    public WindowStateFX() {
        super();
        String aUserDir = System.getProperty("user.dir");
        InputStream in = null;
        try {
            in = new FileInputStream(aUserDir + propertiesFile);
            this.load(in);
        } catch (FileNotFoundException e) {
            System.err.println("Properties file " + propertiesFile + " not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Properties file " + propertiesFile + " could not be loaded!");
            e.printStackTrace();
        } finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Save the window states into a properties file.
     */
    public void save() {
        OutputStream out=null;
        try {
            out = new FileOutputStream(userDir + propertiesFile);
            this.store(out, " Window state file for Chessly by Frank Kopp");
        } catch (FileNotFoundException e) {
            System.err.println("Properties file " + propertiesFile + " could not be saved!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Properties file " + propertiesFile + " could not be saved!");
            e.printStackTrace();
        } finally {
            if (out!=null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

}
