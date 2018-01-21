/** */
package fko.tetris.AI;

import fko.tetris.game.TetrisGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of a Bot. This implementation handles the thread control by starting
 * and stopping a thread. It implements <code>Runnable</code> and when starting a thread it calls
 * the <code>run()</code> method. <br>
 * The thread is stopped by calling the Bot-Thread's <code>.interrupt()</code> method. So
 * implementing classes have to query the interrupted status of the current thread in their run()
 * method if running in a loop to be able to stop the Bot.
 */
public abstract class AbstractBot implements Bot, Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractBot.class);

  protected TetrisGame _game;

  public AbstractBot(TetrisGame game) {
    this._game = game;
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
      LOG.info("Bot thread started");
    }
  }

  @Override
  public void stopBot() {
    if (_botThread != null) {
      _botThread.interrupt();
      LOG.info("Bot thread stopped");
    }
  }

  public abstract void run();
}
