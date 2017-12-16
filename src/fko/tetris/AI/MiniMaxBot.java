package fko.tetris.AI;

import java.util.Random;

import fko.tetris.game.Playfield;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;

public class MiniMaxBot extends AbstractBot {

	public MiniMaxBot(TetrisGame game) {
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
		
		Random rand = new Random();

		for(int i=0;i<10;i++) { // 5 moves sideways
			if (rand.nextInt(2) == 1) {
				_game.controlQueueAdd(TetrisControlEvents.LEFT);
			} else {
				_game.controlQueueAdd(TetrisControlEvents.RIGHT);
			}
			if (rand.nextInt(2) == 1) {
				_game.controlQueueAdd(TetrisControlEvents.LEFT);
			} else {
				_game.controlQueueAdd(TetrisControlEvents.RIGHT);
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}

		// finally a hardrop
		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);
	}

}
