package fko.tetris.AI;

import java.util.Random;

import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;

/**
 * A basic implementation of a bot to focus on the base functionality of a bot.
 * Picks random moves. 
 */
public class SimpleBot extends AbstractBot {

	public SimpleBot(TetrisGame game) {
		super(game);
		System.out.println("SIMPLE BOT CREATED");
	}

	@Override
	public void run() {
		System.out.println("SIMPLE BOT STARTED");
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
				
				// System.out.println(_game.getPlayfield().toString());

				// do something
				Thread.sleep(100);

			} catch (InterruptedException e) {
				break;
			}
		}
		System.out.println("SIMPLE BOT STOPPED");
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
