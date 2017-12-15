/**
 * 
 */
package fko.tetris.AI;

import fko.tetris.game.TetrisGame;

/**
 *
 */
public abstract class AbstractBot implements Bot, Runnable {

	protected TetrisGame _game;

	public AbstractBot(TetrisGame game) {
		this._game=game;
	}

	// Thread
	private Thread _botThread = null;

	@Override
	public void startBot() {
		// we call this method so that this method can be overwritten by subclasses
		if (_botThread == null) {
			_botThread = new Thread(this, "Bot Thread");
			_botThread.setDaemon(true);
			_botThread.start();
			System.out.println("Bot started");
		}
	}

	@Override
	public void stopBot() {
		System.out.println("Bot stopped");
		if (_botThread != null) {
			_botThread.interrupt();
        }
    }

	public abstract void run();

}
