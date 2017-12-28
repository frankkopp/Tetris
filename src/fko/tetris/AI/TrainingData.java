/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fko.tetris.AI;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import fko.tetris.Tetris;
import fko.tetris.game.Matrix;
import fko.tetris.game.TetrisColor;
import fko.tetris.tetriminos.Tetrimino;

/**
 * Reads and stores the highscore from and to file
 */
public class TrainingData {

	/* default value for folder */
	private static final String folderPathPlain = "./var/";
	private static final Path _folderPath = FileSystems.getDefault().getPath(folderPathPlain);
	private static final String fileNamePlain = "trainingdata.csv";
	private static final Path _filePath = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlain);

	// Buffer for entry to save asynchronously
	private LinkedBlockingQueue<DatasetRow> _bufferQueue = new LinkedBlockingQueue<DatasetRow>();

	// Executor to run separate thread to save to file asynchronously
	ExecutorService _executor = Executors.newSingleThreadExecutor();

	/*
	 * Private constructor because of Singleton pattern.
	 * Reads the file and adds the entries to _list
	 */
	public TrainingData() {

		// Check if folder exists and if not try to create it.
		if (!Files.exists(_folderPath, LinkOption.NOFOLLOW_LINKS)) {
			Tetris.minorError(String.format(
					"While reading training data file: Path %s could not be found. Trying to create it."
					,_folderPath.toString()
					));
			try {
				Files.createDirectories(_folderPath);
			} catch (IOException e) {
				Tetris.fatalError(String.format(
						"While reading training data file: Path %s could not be found. Trying to create it."
						,_folderPath.toString()
						));
			}
		}

		// Check if file exists and if not create a empty file
		if (Files.notExists(_filePath, LinkOption.NOFOLLOW_LINKS)) {
			Tetris.minorError(String.format(
					"While reading training data file: File %s could not be found. Trying to create it."
					,_filePath.getFileName().toString()
					));
			try {
				Files.createFile(_filePath);
			} catch (IOException e) {
				Tetris.fatalError(String.format(
						"While reading training data file: File %s could not be found. Trying to create it."
						,_filePath.getFileName().toString()
						));
			}
		}

		// start the service which runs the loop to white for lines to append to file
		_executor.execute(new Appendtask());
	}
	
	/**
	 * Reads all line into an List
	 */
	public static List<double[]> readFromFile() {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(_filePath);
		} catch (IOException e) {
			Tetris.fatalError("Dataset file '" + _filePath + "' could not be loaded!");
		}
		List<double[]> dataset = new ArrayList<>(lines.size());
		lines.stream().forEach(l -> { 
			if (l.endsWith(";")) {
				l=l.substring(0, l.length()-1);
			}
			String[] subs = l.split(";");
			double[] datarow = new double[subs.length];
			for (int i=0; i<subs.length; i++) {
				datarow[i] = Double.valueOf(subs[i]);
			}
			dataset.add(datarow);
		});
		return dataset;
	}

	/**
	 * Adds an new row to the training data and saves this to file.<br>
	 * Saving is asynchronously to be able to return to caller as soon as possible to not slow down caller<br>
	 */
	public void appendEntry(Matrix matrix, Tetrimino currentTetrimino, Tetrimino nextTetrimino, int turn, int move) { 
		DatasetRow dr = new DatasetRow(matrix, currentTetrimino, nextTetrimino, turn, move);
		_bufferQueue.add(dr);
	}

	/**
	 * Opens the file for appending the given line a closes the file
	 * @param line
	 */
	private void appendFile(String line) {
		try(FileWriter fw = new FileWriter(_filePath.toFile(), true)) {
			fw.write(line+"\n"); //appends the string to the file
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the Task the for writing to file
	 */
	private final class Appendtask implements Runnable  {
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
				if (dr != null) 	{ 
					// saves line to file
					appendFile(dr.toString());
				}
			}
		}
	}

	/**
	 * A entry in the Highscore list. 
	 */
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
		 * @param score
		 */
		public DatasetRow(Matrix matrix, Tetrimino currentTetrimino, Tetrimino nextTetrimino, int turn, int move) {
			this.matrix = matrix;
			this.currentTetrimino = currentTetrimino;
			this.nextTetrimino = nextTetrimino;
			this.turn = turn;
			this.move = move;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(500);
			
			// flatten matrix and normalize everything to 0 and 1
			for (int y=0; y<Matrix.MATRIX_HEIGHT; y++) {
				for(int x=0; x<Matrix.MATRIX_WIDTH;x++) {
					sb.append(matrix.getCell(x, y) == TetrisColor.EMPTY ? "0" : "1").append(";");
				}
			}
			sb.append(" ");
			for (int i=1; i<=7; i++) {
				sb.append(i==currentTetrimino.getColor().ordinal() ? "1" : "0").append(";");
			}
			sb.append(" ");
			for (int i=1; i<=7; i++) {
				sb.append(i==nextTetrimino.getColor().ordinal() ? "1" : "0").append(";");
			}
			sb.append(" ");
			for (int i=0; i<4; i++) {
				sb.append(i==turn ? "1" : "0").append(";");
			}
			sb.append(" ");
			for (int i=-5; i<=5; i++) { // hard coded playfield width 10 = 5+5
				sb.append(i==move ? "1" : "0");
				if (i != 5) sb.append(";");
			}
			return sb.toString();
		}
	}
}
