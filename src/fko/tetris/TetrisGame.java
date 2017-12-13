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
 * TODO: Highscore Ranking
 */
public class TetrisGame extends Observable implements Runnable, Observer {

	/**
	 * Sets how many Tetriminos are in the next queue. Not necessarily the same as how 
	 * many are shown in the ui.
	 */
	public static final int NEXTQUEUE_SIZE = 7;
	
	// Tetris state
	private Playfield 	_playfield;		// matrix with all cells
	private Bag			_bag;			// bag with all 7 Tetriminos - randomly shuffled to the next queue
	private NextQueue	_nextQueue;		// holds a list the next Tetriminos
	private Tetrimino	_holdQueue; 	// holds one Tetrimino to be used later
	private int			_startLevel; 	// start level can be set differently by the UI
	private int			_currentLevel; 	// current level while playing
	private int			_score;			// current score
	private int			_lineCount;		// who many line have been eliminated since start
	private int			_tetrisesCount;	// number of Tetrises since start

	// application fields
	private Thread		_gameThread; 			// the thread where the Tetris game will run in
	private boolean 	_gameStopped = true; 	// flag to stop a running game
	private boolean 	_isPaused;

	private TetrisPhase _phaseState;	// the state/phase the engine is currently in
	// this determines what inputs and actions are allowed and 
	// which states can follow

	private TetrisTimer _fallingTimer = new TetrisTimer(1000); // timer to control falling time
	private TetrisTimer _lockTimer = new TetrisTimer(500); // timer to control lock time
	
	private LinkedBlockingQueue<TetrisControlEvents> _controlQueue = new LinkedBlockingQueue<>();
		
	private boolean _holdAllowed = true; // using hold is only allowed once between LOCK phases
	
	private int _lastClearedLinesCount = 0;
	private int _lastHardDropLineCount = 0;
	private int _lastSoftDropLineCount = 0;
	
	/**
	 * Creates a Tetris game with default values
	 */
	public TetrisGame() {
		_playfield 		= new Playfield();
		_bag 			= new Bag();
		_nextQueue		= new NextQueue(_bag, NEXTQUEUE_SIZE);
		_holdQueue 		= null;
		_startLevel 	= 1;
		_currentLevel 	= 1;
		_score 			= 0;
		_lineCount 		= 0;
		_tetrisesCount 	= 0;
	}

