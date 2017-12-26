package fko.tetris.AI;

import java.util.ArrayList;
import java.util.List;

import fko.tetris.game.Matrix;
import fko.tetris.game.TetrisColor;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;
import fko.tetris.tetriminos.Tetrimino;

/**
 * A bit capable of looking several Tetriminos ahead using the NextQueue<br>
 * It evaluates absolute height, aggregated height, unevenness, holes, blocker (Minos over holes).<br>
 * It can't play with a lookahead of 3+ in higher levels (12+) as it takes too long to calculate.<br>
 * 
 * TODO: Optimize performance to be able to look ahead more
 * TODO: Use LOCK moves to lose holes (side movements when Tetrimino locks)
 * TODO: Optimize for Score instead only height
 */
public class LockAheadBot extends AbstractBot {

	private static final int MAX_VISIBLE_NEXTQUEUE = 2;

	private List<Tetrimino> _nextQueue = new ArrayList<Tetrimino>(); 

	private int _numberOfEvaluations = 0;

	/**
	 * Creates a bot with a handle to the game
	 * @param game
	 */
	public LockAheadBot(TetrisGame game) {
		super(game);
	}

	/**
	 * Run the bot as long as Thread is not interrupted. 
	 */
	@Override
	public void run() {
		boolean moveDone = false;
		while (!Thread.interrupted()) {
				final TetrisPhase phaseState = _game.getPhaseState();
				switch(phaseState) {
				// we can only move when we are in FALLING phase
				case FALLING: {
					if (!moveDone) {
						long time = System.nanoTime();
						_numberOfEvaluations = 0;
						_nextQueue.clear();
						// copy the nextQueue into a list
						for (int i = 0; i <= MAX_VISIBLE_NEXTQUEUE; i++) {
							_nextQueue.add(_game.getNextQueue().get(i));
						}
						// calculate the best position and place Tetrimino
						placeTetrimino();
						moveDone = true;
						System.out.println(String.format("Bot took %,10d ns", (System.nanoTime() - time)));
						System.out.println();
					}
					break;
				}
				// we we reset our moveDone flag during LOCK phase but do nothing else for now in LOCK phase
				case LOCK: moveDone=false; break;
				// game over stops thread
				case GAMEOVER: moveDone=false; Thread.currentThread().interrupt(); break;
				default: moveDone=false; break;
				}
		}
	}

	/*
	 * Calculate the control commands for playing Tetris 
	 */
	private void placeTetrimino() {

		int best_turn = 0;
		int best_move = 0;
		int best_score = Integer.MIN_VALUE;
		
		// make a copy of the playfield as the game playfield could move on in the meantime
		// also we do want to change the original Matrix
		Matrix myMatrix = _game.getMatrix().clone();

		// for each relevant permutation of move position and turn position get a score
		// avoid symmetrical permutations to save some time
		int turns = 4;
		switch(myMatrix.getCurrentTetrimino().toString()) {
			case "O": turns=1; break; // no turns needed - similar for all permutations
			case "Z":
			case "S":
			case "I": turns=2; break; // only needs the base turn and a 90° turn to cover all permutations
			case "T":
			case "J":
			case "L": 
			default: turns=4; break; // need 4 turns for all permutations
		}
		for (int turn=0; turn<turns; turn++) {
			Matrix myMatrixCopy = myMatrix.clone(); // copy to not change the original
			for (int i=0; i<turn; i++) myMatrixCopy.turnMove(1); // make the turn
			// determine max moves right and left
			int moveR=0;
			while (!myMatrixCopy.moveSideway(1)) {
				moveR++;
			}
			int moveL=moveR;
			while (!myMatrixCopy.moveSideway(-1)) {
				moveL--;
			}
			// now we are on the left - for each position horizontally make the drop a call recursive function
			for (int m = moveL; m <= moveR; m++) {
				Matrix myMatrixCopy2 = myMatrixCopy.clone(); // copy to not change the preserve to source copy
				// move right
				for (int i=0; i<m-moveL; i++) {
					myMatrixCopy2.moveSideway(1);
				}
				myMatrixCopy2.drop();
				myMatrixCopy2.merge();
				myMatrixCopy2.markLinesToBeCleared();
				myMatrixCopy2.clearMarkedLines();
				if (myMatrixCopy2.spawn(_nextQueue.get(0).clone())) {
					return; // game over
				}
				
				int score = bruteForceTree(myMatrixCopy2, 1);
				
				if (score > best_score) {
					best_turn = turn;
					best_move = m;
					best_score = score;
				}
			}
		}

		// now turn to the best position on the real matrix
		for (int i=0; i<best_turn; i++) {
			_game.controlQueueAdd(TetrisControlEvents.RTURN);
		}

		// now move to the best position on the real matrix
		for (int i=0; i < Math.abs(best_move); i++) {
			if (best_move < 0) {
				_game.controlQueueAdd(TetrisControlEvents.LEFT);
			} else if (best_move > 0) {
				_game.controlQueueAdd(TetrisControlEvents.RIGHT);
			}
		}
		
		// finally drop on the Tetrimino on the real matrix
		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);

