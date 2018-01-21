/**
 * The MIT License (MIT)
 *
 * <p>"Tetris by Frank Kopp"
 *
 * <p>mail-to:frank@familie-kopp.de
 *
 * <p>Copyright (c) 2016 Frank Kopp
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fko.tetris.AI;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fko.tetris.Tetris;
import fko.tetris.game.Matrix;
import fko.tetris.game.TetrisColor;
import fko.tetris.tetriminos.Tetrimino;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import views.html.helper.options;

/** Reads and stores the highscore from and to file */
public class TrainingData {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingData.class);

  /* default value for folder */
  private static final String folderPathPlain = "./var/";
  private static final Path folderPath = FileSystems.getDefault().getPath(folderPathPlain);
  private static final String fileNamePlain = "trainingdata.csv";
  private static final Path filePath =
      FileSystems.getDefault().getPath(folderPathPlain, fileNamePlain);

  // Buffer for entry to save asynchronously
  private LinkedBlockingQueue<DatasetRow> _bufferQueue = new LinkedBlockingQueue<DatasetRow>();

  // Executor to run separate thread to save to file asynchronously
  ExecutorService executor = Executors.newSingleThreadExecutor();

  /*
   * Reads the file and adds the entries to _list
   */
  public TrainingData() {

    // Check if folder exists and if not try to create it.
    if (!Files.exists(folderPath, LinkOption.NOFOLLOW_LINKS)) {
      Tetris.minorError(
          String.format(
              "While reading training data file: Path %s could not be found. Trying to create it.",
              folderPath.toString()));
      try {
        Files.createDirectories(folderPath);
      } catch (IOException e) {
        Tetris.fatalError(
            String.format(
                "While reading training data file: Path %s could not be found. Trying to create it.",
                folderPath.toString()));
      }
    }

    // Check if file exists and if not create a empty file
    if (Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS)) {
      Tetris.minorError(
          String.format(
              "While reading training data file: File %s could not be found. Trying to create it.",
              filePath.getFileName().toString()));
      try {
        Files.createFile(filePath);
      } catch (IOException e) {
        Tetris.fatalError(
            String.format(
                "While reading training data file: File %s could not be found. Trying to create it.",
                filePath.getFileName().toString()));
      }
    }

    // start the service which runs the loop to white for lines to append to file
    executor.execute(new Appendtask());
  }

  /**
   * Adds an new row to the training data and saves this to file.<br>
   * Saving is asynchronously to be able to return to caller as soon as possible to not slow down
   * caller<br>
   */
  public void appendEntry(
      Matrix matrix, Tetrimino currentTetrimino, Tetrimino nextTetrimino, int turn, int move) {
    DatasetRow dr = new DatasetRow(matrix, currentTetrimino, nextTetrimino, turn, move);
    _bufferQueue.add(dr);
    LOG.debug("Entry added. Queue size={}", _bufferQueue.size());
  }

  /**
   * Opens the file for appending the given line a closes the file
   *
   * @param line
   */
  private void appendFile(String line) {
    try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {
      fw.write(line + "\n"); // appends the string to the file
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** This is the Task the for writing to file */
  private final class Appendtask implements Runnable {
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        DatasetRow dr = null;
        try {
          // takes a line from the queue or waits until a line is available
          dr = _bufferQueue.take();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (dr != null) {
          // saves line to file
          appendFile(dr.toDataRowCSV());
        }
      }
    }
  }

  /** A entry in the list. */
  public static class DatasetRow {

    public final Matrix matrix; // x0y0...x10y0...x0y1...x10y1...
    public final Tetrimino currentTetrimino;
    public final Tetrimino nextTetrimino;
    public final int turn;
    public final int move;

    /**
     * @param matrix
     * @param currentTetrimino
     * @param nextTetrimino
     * @param turn
     * @param move
     */
    public DatasetRow(
        Matrix matrix, Tetrimino currentTetrimino, Tetrimino nextTetrimino, int turn, int move) {
      this.matrix = matrix;
      this.currentTetrimino = currentTetrimino;
      this.nextTetrimino = nextTetrimino;
      this.turn = turn;
      this.move = move;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toDataRowCSV() {
      StringBuilder sb = new StringBuilder(500);

      // Generate a 10x24 matrix - 10 wide, 26 height
      // current tetrimino should be in the 231+24th row, next in 25-26th
      // this is optimal input for a cnn

      int[][] tMatrix;

      // now add current tetrimino in it's start position
      tMatrix = nextTetrimino.getMatrix(Tetrimino.Facing.NORTH);
      // we can assume that in spawn position all tetriminos are max 2 in height
      for (int y = 0; y < 2; y++) {
        for (int x = 0; x < Matrix.MATRIX_WIDTH; x++) {
          if (x < nextTetrimino.getCurrentPosition().x
              || x >= nextTetrimino.getCurrentPosition().x + tMatrix[y].length) {
            sb.append(0f).append(";");
          } else {
            sb.append(tMatrix[y][x - nextTetrimino.getCurrentPosition().x] == 0 ? 0f : 1f)
                .append(";");
          }
        }
      }

      // now add next tetrimino in it's start position
      tMatrix = currentTetrimino.getMatrix(Tetrimino.Facing.NORTH);
      // we can assume that in spawn position all tetriminos are max 2 in height
      for (int y = 0; y < 2; y++) {
        for (int x = 0; x < Matrix.MATRIX_WIDTH; x++) {
          if (x < currentTetrimino.getCurrentPosition().x
              || x >= currentTetrimino.getCurrentPosition().x + tMatrix[y].length) {
            sb.append(0f).append(";");
          } else {
            sb.append(tMatrix[y][x - currentTetrimino.getCurrentPosition().x] == 0 ? 0f : 1f)
                .append(";");
          }
        }
      }

      // flatten matrix and normalize everything to 0 and 1
      for (int y = Matrix.MATRIX_HEIGHT - 1; y >= 0; y--) {
        for (int x = 0; x < Matrix.MATRIX_WIDTH; x++) {
          sb.append(matrix.getCell(x, y) == TetrisColor.EMPTY ? 0f : 1f).append(";");
        }
      }

      sb.append(calculateLabel());
      return sb.toString();
    }

    /** @return label */
    public int calculateLabel() {
      int label = turn * 11 + (move + 5); // normalize from range -5 to 5 to range 0-11
      LOG.debug("turn={} move={} label={}", turn, move, label);
      return label;
    }
  }

  public static void main(String[] args) throws ParseException {

    int percentTest;

    // Create options
    Option help = new Option("help", "print this message");
    Option help2 = new Option("?", "print this message");
    Option split = Option.builder("split").hasArg(true).desc("the file to be split").build();
    Option testPercentage =
        Option.builder("test").hasArg(true).desc("percent of overall data for tests").build();

    // create Options object
    Options options = new Options();
    options.addOption(help);
    options.addOption(help2);
    options.addOption(split);
    options.addOption(testPercentage);

    // create the parser
    CommandLineParser parser = new DefaultParser();
    CommandLine param = parser.parse(options, args);

    if (param.hasOption("test")) {
      percentTest = Integer.parseInt(param.getOptionValue("test"));
      LOG.info("Percentage of test data is set to {}%", percentTest);
    } else {
      percentTest = 20;
      LOG.info("Percentage of test data is not set. Using default of 20%");
    }

    if (param.hasOption('?') || param.hasOption("help")) {
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("TrainingData", options);
    }

    if (param.hasOption("split")) {
      splitDataFile(param.getOptionValue("split"), percentTest);
    }
  }

  private static void splitDataFile(final String file, final int percentTest) {

    try (Stream<String> stream = Files.lines(Paths.get(file))) {
      LOG.info("File \"{}\" opened...", file);

      // read all line into a List
      List<String> list = stream.collect(Collectors.toList());
      LOG.info("Number of Examples: {}", list.size());

      // shuffle the list
      Collections.shuffle(list);

      // split the list
      double percentage = 1.0 - (percentTest / 100f);
      long nTrain = (long) (list.size() * percentage);

      if (LOG.isDebugEnabled()) {
        long train = list.stream().limit(nTrain).count();
        long test = list.stream().skip(nTrain).count();
        LOG.debug(
            "Total Examples: {}, Train Set has {} examples, Test Set has {}",
            (train + test),
            train,
            test);
      }

      // Write train data to file
      String trainDataFileName =
          file.substring(0, file.lastIndexOf('.'))
              + "_train"
              + file.substring(file.lastIndexOf('.', file.length()));
      FileWriter trainFileW = new FileWriter(trainDataFileName, false);
      LOG.info("Writing train data into {}", trainDataFileName);
      for (String str : list.stream().limit(nTrain).collect(Collectors.toList())) {
        trainFileW.write(str);
        trainFileW.write(System.lineSeparator());
      }
      trainFileW.close();

      // Write test data to file
      String testDataFileName =
              file.substring(0, file.lastIndexOf('.'))
                      + "_test"
                      + file.substring(file.lastIndexOf('.', file.length()));
      FileWriter testFileW = new FileWriter(testDataFileName, false);
      LOG.info("Writing test data into {}", testDataFileName);
      for (String str : list.stream().skip(nTrain).collect(Collectors.toList())) {
        testFileW.write(str);
        testFileW.write(System.lineSeparator());
      }
      testFileW.close();

    } catch (IOException e) {
      LOG.error("" + e);
    }
  }
}
