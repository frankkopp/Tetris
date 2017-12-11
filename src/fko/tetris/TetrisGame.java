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
import java.util.Observer;
import java.util.concurrent.LinkedBlockingQueue;

import fko.tetris.tetriminos.Tetrimino;

/**
 * This represents the state of a Tetris game. It holds all information necessary to represent a Tetris game at any 
 * point in time.
 * 
 */
public class TetrisGame extends Observable implements Runnable, Observer {

	// Tetris state
	private Playfield 	_playfield;	// matrix with all cells
	private Bag			_bag;		// bag with all 7 Tetriminos - randomly shuffled to the next queue
	private NextQueue	_nextQueue;	// holds a list the next Tetriminos
	private Tetrimino	_holdQueue; // holds one Tetrimino to be used later
	private int			_startLevel; 	// start level can be set differently by the UI
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
	private TetrisTimer _lockTimer;		// timer to control lock time
	
	private LinkedBlockingQueue<TetrisControlEvents> _controlQueue = new LinkedBlockingQueue<>();
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

		// clear control queue
		_controlQueue.clear();

		do { // loop as long as game is running

			// STATE MACHINE
			switch (_phaseState) {
			case GENERATION:
				generationPhase();
				break;
			case FALLING:
				fallingPhase(); 
				break;
			case LOCK:
				lockPhase();
				break;
			case PATTERN:
				System.out.println("Enter PATTERN phase");
				_phaseState = TetrisPhase.ITERATE;
				break;
			case ITERATE:
				_phaseState = TetrisPhase.ANIMATE;
				break;
			case ANIMATE:
				_phaseState = TetrisPhase.ELIMINATE;
				break;
			case ELIMINATE:
				_phaseState = TetrisPhase.COMPLETION;
				break;
			case COMPLETION:
				_phaseState = TetrisPhase.GENERATION;
				break;
			case GAMEOVER:
				System.out.println("GAME OVER");
				_gameStopped=true;
				break;
			}


			// -- tell the view that model has changed
			setChanged();
			notifyObservers("After PHASE loop");

			// TODO: improve to also pause during the phases
			waitIfPaused();

		} while (_gameStopped == false);

		// -- tell the view that model has changed
		setChanged();
		notifyObservers("Game Thread stopped");
	}

	private void generationPhase() {
		// Generation Phase
		Tetrimino next = _nextQueue.getNext();
		if (_playfield.spawn(next)) {
			// collision detected - "BLOCK OUT" GAME OVER CONDITION
			_phaseState = TetrisPhase.GAMEOVER;
		} else {
			_phaseState = TetrisPhase.FALLING;
		}
	}

	/**
	 * 
	 */
	private void fallingPhase() {
		
		// Start falling timer
		// TODO: make time depended of level
		_fallingTimer = new TetrisTimer(1000);
		_fallingTimer.addObserver(this);
		_fallingTimer.start();
		
		// clear the control queue
		_controlQueue.clear();

		// While timer is >0 allow movements
		// movement = inputs from keyboard (events)
		// we query an event blocking if necessary
		// timer will wake us if no event
		boolean breakFlag = false;
		do {
			// handle movement events
			// Take next control event or wait until available
			TetrisControlEvents event = TetrisControlEvents.NONE;
			try { 
				event = _controlQueue.take();
			} catch (InterruptedException e) { /* empty*/ }

			// event handling
			switch(event) {
			case LEFT:	
				_playfield.moveSideway(-1); // ignored if no move possible
				break;
			case RIGHT:
				_playfield.moveSideway(1); // ignored if no move possible
				break;
			case RTURN:
				_playfield.turnMove(1); // ignored if no move possible
				break;
			case LTURN:
				_playfield.turnMove(-1);  // ignored if no move possible
				break;
			case SOFTDOWN:				
				_playfield.moveDown();	// ignored if no move possible
				break;
			case HARDDOWN:				
				while (!_playfield.moveDown()) {
					// -- tell the view that model has changed
					setChanged();
					notifyObservers("During FALLING after HARDWON");
				}
				breakFlag = true;
				break;
			case HOLD:
				// ignore for now
				break;
			case NONE:
				break;
			}

			// -- tell the view that model has changed
			setChanged();
			notifyObservers("During FALLING");

		} while (!breakFlag && _fallingTimer.getRemainingTime() > 0);

		// stop the timer just t make sure
		_fallingTimer.stop();

		// time is <=0 shift Tetrimino DOWN
		// if landed on surface then phase = LOCK
		// else start over with FALLING
		if (_playfield.moveDown()) { 
			// landed on surface
			_phaseState = TetrisPhase.LOCK;
		}
	}

	/**
	 * 
	 */
	private void lockPhase() {
		//System.out.println("Enter LOCK phase");

		// Start lock timer
		// resets to 500ms every time the Tetrimino moves
		_lockTimer = new TetrisTimer(500);
		_lockTimer.addObserver(this);
		_lockTimer.start();
		
		// clear the control queue
		_controlQueue.clear();
		
		// While timer is >0 allow movements
		// movement = inputs from keyboard (events)
		// we query an event blocking if necessary
		// timer will wake us if no event
		boolean breakFlag = false;
		do {
			// handle movement events
			// Take next control event or wait until available
			TetrisControlEvents event = TetrisControlEvents.NONE;
			try { 
				event = _controlQueue.take();
			} catch (InterruptedException e) { /* empty*/ }

			// event handling
			switch(event) {
			case LEFT:	
				if (!_playfield.moveSideway(-1)) { // if moved reset timer
					_lockTimer.stopResetRestart();
				}
				break;
			case RIGHT:
				if (!_playfield.moveSideway(1)) { // if moved reset timer
					_lockTimer.stopResetRestart();				}
				break;
			case RTURN:
				if (!_playfield.turnMove(1)) { // if moved reset timer
					_lockTimer.stopResetRestart();				}
				break;
			case LTURN:
				if (!_playfield.turnMove(-1)) { // if moved reset timer
					_lockTimer.stopResetRestart();				}
				break;
			case SOFTDOWN:		
				// ignore in LOCK
				break;
			case HARDDOWN:				
				while (!_playfield.moveDown()) {
					// -- tell the view that model has changed
					setChanged();
					notifyObservers("During LOCK after HARDDOWN");
				}
				breakFlag = true;
				break;
			case HOLD:
				// ignore for now
				break;
			case NONE:
				break;
			}
			
			// check if Tetrimino can move down
			// if yes then go back to phase FALLING
			if (_playfield.canMoveDown()) {
				//System.out.println("LOCK CAN MOVE -> FALLING");
				breakFlag = true;
				_phaseState = TetrisPhase.FALLING;
			}

			// -- tell the view that model has changed
			setChanged();
			notifyObservers("During LOCK");

		} while (!breakFlag && _lockTimer.getRemainingTime() > 0);

		// stop the timer just to make sure
		_lockTimer.stop();
		
		// merge Tetrimino into background
		if (_phaseState == TetrisPhase.LOCK) {// only merge if we are still in phase LOCK
			_playfield.merge();
			_phaseState = TetrisPhase.PATTERN; // go to next phase
		}

	}

	public void controlQueueAdd(TetrisControlEvents e) {
		_controlQueue.add(e);
	}

	@Override
	public void update(Observable o, Object arg) {
//		if (o ==_fallingTimer)
//			System.out.println("Update from FALLING timer time is out!");
//		else 
//			System.out.println("Update from LOCK timer time is out!");
		
		_controlQueue.add(TetrisControlEvents.NONE);
	}

	/*
	 * Checks if game is paused and waits until game is resumed 
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