	/**
	 * @param startLevel
	 */
	public TetrisGame(int startLevel) {
		_playfield 		= new Playfield();
		_bag 			= new Bag();
		_nextQueue		= new NextQueue(_bag, NEXTQUEUE_SIZE);
		_holdQueue 		= null;
		_startLevel 	= startLevel;
		_currentLevel 	= startLevel;
		_score 			= 0;
		_lineCount 		= (startLevel-1) * 10; // if started with a higher level assume appropriate line count 
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

		completionPhase();

		// clear control queue
		_controlQueue.clear();

		do { // loop as long as game is running

			/* ******************************************************
			 * TETRIS STATE MACHINE 
			 ********************************************************/
			switch (_phaseState) {
			case GENERATION:
				/*
				 * Random Generation
				 * Tetris uses a “bag” system to determine the sequence of Tetriminos that appear during game play. 
				 * This system allows for equal distribution among the seven Tetriminos.
				 * The seven different Tetriminos are placed into a virtual bag, then shuffled into a random order. 
				 * This order is the sequence that the bag “feeds” the Next Queue. Every time a new Tetrimino is 
				 * generated and starts its fall within the Matrix, the Tetrimino at the front of the line in the bag 
				 * is placed at the end of the Next Queue, pushing all Tetriminos in the Next Queue forward by one. 
				 * The bag is refilled and reshuffled once it is empty.
				 */
				generationPhase();
				break;
			case FALLING:
				/*
				 * Throughout the Falling Phase, the player can move, rotate, Soft Drop, Hard Drop, and Hold a 
				 * Tetrimino. The Tetrimino enters the Lock Phase once it lands on a Surface.
				 */
				fallingPhase(); 
				break;
			case LOCK:
				/*
				 * The player can perform the same actions on a Tetrimino in this phase as he/she can in the 
				 * Falling Phase, as long as the Tetrimino is not yet Locked down. A Tetrimino that is Hard Dropped 
				 * Locks Down immediately. However, if a Tetrimino naturally falls or Soft Drops onto a landing 
				 * Surface, it is given 0.5 seconds on a Lock Down Timer before it actually Locks Down.
				 * Three rule sets —Infinite Placement, Extended, and Classic— dictate the conditions for Lock Down 
				 * 
				 * Note: Using the Super Rotation System, rotating a Tetrimino often causes the y-coordinate of the 
				 * Tetrimino to increase, i.e., it “lifts up” off the Surface it landed on. The Lock Down Timer does 
				 * not reset in this case, but it does stop counting down until the Tetrimino lands again on a Surface
				 * that has the same (or higher) y-coordinate as it did before it was rotated. Only if it lands on a 
				 * Surface with a lower y-coordinate will the timer reset.
				 * 
				 * If moving or rotating a Tetrimino causes it to fall again, it re-enters the Falling Phase. Once the 
				 * Tetrimino is fully Locked Down, it enters the Pattern Phase.
				 */
				lockPhase();
				break;
			case PATTERN:
				/*
				 * In this phase, the engine looks for patterns made from Locked Down Blocks in the Matrix. Once a 
				 * pattern has been matched, it can trigger any number of Tetris variant-related effects.
				 * The classic pattern is the Line Clear pattern. This pattern is matched when one or more rows of 
				 * 10 horizontally aligned Matrix cells are occupied by Blocks. The matching Blocks are then marked
				 * for removal on a hit list. Blocks on the hit list are cleared from the Matrix at a later time in 
				 * the Eliminate Phase.
				 */
				patternPhase();
				break;
			case ITERATE:
				/*
				 * In this phase, the engine is given a chance to scan through all cells in the Matrix and evaluate or 
				 * manipulate them according to an editor-defined iteration script. This phase consumes no apparent 
				 * game time.
				 * Note: This phase is included in the engine to allow for more complicated variants in the future, 
				 * and has thus far not been used.
				 */
				_phaseState = TetrisPhase.ANIMATE;
				break;
			case ANIMATE:
				/*
				 * Here, any animation scripts are executed within the Matrix. The Tetris Engine moves on to the 
				 * Eliminate Phase once all animation scripts have been run.
				 * TODO: Add Sounds and maybe animations
				 */
				_phaseState = TetrisPhase.ELIMINATE; // not implemented
				break;
			case ELIMINATE:
				/*
				 * Any Minos marked for removal, i.e., on the hit list, are cleared from the Matrix in this phase. If 
				 * this results in one or more complete 10-cell rows in the Matrix becoming unoccupied by Minos, then 
				 * all Minos above that row(s) collapse, or fall by the number of complete rows cleared from the 
				 * Matrix. Points are awarded to the player according to the Tetris Scoring System, as seen in the 
				 * Scoring section.
				 * 
				 * Game Statistics
				 * Statistics such as the number of Singles, Doubles, Triples, Tetrises, and T-Spins can also be 
				 * tracked in the Eliminate Phase. Ideally, some sort of High Score Table should record the player’s
				 * name, the highest level reached, his total score, and other statistics that can be tracked in 
				 * this phase.
				 */
				eliminatePhase();
				break;
			case COMPLETION:
				/*
				 * This is where any updates to information fields on the Tetris playfield are updated, such as the 
				 * Score and Time. The Level Up condition is also checked to see if it is necessary to advance the 
				 * game level.
				 * Whether the player Levels Up or not, control flows back into the Generation Phase of the 
				 * Next Tetrimino.
				 */
				completionPhase();
				break;
			case GAMEOVER:
				System.out.println("GAME OVER");
				_gameStopped=true;
				break;
			}


			// -- tell the view that model has changed
			setChanged();
			notifyObservers("After PHASE loop");

			waitIfPaused();

		} while (_gameStopped == false);

		// -- tell the view that model has changed
		setChanged();
		notifyObservers("Game Thread stopped");
	}

