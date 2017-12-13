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
package fko.tetris;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * <p>Properties class for Tetris.</p>
 * <p>Reads its properties from ./var/tetris.settings
 * (This is the default path. An option might override that later.)</p>
 *
 * <p>This class is an singleton as we only will have one properties file
 * for configuration.</p>
 *
 * <p>This class extends java.util.Properties.</p>
 *
 * @see java.util.Properties
 */
public class TetrisSettings extends Properties {

    // Singleton instance
    private final static TetrisSettings _instance = new TetrisSettings();

    // Default properties file
    private final static String propertiesFile = "./var/tetris.settings";

    /**
     * ReversiProperties is a Singleton so use getInstance()
     * @return ReversiProperties instance
     */
    public static TetrisSettings getInstance() {
        return _instance;
    }

    private TetrisSettings() {
        // -- call constructor of java.util.Properties
        super();
        String filename = propertiesFile;
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            load(in);
        } catch (FileNotFoundException e) {
            Tetris.criticalError("Properties file " + filename + " not found!");
        } catch (IOException e) {
            Tetris.criticalError("Properties file " + filename + " could not be loaded!");
        } finally {
            try {
                if (in!=null) {
                    in.close();
                }
            } catch (IOException e) {/*ignore*/}
        }
    }
    
    /**
     * Save properties into a properties file.
     */
    public void save() {
        OutputStream out=null;
        try {
            out = new FileOutputStream(propertiesFile);
            this.store(out, " Window state file for Tetris by Frank Kopp");
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
