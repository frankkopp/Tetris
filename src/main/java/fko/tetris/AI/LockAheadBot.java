package fko.tetris.AI;

import java.util.ArrayList;
import java.util.List;

import fko.tetris.game.Matrix;
import fko.tetris.game.TetrisColor;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;
import fko.tetris.tetriminos.Tetrimino;
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
public class LockAheadBot extends AbstractBot {

  private static final Logger LOG = LoggerFactory.getLogger(LockAheadBot.class);

  private static final int MAX_VISIBLE_NEXTQUEUE = 2;

  final List<Tetrimino> nextQueue = new ArrayList<Tetrimino>();

  private int numberOfSpawns = 0;
  private int numberOfEvaluations = 0;

  /**
   * Creates a bot with a handle to the game
   *
   * @param game
   */
  public LockAheadBot(TetrisGame game) {
    super(game);
  }

  /** Run the bot as long as Thread is not interrupted. */
  @Override
  public void run() {
    LOG.info("{} started.",this.getClass().getName());
    boolean moveDone = false; // to prevent several calculations during the falling phase
    while (!Thread.interrupted()) {
      final TetrisPhase phaseState = _game.getPhaseState();
      switch (phaseState) {
          // we can only move when we are in FALLING phase
        case LOCK: // we can still move during LOCK - this is helpful in higher levels when game is
          // really fast
        case FALLING:
          {
            if (!moveDone) {
              long time = System.nanoTime();
              numberOfEvaluations = 0;
              numberOfSpawns++;
              nextQueue.clear();
              // copy the nextQueue into a list
              for (int i = 0; i <= MAX_VISIBLE_NEXTQUEUE; i++) {
                nextQueue.add(_game.getNextQueue().get(i));
              }
              // calculate the best position and place Tetrimino
              placeTetrimino();
              moveDone = true;
              LOG.info(String.format("Bot took %,10d ns", (System.nanoTime() - time)));
            }
            break;
          }
          // we we reset our moveDone flag during LOCK phase but do nothing else for now in LOCK
          // phase
          // game over stops thread
        case GAMEOVER:
          moveDone = false;
          Thread.currentThread().interrupt();
          break;
        default:
          moveDone = false;
          break;
      }
    }
    LOG.info("{} stopped.",this.getClass().getName());
  }

  /*
   * Calculate the control commands for playing Tetris
   */
  private void placeTetrimino() {

    int best_turn = 0;
    int best_move = 0;
    int best_score = Integer.MIN_VALUE;

    // make a copy of the playfield as the game playfield could move on in the meantime
    // also we do want to change the original Matrix
    Matrix myMatrix = _game.getMatrix().clone();

    // for each relevant permutation of move position and turn position get a score
    // avoid symmetrical permutations to save some time
    int turns = 4;
    switch (myMatrix.getCurrentTetrimino().toString()) {
      case "O":
        turns = 1;
        break; // no turns needed - similar for all permutations
      case "Z":
      case "S":
      case "I":
        turns = 2;
        break; // only needs the base turn and a 90° turn to cover all permutations
      case "T":
      case "J":
      case "L":
      default:
        turns = 4;
        break; // need 4 turns for all permutations
    }
    for (int turn = 0; turn < turns; turn++) {
      Matrix myMatrixCopy = myMatrix.clone(); // copy to not change the original
      for (int i = 0; i < turn; i++) myMatrixCopy.turnMove(1); // make the turn
      // determine max moves right and left
      int moveR = 0;
      while (!myMatrixCopy.moveSideway(1)) {
        moveR++;
      }
      int moveL = moveR;
      while (!myMatrixCopy.moveSideway(-1)) {
        moveL--;
      }
      // now we are on the left - for each position horizontally make the drop a call recursive
      // function
      for (int m = moveL; m <= moveR; m++) {
        Matrix myMatrixCopy2 =
            myMatrixCopy.clone(); // copy to not change the preserve to source copy
        // move right
        for (int i = 0; i < m - moveL; i++) {
          myMatrixCopy2.moveSideway(1);
        }
        myMatrixCopy2.drop();
        myMatrixCopy2.merge();
        myMatrixCopy2.markLinesToBeCleared();
        myMatrixCopy2.clearMarkedLines();
        if (myMatrixCopy2.spawn(nextQueue.get(0).clone())) {
          return; // game over
        }

        int score = bruteForceTree(myMatrixCopy2, 1);

        if (score > best_score) {
          best_turn = turn;
          best_move = m;
          best_score = score;
        } else if (score == best_score && Math.random() < 0.5) {
          best_turn = turn;
          best_move = m;
          best_score = score;
        }
      }
    }

    // now turn to the best position on the real matrix
    for (int i = 0; i < best_turn; i++) {
      _game.controlQueueAdd(TetrisControlEvents.RTURN);
    }

    // now move to the best position on the real matrix
    for (int i = 0; i < Math.abs(best_move); i++) {
      if (best_move < 0) {
        _game.controlQueueAdd(TetrisControlEvents.LEFT);
      } else if (best_move > 0) {
        _game.controlQueueAdd(TetrisControlEvents.RIGHT);
      }
    }

    // finally drop on the Tetrimino on the real matrix
    _game.controlQueueAdd(TetrisControlEvents.HARDDOWN);

    saveTrainingData(best_turn, best_move, myMatrix);

    LOG.info("TETRIMINO: {}", myMatrix.getCurrentTetrimino());
    LOG.info("BEST TURN {} BEST MOVE: {}", best_turn, best_move);
    LOG.info("BEST SCORE: {}", best_score);
    LOG.info(String.format("Spawn Nr: %,d Evaluations: %,d", numberOfSpawns, numberOfEvaluations));
    LOG.info(">>>>>>>>>>>>>>>>>>>> BOT MAKES MOVE <<<<<<<<<<<<<<<<<<<<<<<<<<");
  }