	/*
	 * GENERATION phase 
	 */
	private void generationPhase() {
		// Generation Phase
		Tetrimino next = _nextQueue.getNext();
		if (_playfield.spawn(next)) {
			// collision detected - "BLOCK OUT" GAME OVER CONDITION
			_phaseState = TetrisPhase.GAMEOVER;
		} else {
			// Immediately fall into visible area and check for collision
			if (_playfield.moveDown()) {
				// collision detected - "BLOCK OUT" GAME OVER CONDITION
				_phaseState = TetrisPhase.LOCK;
			}
			_phaseState = TetrisPhase.FALLING;
		}
	}

	/*
	 * FALLING phase
	 */
	private void fallingPhase() {
		
		// Start falling timer
		_fallingTimer.addObserver(this);
		_fallingTimer.setTimer(calculateFallingTime());
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

			waitIfPaused();
			
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
				_lastSoftDropLineCount+=1;
				break;
			case HARDDOWN:				
				_lastHardDropLineCount=0;
				while (!_playfield.moveDown()) {
					_lastHardDropLineCount++;
					// -- tell the view that model has changed
					setChanged();
					notifyObservers("During FALLING after HARDOWN");
				}
				breakFlag = true;
				break;
			case HOLD:
				if (_holdAllowed) {
					Tetrimino toField = _holdQueue == null ? _nextQueue.getNext() : _holdQueue;
					try { // to reset start coordinates we use a new object - avoids a long switch statement :)
						_holdQueue = _playfield.getCurrentTetrimino().getClass().newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} 
					_playfield.spawn(toField);
					// -- tell the view that model has changed
					setChanged();
					notifyObservers("During FALLING after HOLD");
				}
				_holdAllowed = false; 
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

	/*
	 * LOCK phase
	 * Implements the INFINITE PLACEMENT LOCK DOWN	
	 * TODO: Implement EXTENDED LOCK DOWN and CLASSIC LOCKDOWN
	 */
	private void lockPhase() {
		//System.out.println("Enter LOCK phase");

		// Start lock timer - lock time is always 500ms
		_lockTimer.addObserver(this);
		_lockTimer.restart();
		
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

			waitIfPaused();
			
			// event handling
			switch(event) {
			case LEFT:	
				if (!_playfield.moveSideway(-1)) { // if moved reset timer
					_lockTimer.restart();
				}
				break;
			case RIGHT:
				if (!_playfield.moveSideway(1)) { // if moved reset timer
					_lockTimer.restart();				}
				break;
			case RTURN:
				if (!_playfield.turnMove(1)) { // if moved reset timer
					_lockTimer.restart();				}
				break;
			case LTURN:
				if (!_playfield.turnMove(-1)) { // if moved reset timer
					_lockTimer.restart();				}
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
				// ignore in LOCK
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
		
		// allow new holds
		_holdAllowed = true;
		
		// merge Tetrimino into background
		if (_phaseState == TetrisPhase.LOCK) {// only merge if we are still in phase LOCK
			_playfield.merge();
			_phaseState = TetrisPhase.PATTERN; // go to next phase
		}

	}

	/*
	 * PATTERN phase
	 * This phase marks all lines for clearance in the ELIMINATE phase.
	 */
	private void patternPhase() {
		//System.out.println("Enter PATTERN phase");
		
		// look for LINE CLEAR
		_playfield.markLinesToBeCleared();
		
		// currently no other patterns to look for 
		
		_phaseState = TetrisPhase.ITERATE;
	}

	/*
	 * ELIMINATE phase
	 * This phase removes all lines from the playfield which were marked for clearance.<br/>
	 * Also handles game statistics like scoring, bonus scores, etc.
	 * TODO: BackToBack Bonus, SPIN Bonus
	 */
	private void eliminatePhase() {

		// clear lines
		_lastClearedLinesCount = _playfield.clearMarkedLines();
		
		// score
		_score += calculateLineClearScore(_lastClearedLinesCount);
		_score += _lastSoftDropLineCount; // soft drop points 1 x number of lines
		_score += _lastHardDropLineCount * 2; // hard drop points 2 x number of lines
		_lineCount += _lastClearedLinesCount;
		_lastHardDropLineCount = 0;
		_lastSoftDropLineCount = 0;
		_lastClearedLinesCount = 0;

		// other statistics
		if (_lastClearedLinesCount == 4) _tetrisesCount++;
				
		_phaseState = TetrisPhase.COMPLETION;
	}

	/*
	 * COMPLETION phase
	 * This is where any updates to information fields on the Tetris playfield are updated, such as the Score and Time. 
	 * The Level Up condition is also checked to see if it is necessary to advance the game level.
	 */
	private void completionPhase() {
		//System.out.println("Enter COMPLETION phase");
		
		// set level - FIXED GOAL SYSTEM
		// TODO: implement VARIABLE GOAL SYSTEM
		if (_lineCount > 0 && _lastClearedLinesCount > 0) {
			_currentLevel = _lineCount/10 +1;
		}
		
		_phaseState = TetrisPhase.GENERATION;
	}

	/*
	 * @param numberOfClearedLines
	 * @return score for the last placement
	 */
	private int calculateLineClearScore(int numberOfClearedLines) {
		int score = 0;
		switch (numberOfClearedLines) {
		case 0: break;
		case 1: score += 100 * _currentLevel;
		case 2: score += 300 * _currentLevel;
		case 3: score += 500 * _currentLevel;
		case 4: score += 800 * _currentLevel;
		}
		return score;
	}

	/*
	 * @return the falling time for the current level
	 */
	private long calculateFallingTime() {
		switch (_currentLevel) {
		case 1: return 1000; 
		case 2: return 793;
		case 3: return 618;
		case 4: return 473;
		case 5: return 355;
		case 6: return 262;
		case 7: return 190;
		case 8: return 135;
		case 9: return 94;
		case 10: return 64;
		case 11: return 43;
		case 12: return 28;
		case 13: return 18;
		case 14: return 11;
		case 15: return 7;
		}
		return 1000;
	}
	
	/**
	 * This is called from the ui to add control events (e.g. key press) to our queue.
	 * @param e
	 */
	public void controlQueueAdd(TetrisControlEvents e) {
		_controlQueue.add(e);
	}

	/**
	 * This is called from the timer to wake us from waiting for a key event
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
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
					synchronized (_gameThread) {
						_gameThread.wait();
					}
					//Thread.sleep(1000);
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
		synchronized (_gameThread) {
			_gameThread.notify();
		}
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

	/**
	 * @return the Tetrimino in hold
	 */
	public Tetrimino getHoldTetrimino() {
		return _holdQueue;
	}

	/**
	 * @return the _startLevel
	 */
	public int getStartLevel() {
		return _startLevel;
	}

	/**
	 * @param _startLevel the _startLevel to set
	 */
	public void setStartLevel(int _startLevel) {
		this._startLevel = _startLevel;
	}

	/**
	 * @return the _currentLevel
	 */
	public int getCurrentLevel() {
		return _currentLevel;
	}

	/**
	 * @return the _score
	 */
	public int getScore() {
		return _score;
	}

	/**
	 * @return the _lineCount
	 */
	public int getLineCount() {
		return _lineCount;
	}

	/**
	 * @return the _tetrisesCount
	 */
	public int getTetrisesCount() {
		return _tetrisesCount;
	}

}
