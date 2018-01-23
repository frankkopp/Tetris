package fko.tetris.AI;

import fko.tetris.game.*;
import fko.tetris.tetriminos.Tetrimino;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This implementation creates a NeuraNetwork driven Bot.<br>
 * Using: http://neuroph.sourceforge.net/index.html
 */
public class NeuralNetworkBot extends AbstractBot {

  private static final Logger LOG = LoggerFactory.getLogger(NeuralNetworkBot.class);

  private static final int MAX_VISIBLE_NEXTQUEUE = 2;

  private final List<Tetrimino> _nextQueue = new ArrayList<Tetrimino>();

  /* default value for folder */
  private static final String folderPathPlain = "./var/";
  private static final String fileNamePlain = "tetris_nn_model_F1-05132.zip";
  private static final String fileNamePlainTrain = "trainingdata_train.csv";
  private static final String fileNamePlainTest = "trainingdata_test.csv";

  private MultiLayerNetwork botAI = null;

  //  tetris neural network - loads trained network in constructor
  //	private final NeuralNetwork<?> _myNeuralNetwork;

  /**
   * Creates a bot with a handle to the game
   *
   * @param game
   */
  public NeuralNetworkBot(TetrisGame game) {
    super(game);

    try {
      botAI = ModelSerializer.restoreMultiLayerNetwork(folderPathPlain + fileNamePlain);
    } catch (IOException e) {
      LOG.error("Could not load trained NN", e);
    }
  }

  /** Run the bot as long as Thread is not interrupted. */
  @Override
  public void run() {
    boolean moveDone = false; // to prevent several calculations during the falling phase
    while (!Thread.interrupted()) {
      final TetrisPhase phaseState = game.getPhaseState();
      switch (phaseState) {
          // we can only move when we are in FALLING phase
        case LOCK: // we can still move during LOCK - this is helpful in higher levels when game is
          // really fast
        case FALLING:
          {
            if (!moveDone) {
              long time = System.nanoTime();
              _nextQueue.clear();
              // copy the nextQueue into a list
              for (int i = 0; i <= MAX_VISIBLE_NEXTQUEUE; i++) {
                _nextQueue.add(game.getNextQueue().get(i));
              }
              // calculate the best position and place Tetrimino
              placeTetrimino();
              moveDone = true;
              System.out.println(String.format("Bot took %,10d ns", (System.nanoTime() - time)));
              System.out.println();
            }
            break;
          }
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
  }


  /**
   * Calculate the control commands for playing Tetris
   */
  private void placeTetrimino() {

    int best_turn = 0;
    int best_move = 0;
    // int best_score = Integer.MIN_VALUE;

    // make a copy of the playfield as the game playfield could move on in the meantime
    // also we do want to change the original Matrix
    Matrix myMatrix = game.getMatrix().clone();

    // prepare input vector
    INDArray input = getInputNDArray(myMatrix);
    LOG.debug("NN input {}", input);

    // get result from trained network
    INDArray output = botAI.output(input);

    int argMax = ((Float) output.argMax().element()).intValue();

    LOG.debug("NN output {}", output);
    LOG.debug("NN max output index {}", argMax);

    // Translate from NN output

    best_turn = argMax/11;
    System.out.println("Turn: " + best_turn);

    best_move = (argMax%11) -5;
    System.out.println("Move: " + best_move);

    // now turn to the best position on the real matrix
    for (int i = 0; i < best_turn; i++) {
      game.controlQueueAdd(TetrisControlEvents.RTURN);
    }

    // now move to the best position on the real matrix
    for (int i = 0; i < Math.abs(best_move); i++) {
      if (best_move < 0) {
        game.controlQueueAdd(TetrisControlEvents.LEFT);
      } else if (best_move > 0) {
        game.controlQueueAdd(TetrisControlEvents.RIGHT);
      }
    }

    // finally drop on the Tetrimino on the real matrix
    game.controlQueueAdd(TetrisControlEvents.HARDDOWN);

    System.out.println("TETRIMINO: " + myMatrix.getCurrentTetrimino());
    System.out.println("BEST TURN: " + best_turn + " BEST MOVE: " + best_move);
    // System.out.println("BEST SCORE: " + best_score);
    // System.out.println(String.format("Spawn Nr: %,d Evaluations: %,d", _numberOfSpawns,
    // _numberOfEvaluations));
    System.out.println(">>>>>>>>>>>>>>>>>>>> BOT MAKES MOVE <<<<<<<<<<<<<<<<<<<<<<<<<<");
  }

  private INDArray getInputNDArray(Matrix matrix) {

    // 22x10 + 2x10 current + 2x10 next tetrimino
    int matrixWidth = Matrix.MATRIX_WIDTH;
    float [] flatNormalizedMatrix = new float[(Matrix.MATRIX_HEIGHT+4)* matrixWidth];

    int[][] tMatrix;

    // now add current tetrimino in it's start position
    Tetrimino nextTetrimino = _nextQueue.get(0);
    tMatrix = nextTetrimino.getMatrix(Tetrimino.Facing.NORTH);
    // we can assume that in spawn position all tetriminos are max 2 in height
    for (int y = 0; y < 2; y++) {
      for (int x = 0; x < matrixWidth; x++) {
        if (x < nextTetrimino.getCurrentPosition().x || x >= nextTetrimino.getCurrentPosition().x + tMatrix[y].length) {
          flatNormalizedMatrix[y*matrixWidth+x] = 0f;
        } else {
          flatNormalizedMatrix[y*matrixWidth+x] = tMatrix[y][x - nextTetrimino.getCurrentPosition().x] == 0 ? 0f : 1f;
        }
      }
    }

    // now add next tetrimino in it's start position
    final Tetrimino currentTetrimino = matrix.getCurrentTetrimino();
    tMatrix = currentTetrimino.getMatrix(Tetrimino.Facing.NORTH);
    // we can assume that in spawn position all tetriminos are max 2 in height
    for (int y = 0; y < 2; y++) {
      for (int x = 0; x < matrixWidth; x++) {
        if (x < currentTetrimino.getCurrentPosition().x || x >= currentTetrimino.getCurrentPosition().x + tMatrix[y].length) {
          flatNormalizedMatrix[(y+2)*matrixWidth+x] = 0f;
        } else {
          flatNormalizedMatrix[(y+2)*matrixWidth+x] = tMatrix[y][x - currentTetrimino.getCurrentPosition().x] == 0 ? 0f : 1f;
        }
      }
    }

    // flatten matrix and normalize everything to 0 and 1
    for (int y = 0; y < Matrix.MATRIX_HEIGHT; y++) {
      for (int x = 0; x < matrixWidth; x++) {

        flatNormalizedMatrix[(Matrix.MATRIX_HEIGHT-1-y+4)*matrixWidth+x] =
                matrix.getCell(x, y) == TetrisColor.EMPTY ? 0f : 1f;

      }
    }

    if (LOG.isTraceEnabled()) {
      String line = System.lineSeparator();
      for (int i=0; i<flatNormalizedMatrix.length; i++) {
        line += (int) flatNormalizedMatrix[i] + " ";
        if ((i+1) % 10 == 0) {
          line += System.lineSeparator();
        }
      }
      LOG.trace(line);
    }

    return new NDArray(flatNormalizedMatrix);
  }
}