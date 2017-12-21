package fko.tetris.AI;

import fko.tetris.game.Playfield;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;

public class LockAheadBot extends AbstractBot {
	
	private static final int MAX_VISIBLE_NEXTQUEUE = 1;

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
					placeTetrimino();
					break;
				}
				case GAMEOVER: Thread.currentThread().interrupt(); break;
				default: break;
				}
				
				// do something
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

		// for each permutation of move position and turn position get a score
		int best_turn = 0;
		int best_move = 0;
		int best_score = 0;
		
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
				pfm.drop();
				pfm.merge();
				pfm.markLinesToBeCleared();
				int clearedLines = pfm.clearMarkedLines();
				pfm.spawn(_game.getNextQueue().get(0).clone());
				int score = bruteForceTree(pfm, 1);
			}
		}
	
		// finally a hard drop
		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);
	}

	private int bruteForceTree(Playfield playfield, int nextQueueIndex) {
		if (nextQueueIndex >= MAX_VISIBLE_NEXTQUEUE) {
			int score =  evalutation(playfield);
		}
		
		for (int turn=0; turn<4; turn++) {
			Playfield pf = playfield;
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
				pfm.drop();
				pfm.merge();
				pfm.markLinesToBeCleared();
				int clearedLines = pfm.clearMarkedLines();
				pfm.spawn(_game.getNextQueue().get(0).clone());
				int score = bruteForceTree(pfm, 1);
			}
		}
		return 0;
	}
	
	private int evalutation(Playfield pf) {
		return 0;
	}
	
}