		System.out.println("TETRIMINO: "+myMatrix.getCurrentTetrimino());
		System.out.println("BEST TURN: "+best_turn+" BEST MOVE: "+best_move);
		System.out.println("BEST SCORE: "+best_score);
		System.out.println(String.format("Evaluations: %,d",_numberOfEvaluations));
		System.out.println(">>>>>>>>>>>>>>>>>>>> BOT MAKES MOVE <<<<<<<<<<<<<<<<<<<<<<<<<<");
		
	}

	private int bruteForceTree(Matrix myMatrix, int nextQueueIndex) {
		if (Thread.currentThread().isInterrupted()) return Integer.MIN_VALUE;

		if (nextQueueIndex >= MAX_VISIBLE_NEXTQUEUE) {
			return evalutation(myMatrix);
		}

		int best_score = Integer.MIN_VALUE;

		for (int turn=0; turn<4; turn++) {
			Matrix myMatrixCopy = myMatrix.clone(); // make a copy to preserve the original state
			for (int i=0; i<turn; i++) myMatrixCopy.turnMove(1); // make the turn
			// determine max moves right and left
			int moveR=0;
			while (!myMatrixCopy.moveSideway(1)) {
				moveR++;
			}
			int moveL=moveR;
			while (!myMatrixCopy.moveSideway(-1)) {
				moveL--;
			}
			// now we are on the left - for each position horizontally make the drop a call recursive function
			for (int m = moveL; m <= moveR; m++) {
				Matrix myMatrixCopy2 = myMatrixCopy.clone(); // make a copy to preserve the original state
				// move right
				for (int i=0; i<m-moveL; i++) {
					myMatrixCopy2.moveSideway(1);
				}
				myMatrixCopy2.drop();
				myMatrixCopy2.merge();
				myMatrixCopy2.markLinesToBeCleared();
				myMatrixCopy2.clearMarkedLines();
				int score = 0;
				if (myMatrixCopy2.spawn(_nextQueue.get(nextQueueIndex).clone())) {
					score += Integer.MIN_VALUE; // game_over
				} else {
					score += bruteForceTree(myMatrixCopy2, nextQueueIndex+1);
				};
				if (score > best_score) best_score = score;
			}
		}
		return best_score;
	}

	private static final double weightabsolutHeight = -1.0;
	private static final double weightAggregatedHeight = -1.0;
	private static final double weightUnevenness = -1.0;
	private static final double weightHoles = -2.0;
	private static final double weightBlocker = -3.0;
	
	private int evalutation(Matrix myMatrix) {
		_numberOfEvaluations++;
		int score = 0;
		int [] result = scanFieldEvaluations(myMatrix);
		score += weightabsolutHeight		* result[0];  
		score += weightAggregatedHeight	* result[1];
		score += weightUnevenness		* result[2];
		score += weightHoles				* result[3];
		score += weightBlocker			* result[4];
		//System.out.println("Eval: "+myMatrix.getLastTetrimino().toString()+" Score: "+score);
		return score;
	}

	/*
	 * scans the whole field and calculates various evaluations and returns the as an array
	 * { absolute height, aggregated height, unevenness, holes, unwanted blocker }
	 */
	private int[] scanFieldEvaluations(Matrix myMatrix) {
		int aggregatedHeight = 0;
		int absoluteHeight = 0;
		int lastHeight = 0;
		int unevenness = 0;
		int holes = 0;
		int blocker = 0;
		int height = 0;

		// scan the field once and do all evaluations which need to scan the whole matrix
		// to avoid scanning the matrix multiple times
		for (int x=0; x<Matrix.MATRIX_WIDTH;x++) {
			int possibleHoles = 0;
			height = 0;
			int tmpHoles = 0;
			int tmpBlocker = 0;
			for (int y=0; y<Matrix.PLAYFIELD_HEIGHT; y++) {
				if (myMatrix.getCell(x, y) != TetrisColor.EMPTY) {
					height = y+1; 
					if (possibleHoles > 0) {
						tmpHoles += possibleHoles;
						possibleHoles = 0;
						tmpBlocker++;
					}
				} else {
					possibleHoles++;
				}
			}
			if (tmpHoles > 0 && tmpBlocker > 0) {
				holes += tmpHoles;
				blocker += tmpBlocker;
			}
			aggregatedHeight += height;
			unevenness += Math.abs(lastHeight-height);
			lastHeight = height;
			if (height > absoluteHeight) absoluteHeight = height;
		}
		unevenness += Math.abs(lastHeight-0);
		
//		pf.debugPrintMatrix();
//		System.out.println("AGGREGATED HEIGHT: "+aggregatedHeight);
//		System.out.println("ABSOLTUE HEIGHT: "+absoluteHeight);
//		System.out.println("UNEVENNESS: "+unevenness);
//		System.out.println("HOLES: "+holes);
//		System.out.println("BLOCKER: "+blocker);

		return new int[] {
				aggregatedHeight, 
				absoluteHeight, // to emphasize height 
				unevenness,
				holes,
				blocker};
	}

}
