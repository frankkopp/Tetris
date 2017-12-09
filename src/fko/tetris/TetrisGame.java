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

import java.time.LocalDateTime;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;

import fko.tetris.tetriminos.Tetrimino;

/**
 * This represents the state of a Tetris game. It holds all information necessary to represent a Tetris game at any 
 * point in time.
 * 
 */
public class TetrisGame extends Observable implements Runnable {
	
	// Tetris state
	private Playfield 	_playfield;	// matrix with all cells
	private Bag			_bag;		// bag with all 7 Tetriminos - randomly shuffled to the next queue
	private NextQueue	_nextQueue;	// holds a list the next Tetriminos
	private Tetrimino	_holdQueue; // holds one Tetrimino to be used later
	private int			_startLevel; 	// start level can be set differntly by the UI
	private int			_currentLevel; 	// current level while playing
	private int			_score;			// current score
	private int			_lineCount;	// who many line have been eliminated since start
	private int			_tetrisesCount;	// number of Tetrises since start
	
	// application fields
	private Thread		_gameThread; 			// the thread where the Tetris game will run in
	private boolean 	_gameStopped = true; 	// flag to stop a running game
	private boolean 	_isPaused;
	
	/**
	 * Creates a Tetris game with default values
	 */
	public TetrisGame() {
		_playfield 		= new Playfield();
		_bag 			= new Bag();
		_nextQueue		= new NextQueue(_bag, 3);
		_holdQueue 		= null;
		_startLevel 	= 1;
		_currentLevel 	= 1;
		_score 			= 0;
		_lineCount 		= 0;
		_tetrisesCount 	= 0;
	}
	
	 /**
     * Starts a new a new game of Tetris<br/>
     * The thread then calls run() to actually do the work.
     */
    public void startTetrisGame() {
            // Now start the thread
            if (_gameThread == null) {
            	_gameThread = new Thread(this, "TetrisGame");
            	_gameThread.start();
            } else {
                throw new IllegalStateException("startTetrisGame(): Game thread already exists.");
            }
    }

    /**
     * Stops the current game.<br/>
     */
    public void stopTetrisGame() {
        if (_gameThread==null || !_gameThread.isAlive()) {
            throw new IllegalStateException("stopTetrisGame(): Game thread is not running");
        }
        // set a flag to stop the game
        _gameStopped = true;
        _gameThread.interrupt();
    }


	/**
	 * Implements the Runnable interface
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		_gameStopped = false;
		
		// -- tell the view that model has changed
        setChanged();
        notifyObservers("Game Thread started");
		
		do { // loop as long as game is running
			
			// vvv DEBUG / TEMPORARY
			try {
				System.out.println(LocalDateTime.now());
				Thread.sleep(1000);
			} catch (InterruptedException e) { /* nothing */ }
			// ^^^ DEBUG
			
			if (_isPaused) {
				while (_isPaused && _gameStopped == false) {
					System.out.println("PAUSED "+LocalDateTime.now());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { /* nothing */ }
				}
			}
			
		} while (_gameStopped == false);
		
		// -- tell the view that model has changed
        setChanged();
        notifyObservers("Game Thread stopped");
	}
	
	public boolean isRunning() {
		return !_gameStopped; 
	}
	
	/**
	 * @return the _isPaused
	 */
	public boolean isPaused() {
		return _isPaused;
	}

	/**
	 * @param _isPaused the _isPaused to set
	 */
	public void setPaused(boolean _isPaused) {
		this._isPaused = _isPaused;
		_gameThread.interrupt();
		// -- tell the view that model has changed
        setChanged();
        notifyObservers("Game paused: "+_isPaused);
	}

	/**
	 * Fills the next queue from the bag until queue has max elements. Creates new bag if bag is empty.
	 * @return the filled queue
	 */
	private Queue fillFromBag() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return new Set of 7 Tetriminos
	 */
	private Set createNewBag() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the _playfield
	 */
	public Playfield getPlayfield() {
		return _playfield;
	}
	
}