  protected void saveTrainingData(
      final int best_turn, final int best_move, final Matrix myMatrix) {}

  private int bruteForceTree(Matrix myMatrix, int nextQueueIndex) {
    if (Thread.currentThread().isInterrupted()) return Integer.MIN_VALUE;

    if (nextQueueIndex >= MAX_VISIBLE_NEXTQUEUE) {
      return evalutation(myMatrix);
    }

    int best_score = Integer.MIN_VALUE;

    for (int turn = 0; turn < 4; turn++) {
      Matrix myMatrixCopy = myMatrix.clone(); // make a copy to preserve the original state
      for (int i = 0; i < turn; i++) myMatrixCopy.turnMove(1); // make the turn
      // determine max moves right and left
      int moveR = 0;
      while (!myMatrixCopy.moveSideway(1)) {
        moveR++;
      }
      int moveL = moveR;
      while (!myMatrixCopy.moveSideway(-1)) {
        moveL--;
      }
      // now we are on the left - for each position horizontally make the drop a call recursive
      // function
      for (int m = moveL; m <= moveR; m++) {
        Matrix myMatrixCopy2 = myMatrixCopy.clone(); // make a copy to preserve the original state
        // move right
        for (int i = 0; i < m - moveL; i++) {
          myMatrixCopy2.moveSideway(1);
        }
        myMatrixCopy2.drop();
        myMatrixCopy2.merge();
        myMatrixCopy2.markLinesToBeCleared();
        myMatrixCopy2.clearMarkedLines();
        int score = 0;
        if (myMatrixCopy2.spawn(nextQueue.get(nextQueueIndex).clone())) {
          score += Integer.MIN_VALUE; // game_over
        } else {
          score += bruteForceTree(myMatrixCopy2, nextQueueIndex + 1);
        }
        ;
        if (score > best_score) best_score = score;
      }
    }
    return best_score;
  }

  private static final double weightAbsoluteHeight = -1.0;
  private static final double weightAggregatedHeight = -1.0;
  private static final double weightUnevenness = -1.0;
  private static final double weightHoles = -2.0;
  private static final double weightBlocker = -3.0;

  private int evalutation(Matrix myMatrix) {
    numberOfEvaluations++;
    int score = 0;
    int[] result = scanFieldEvaluations(myMatrix);
    score += weightAbsoluteHeight * result[0];
    score += weightAggregatedHeight * result[1];
    score += weightUnevenness * result[2];
    score += weightHoles * result[3];
    score += weightBlocker * result[4];
    // System.out.println("Eval: "+myMatrix.getLastTetrimino().toString()+" Score: "+score);
    return score;
  }

  /*
   * scans the whole field and calculates various evaluations and returns the as an array
   * { absolute height, aggregated height, unevenness, holes, unwanted blocker }
   */
  private int[] scanFieldEvaluations(Matrix myMatrix) {
    int aggregatedHeight = 0;
    int absoluteHeight = 0;
    int lastHeight = 0;
    int unevenness = 0;
    int holes = 0;
    int blocker = 0;
    int height = 0;

    // scan the field once and do all evaluations which need to scan the whole matrix
    // to avoid scanning the matrix multiple times
    for (int x = 0; x < Matrix.MATRIX_WIDTH; x++) {
      int possibleHoles = 0;
      height = 0;
      int tmpHoles = 0;
      int tmpBlocker = 0;
      for (int y = 0; y < Matrix.MATRIX_HEIGHT; y++) {
        if (myMatrix.getCell(x, y) != TetrisColor.EMPTY) {
          height = y + 1;
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
      aggregatedHeight += height;
      unevenness += Math.abs(lastHeight - height);
      lastHeight = height;
      if (height > absoluteHeight) absoluteHeight = height;
    }
    unevenness += Math.abs(lastHeight - 0);

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
      blocker
    };
  }
}
