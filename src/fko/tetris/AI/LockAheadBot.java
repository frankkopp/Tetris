package fko.tetris.AI;

import java.util.ArrayList;
import java.util.List;

import fko.tetris.game.Playfield;
import fko.tetris.game.TetrisColor;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;
import fko.tetris.tetriminos.Tetrimino;

public class LockAheadBot extends AbstractBot {
	
	private static final int MAX_VISIBLE_NEXTQUEUE = 3;
	
	private List<Tetrimino> _nextQueue = new ArrayList<Tetrimino>(); 

	public LockAheadBot(TetrisGame game) {
		super(game);
		System.out.println("MINIMAX BOT CREATED");
	}

	@Override
	public void run() {
		System.out.println("MINIMAX BOT STARTED");
		while (!Thread.interrupted()) {
			try {

				final TetrisPhase phaseState = _game.getPhaseState();
				System.out.println(phaseState.toString());
				
				switch(phaseState) {
				case FALLING: {
					_nextQueue.clear();
					for (int i=0; i<=MAX_VISIBLE_NEXTQUEUE; i++) {
						_nextQueue.add(_game.getNextQueue().get(i));
					}
					placeTetrimino();
					break;
				}
				case GAMEOVER: Thread.currentThread().interrupt(); break;
				default: break;
				}
				
				// slow down a bit
				Thread.sleep(100);

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
		int best_score = 0;

		// for each permutation of move position and turn position get a score
		for (int turn=0; turn<4; turn++) {
			Playfield pf = _game.getPlayfield().clone();
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
				Playfield pfm = pf.clone();
				// move right
				for (int i=0; i<m-moveL; i++) {
					pfm.moveSideway(1);
				}
				pfm.drop();
				pfm.merge();
				pfm.markLinesToBeCleared();
				int clearedLines = pfm.clearMarkedLines();
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

		System.out.println("Best Score: "+best_score);
		System.out.println("Turn: "+best_turn+" Move: "+best_move);
		
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
		
		// finally a hard drop
		System.out.println("BOT MAKES MOVE");
		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);
	}

	private int bruteForceTree(Playfield playfield, int nextQueueIndex) {
		if (nextQueueIndex >= MAX_VISIBLE_NEXTQUEUE) {
			final int score =  evalutation(playfield);
			return score;
		}
		
		int best_score = 0;
		
		for (int turn=0; turn<4; turn++) {
			Playfield pf = playfield.clone();
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
				Playfield pfm = pf.clone();
				// move right
				for (int i=0; i<m-moveL; i++) {
					pfm.moveSideway(1);
				}
				pfm.drop();
				pfm.merge();
				pfm.markLinesToBeCleared();
				int clearedLines = pfm.clearMarkedLines();
				if (pfm.spawn(_nextQueue.get(nextQueueIndex).clone())) {
					return -99; // game_over
				};
				
				final int score = bruteForceTree(pfm, nextQueueIndex+1);
				if (score > best_score) best_score = score;
				
			}
		}
		return best_score;
	}
	
	private int evalutation(Playfield pf) {
		int score = 0;
		
		// highest tetrimino - points 20 - highest y
		for (int y=0; y<pf.SKYLINE+pf.BUFFERZONE; y++) {
			boolean found = false;
			for (int x=0; x<pf.PLAYFIELD_WIDTH;x++) {
				if (pf.getCell(x,y) != TetrisColor.EMPTY) {
					found=true;
					break;
				}
			}
			if (!found) { // we did not find a Mino in this row so this was the highest. 
				score += 20 - y;
				break;
			}
		}
		
//		pf.debugPrintMatrix();
//		System.out.println("SCORE: "+score);
		return score;
	}
	
}
