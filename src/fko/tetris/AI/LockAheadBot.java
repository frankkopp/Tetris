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
 * TODO: Optimize for Score instead only maximum clearing
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
		System.out.println("MINIMAX BOT CREATED");
	}

	/**
	 * run the bot as long as Thread is not interrupted. 
	 */
	@Override
	public void run() {
		System.out.println("MINIMAX BOT STARTED");
		
		while (!Thread.interrupted()) {
			try {

				final TetrisPhase phaseState = _game.getPhaseState();

				switch(phaseState) {
				case FALLING: {
//					long time = System.currentTimeMillis();
					_numberOfEvaluations = 0;
					_nextQueue.clear();
					for (int i=0; i<=MAX_VISIBLE_NEXTQUEUE; i++) {
						_nextQueue.add(_game.getNextQueue().get(i));
					}
					placeTetrimino();
//					System.out.println("Bot took "+HelperTools.formatTime(System.currentTimeMillis()-time, true));
					break;
				}
				case GAMEOVER: Thread.currentThread().interrupt(); break;
				default: break;
				}

				// slow down a bit
				Thread.sleep(0);

			} catch (InterruptedException e) {
				break;
			}
		}
		System.out.println("MINIMAX BOT STOPPED");
	}

	/*
	 * Calculate the control commands for playing Tetris 
	 */
	private void placeTetrimino() {

		int best_turn = 0;
		int best_move = 0;
		int best_score = Integer.MIN_VALUE;

		// for each permutation of move position and turn position get a score
		for (int turn=0; turn<4; turn++) {
			Matrix pf = _game.getPlayfield().clone();
			for (int i=0; i<turn; i++) pf.turnMove(1); // make the turn
			// determine max moves right and left
			int moveR=0;
			while (!pf.moveSideway(1)) {
				moveR++;
			}
			int moveL=moveR;
			while (!pf.moveSideway(-1)) {
				moveL--;
			}
			// now we are on the left - for each position horizontally make the drop a call recursive function
			for (int m = moveL; m <= moveR; m++) {
				Matrix pfm = pf.clone();
				// move right
				for (int i=0; i<m-moveL; i++) {
					pfm.moveSideway(1);
				}
				pfm.drop();
				pfm.merge();
				pfm.markLinesToBeCleared();
				pfm.clearMarkedLines();
				if (pfm.spawn(_nextQueue.get(0).clone())) {
					return; // game over
				}
				int score = bruteForceTree(pfm, 1);

				if (score > best_score) {
					best_turn = turn;
					best_move = m;
					best_score = score;
				}
			}
		}

		// now turn to the best position
		for (int i=0; i<best_turn; i++) {
			_game.controlQueueAdd(TetrisControlEvents.RTURN);
		}

		// now move to the best position
		for (int i=0; i < Math.abs(best_move); i++) {
			if (best_move < 0) {
				_game.controlQueueAdd(TetrisControlEvents.LEFT);
			} else if (best_move > 0) {
				_game.controlQueueAdd(TetrisControlEvents.RIGHT);
			}
		}
		
		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);

		// finally a hard drop
//		System.out.println("BEST TURN: "+best_turn+" BEST MOVE: "+best_move);
//		System.out.println("BEST SCORE: "+best_score);
//		System.out.println(String.format("Evaluations: %,d",_numberOfEvaluations));
//		System.out.println(">>>>>>>>>>>>>>>>>>>> BOT MAKES MOVE <<<<<<<<<<<<<<<<<<<<<<<<<<");
		
	}

	private int bruteForceTree(Matrix playfield, int nextQueueIndex) {
		if (Thread.currentThread().isInterrupted()) return Integer.MIN_VALUE;

		if (nextQueueIndex >= MAX_VISIBLE_NEXTQUEUE) {
			return evalutation(playfield);
		}

		int best_score = Integer.MIN_VALUE;

		for (int turn=0; turn<4; turn++) {
			Matrix pf = playfield.clone();
			for (int i=0; i<turn; i++) pf.turnMove(1); // make the turn
			// determine max moves right and left
			int moveR=0;
			while (!pf.moveSideway(1)) {
				moveR++;
			}
			int moveL=moveR;
			while (!pf.moveSideway(-1)) {
				moveL--;
			}
			// now we are on the left - for each position horizontally make the drop a call recursive function
			for (int m = moveL; m <= moveR; m++) {
				Matrix pfm = pf.clone();
				// move right
				for (int i=0; i<m-moveL; i++) {
					pfm.moveSideway(1);
				}
				pfm.drop();
				pfm.merge();
				pfm.markLinesToBeCleared();
				pfm.clearMarkedLines();
				int score = 0;
				//score += (clearedLines-1)*2;
				if (pfm.spawn(_nextQueue.get(nextQueueIndex).clone())) {
					score += Integer.MIN_VALUE; // game_over
				} else {
					score += bruteForceTree(pfm, nextQueueIndex+1);
				};
				if (score > best_score) best_score = score;
			}
		}
		return best_score;
	}

	private int evalutation(Matrix pf) {

		_numberOfEvaluations++;

//		if (_numberOfEvaluations%100000 == 0) {
//			System.out.println(String.format("%,d",_numberOfEvaluations));
//		}

		int score = 0;

		final double weightabsolutHeight 	= -1.0;	// Absolute height - higher is worse
		final double weightAggregatedHeight 	= -1.0;	// Aggregated height - higher is worse
		final double weightUnevenness 		= -1.0;	// Unevenness of stacks - higher is worse
		final double weightHoles				= -2.0;	// Holes - higher is worse
		final double weightBlocker			= -3.0;	// Minos blocking a hole - higher is worse

		//pf.debugPrintMatrix();

		int [] result = scanFieldEvaluations(pf);

		score += weightabsolutHeight		* result[0]; // more weight to height if higher 
		score += weightAggregatedHeight	* result[1];
		score += weightUnevenness		* result[2];
		score += weightHoles				* result[3];
		score += weightBlocker			* result[4];

//		System.out.println("SCORE: "+score);

		return score;
	}


	/*
	 * scans the whole field and calculates various evaluations and returns the as an array
	 * { absolute height, aggregated height, unevenness, holes, unwanted blocker }
	 */
	private int[] scanFieldEvaluations(Matrix pf) {
		int aggregatedHeight = 0;
		int absoluteHeight = 0;
		int unevennessLast = 0;
		int unevenness = 0;
		int holes = 0;
		int blocker = 0;

		// scan the field once and do all evaluations which need to scan the whole matrix
		// to avoid scanning the matrix multiple times
		for (int x=0; x<Matrix.PLAYFIELD_WIDTH;x++) {
			int lastHeigth = 0;
			int possibleHoles = 0;
			int tmpHoles = 0;
			int tmpBlocker = 0;
			for (int y=0; y<Matrix.PLAYFIELD_HEIGHT; y++) {
				if (pf.getCell(x, y) != TetrisColor.EMPTY) {
					lastHeigth = y+1; 
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
			aggregatedHeight += lastHeigth;
			unevenness += Math.abs(unevennessLast-lastHeigth);
			unevennessLast = lastHeigth;
			if (lastHeigth > absoluteHeight) absoluteHeight = lastHeigth;
		}

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
