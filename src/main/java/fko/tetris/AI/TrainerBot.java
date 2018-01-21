package fko.tetris.AI;

import fko.tetris.game.Matrix;
import fko.tetris.game.TetrisGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation creates a TrainingSet file for training a Neural Network later.<br>
 * A Bot capable of looking several Tetriminos ahead using the NextQueue<br>
 * It evaluates absolute height, aggregated height, unevenness, holes, blocker (Minos over holes).
 * <br>
 * It can't play with a lookahead of 3+ in higher levels (12+) as it takes too long to calculate.
 * <br>
 * TODO: Optimize performance to be able to look ahead more
 * TODO: Use LOCK moves to lose holes (side movements when Tetrimino locks)
 * TODO: Optimize for Score instead only height
 */
public class TrainerBot extends LockAheadBot {

  private static final Logger LOG = LoggerFactory.getLogger(TrainerBot.class);

  private final TrainingData _trainingData = new TrainingData();

  /**
   * Creates a bot with a handle to the game
   *
   * @param game
   */
  public TrainerBot(final TetrisGame game) {
    super(game);
  }

  protected void saveTrainingData(final int best_turn, final int best_move, final Matrix myMatrix) {
    long time = System.nanoTime();
    _trainingData.appendEntry(
        myMatrix.clone(),
        myMatrix.getCurrentTetrimino().clone(),
        nextQueue.get(0).clone(),
        best_turn,
        best_move);
    LOG.info("SAVE took {} ms", (System.nanoTime()-time)/1e6f);
  }
}
