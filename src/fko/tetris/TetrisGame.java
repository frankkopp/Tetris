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

import java.util.Observable;

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
	
	private TetrisPhase _phaseState;	// the state/phase the engine is currently in
									// this determines what inputs and actions are allowed and 
									// which states can follow
	
	private TetrisTimer _fallingTimer; // timer to control falling time
	
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
		
		_phaseState = TetrisPhase.GENERATION;
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
        
        _phaseState = TetrisPhase.GENERATION; // we always start with GENERATION
		
		do { // loop as long as game is running
			
			// STATE MACHINE
			switch (_phaseState) {
			
			case GENERATION:
				
				// Generation Phase
				Tetrimino next = _nextQueue.getNext();
				if (_playfield.spawn(next)) {
					// collision detected - BLOCK OUT GAME OVER CONDITION
					_phaseState = TetrisPhase.GAMEOVER;
					break;
				} else {
					_phaseState = TetrisPhase.FALLING;
				}
				break;
				
			case FALLING:
				
				// Start falling timer
				// TODO: make time depended of level
				_fallingTimer = new TetrisTimer(1000);
				_fallingTimer.start();

				// While timer is >0 allow movements
				// movement = inputs from keyboard (events)
				// we query an event blocking if necessary
				// timer will wake us if no event
				do {
					System.out.println("Remaining Time: "+_fallingTimer.getRemainingTime());
					
					// handle movement events
					
					// vvv DEBUG / TEMPORARY
					try { Thread.sleep(100);} 
					catch (InterruptedException e) { /* nothing */ }
					// ^^^ DEBUG
					
				} while (_fallingTimer.getRemainingTime() > 0);

				// stop the timer just t make sure
				_fallingTimer.stop();
				
				// time is <=0 shift Tetrimino DOWN
				// if landed on surface then phase = LOCK
				// else start over with FALLING
				if (_playfield.moveDown()) { 
					System.out.println("Touchdown!");
					// landed on surface
					_phaseState = TetrisPhase.LOCK;
				} 
				
				break;
				
			case LOCK:
				break;
			case PATTERN:
				break;
			case ITERATE:
				break;
			case ANIMATE:
				break;
			case ELIMINATE:
				break;
			case COMPLETION:
				break;
			case GAMEOVER:
				System.out.println("GAME OVER");
				_gameStopped=true;
				break;
				
			default:
				System.err.println("Did we miss a state?");
				break;
			}
			
			
			// -- tell the view that model has changed
			setChanged();
	        notifyObservers("After phase loop");
			
			waitIfPaused();
			
		} while (_gameStopped == false);
		
		// -- tell the view that model has changed
        setChanged();
        notifyObservers("Game Thread stopped");
	}

	/*
	 * checkss if game is paused and waits until game is resumed 
	 */
	private void waitIfPaused() {
		if (_isPaused) {
			while (_isPaused && _gameStopped == false) {
				//System.out.println("PAUSED "+LocalDateTime.now());
				try {
			        Thread.sleep(1000);
				} catch (InterruptedException e) { /* nothing */ }
			}
		}
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
	 * @return the _playfield
	 */
	public Playfield getPlayfield() {
		return _playfield;
	}

	/**
	 * @return the _nextQueue
	 */
	public NextQueue getNextQueue() {
		return _nextQueue;
	}
	
	
	
}
