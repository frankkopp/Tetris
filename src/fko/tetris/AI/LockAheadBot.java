package fko.tetris.AI;

import fko.tetris.game.Playfield;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;

public class LockAheadBot extends AbstractBot {

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
				final Playfield playfield = _game.getPlayfield();
				System.out.println(phaseState.toString());

				switch(phaseState) {
				case FALLING: {
					placeTetrimino();
					break;
				}
				case GAMEOVER: Thread.currentThread().interrupt(); break;
				default: break;
				}
				
				// System.out.println(_game.getPlayfield().toString());

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

		// make copy playfield
		Playfield copy = _game.getPlayfield().clone();
		
		// generate all permutations for current tetrimino
		// and make the drop,
		
		
		// then recursively do this for all nextQueue Tetriminos
		// therefore generation a tree of all possibilities for the the visible 
		// Tetriminos.
		
		// call an evaluation function to play the best possible drop 
		
		

		// finally a hardrop
		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);
	}

}
