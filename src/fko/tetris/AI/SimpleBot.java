package fko.tetris.AI;

import fko.tetris.game.TetrisGame;

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
				System.out.println(_game.getPhaseState().toString());
				System.out.println(_game.getPlayfield().toString());
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				break;
			}
		}
		System.out.println("SIMPLE BOT STOPPED");
	}

}
