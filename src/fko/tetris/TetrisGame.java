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

	private TetrisTimer _fallingTimer; // timer to control falling time
	private TetrisTimer _lockTimer;		// timer to control lock time
	
	private LinkedBlockingQueue<TetrisControlEvents> _controlQueue = new LinkedBlockingQueue<>();
		
	private int _numberOfLinesToBeCleared; // stores the number of lines to be cleared after the PATTERN phase
	private int _numberOfLastClearedLines;
	
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

		completionPhase();
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

			// TODO: improve to also pause during the phases
			waitIfPaused();

		} while (_gameStopped == false);

		// -- tell the view that model has changed
		setChanged();
		notifyObservers("Game Thread stopped");
	}

	/*
	 * 
	 */
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

	/*
	 * 
	 */
	private void fallingPhase() {
		
		// Start falling timer
		// TODO: make time depended of level
		_fallingTimer = new TetrisTimer(calculateFallingTime());
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

	/*
	 * Runs id state is LOCK
	 * Implements the INFINITE PLACEMENT LOCK DOWN	
	 * TODO: Implement EXTENDED LOCK DOWN and CLASSIC LOCKDOWN
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

	/*
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
	 * This phase removes all lines from the playfield which were marked for clearance.<br/>
	 * Also handles game statistics like scoring, bonus scores, etc.
	 * no score for SOFTDROP and HARDDROP
	 * TODO: BackToBack Bonus, SPIN Bonus
	 * 
	 */
	private void eliminatePhase() {
		//System.out.println("Enter ELIMINATE phase");
		
		_numberOfLastClearedLines = _playfield.clearMarkedLines();
		
		_score += calculateNewScore(_numberOfLastClearedLines);
		_lineCount += _numberOfLastClearedLines;
		if (_numberOfLastClearedLines == 4) _tetrisesCount++;
				
		_phaseState = TetrisPhase.COMPLETION;
	}

	/*
	 * This is where any updates to information fields on the Tetris playfield are updated, such as the Score and Time. 
	 * The Level Up condition is also checked to see if it is necessary to advance the game level.
	 */
	private void completionPhase() {
		//System.out.println("Enter COMPLETION phase");
		
		// TODO
		if (_lineCount > 0 && _numberOfLastClearedLines > 0 && _lineCount%10 == 0)_currentLevel++;
		
		_phaseState = TetrisPhase.GENERATION;
	}

	/**
	 * @param numberOfClearedLines
	 * @return
	 */
	private int calculateNewScore(int numberOfClearedLines) {
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

	/**
	 * @return
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

	/**
	 * @return the _startLevel
	 */
	public int get_startLevel() {
		return _startLevel;
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
