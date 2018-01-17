package fko.tetris.AI;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import fko.tetris.Tetris;
import fko.tetris.game.Matrix;
import fko.tetris.game.TetrisColor;
import fko.tetris.game.TetrisControlEvents;
import fko.tetris.game.TetrisGame;
import fko.tetris.game.TetrisPhase;
import fko.tetris.tetriminos.Tetrimino;

/**
 * This implementation creates a NeuraNetwork driven Bot.<br>
 * 
 * Using: http://neuroph.sourceforge.net/index.html
 */
public class NeuralNetworkBot extends AbstractBot {
	
	private static final int NEURONS_HIDDEN_LAYER_1 = 50;
	private static final double LEARNING_RATE = 0.1;
	private static final double MOMENTUM = 0.5;
	private static final double MAX_ERROR_RATE = 0.1;

	private static final int MAX_VISIBLE_NEXTQUEUE = 2;

	private final List<Tetrimino> _nextQueue = new ArrayList<Tetrimino>(); 

	/* default value for folder */
	private static final String folderPathPlain = "./var/";
	private static final String fileNamePlain = "NeuralNetworkBot.nnet";
	private static final Path _filePath = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlain);
	private static final String fileNamePlainTrain = "trainingdata_train.csv";
	private static final Path _filePathTrain = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlainTrain);
	private static final String fileNamePlainTest = "trainingdata_test.csv";
	private static final Path _filePathTest = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlainTest);
	

	//  tetris neural network - loads trained network in constructor
//	private final NeuralNetwork<?> _myNeuralNetwork;

	/**
	 * Creates a bot with a handle to the game
	 * @param game
	 */
	public NeuralNetworkBot(TetrisGame game) {
		super(game);
		// load trained network
//		_myNeuralNetwork = NeuralNetwork.createFromFile(_filePath.toFile());
	}

	/**
	 * Run the bot as long as Thread is not interrupted.
	 */
	@Override
	public void run() {
		boolean moveDone = false; // to prevent several calculations during the falling phase
		while (!Thread.interrupted()) {
			final TetrisPhase phaseState = _game.getPhaseState();
			switch(phaseState) {
			// we can only move when we are in FALLING phase
			case LOCK: // we can still move during LOCK - this is helpful in higher levels when game is really fast
			case FALLING: {
				if (!moveDone) {
					long time = System.nanoTime();
					_nextQueue.clear();
					// copy the nextQueue into a list
					for (int i = 0; i <= MAX_VISIBLE_NEXTQUEUE; i++) {
						_nextQueue.add(_game.getNextQueue().get(i));
					}
					// calculate the best position and place Tetrimino
//					placeTetrimino();
					moveDone = true;
					System.out.println(String.format("Bot took %,10d ns", (System.nanoTime() - time)));
					System.out.println();
				}
				break;
			}
			// we we reset our moveDone flag during LOCK phase but do nothing else for now in LOCK phase
			// game over stops thread
			case GAMEOVER: moveDone=false; Thread.currentThread().interrupt(); break;
			default: moveDone=false; break;
			}
		}
	}
//
//	/*
//	 * Calculate the control commands for playing Tetris
//	 */
//	private void placeTetrimino() {
//
//		int best_turn = 0;
//		int best_move = 0;
//		//int best_score = Integer.MIN_VALUE;
//
//		// make a copy of the playfield as the game playfield could move on in the meantime
//		// also we do want to change the original Matrix
//		Matrix myMatrix = _game.getMatrix().clone();
//
//		// TODO Input for Neural Network
//		double input[] = new double[234];
//		int index = 0;
//		for (int y=0; y<Matrix.MATRIX_HEIGHT; y++) {
//			for(int x=0; x<Matrix.MATRIX_WIDTH;x++) {
//				input[index++] = myMatrix.getCell(x, y) == TetrisColor.EMPTY ? 0.0 : 1.0;
//			}
//		}
//		for (int i=1; i<=7; i++) {
//			input[index++] = i==myMatrix.getCurrentTetrimino().getColor().ordinal() ? 1.0 : 0.0;
//		}
//		for (int i=1; i<=7; i++) {
//			input[index++] = i==_nextQueue.get(0).getColor().ordinal() ? 1.0 : 0.0;
//		}
//
//		System.out.println(Arrays.toString(input));
//
//		// set network input
//		_myNeuralNetwork.setInput(input);
//		// calculate network
//		_myNeuralNetwork.calculate();
//		// get network output
//		double[] networkOutput = _myNeuralNetwork.getOutput();
//
//		// Translate from NN output
//
//		//TODO
//		Arrays.stream(networkOutput).forEach(c -> System.out.println(c+" "));
//		System.out.println();
//
//		double tmpMax=0.0;
//		best_turn=0;
//		for (int i=0; i<4; i++) {
//			if (networkOutput[i]>tmpMax) {
//				best_turn = i;
//				tmpMax=networkOutput[i];
//			}
//		}
//		System.out.println("Turn: "+best_turn);
//
//		tmpMax=0.0;
//		best_move=0;
//		for (int i=4; i<15; i++) {
//			if (networkOutput[i]>tmpMax) {
//				best_move = i;
//				tmpMax=networkOutput[i];
//			}
//		}
//		best_move -= 9;
//		System.out.println("Move: "+best_move);
//
//		// now turn to the best position on the real matrix
//		for (int i=0; i<best_turn; i++) {
//			_game.controlQueueAdd(TetrisControlEvents.RTURN);
//		}
//
//		// now move to the best position on the real matrix
//		for (int i=0; i < Math.abs(best_move); i++) {
//			if (best_move < 0) {
//				_game.controlQueueAdd(TetrisControlEvents.LEFT);
//			} else if (best_move > 0) {
//				_game.controlQueueAdd(TetrisControlEvents.RIGHT);
//			}
//		}
//
//		// finally drop on the Tetrimino on the real matrix
//		_game.controlQueueAdd(TetrisControlEvents.HARDDOWN);
//
//		//		System.out.println("TETRIMINO: "+myMatrix.getCurrentTetrimino());
//		//		System.out.println("BEST TURN: "+best_turn+" BEST MOVE: "+best_move);
//		//		System.out.println("BEST SCORE: "+best_score);
//		//		System.out.println(String.format("Spawn Nr: %,d Evaluations: %,d", _numberOfSpawns,_numberOfEvaluations));
//		System.out.println(">>>>>>>>>>>>>>>>>>>> BOT MAKES MOVE <<<<<<<<<<<<<<<<<<<<<<<<<<");
//
//	}
//
//	/* *******************************************************************
//	 * TRAIN AND TEST
//	 *********************************************************************/
//
//	/**
//	 * Run this to train the NeuralNetworkBot and save the Network to file.
//	 * @param args
//	 */
//	public static void main(String[] args) {
//
//		// create multi layer perceptron
//		NeuralNetwork<BackPropagation> myMlPerceptron =
//				new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 234, NEURONS_HIDDEN_LAYER_1, 15);
//
//		System.out.println("Input Layer: "+myMlPerceptron.getLayerAt(0).getNeuronsCount());
//		System.out.println("Hidden Layer: "+myMlPerceptron.getLayerAt(1).getNeuronsCount());
//		System.out.println("Output Layer: "+myMlPerceptron.getLayerAt(2).getNeuronsCount());
//
//		System.out.println("Training neural network");
//		train(myMlPerceptron);
//
//		// test perceptron
//		System.out.println("Testing trained neural network");
//		testNeuralNetwork(myMlPerceptron);
//
//		// save trained neural network
//		System.out.println("Saving trained neural network");
//		myMlPerceptron.save(_filePath.toFile().toString());
//
//		// load saved neural network
//		//		System.out.println("Loading trained neural network");
//		//		NeuralNetwork loadedMlPerceptron = NeuralNetwork.createFromFile(_filePath.toFile());
//
//		// test loaded neural network
//		//		System.out.println("Testing loaded neural network");
//		//		testNeuralNetwork(loadedMlPerceptron, trainingSet);
//	}
//
//	/**
//	 * @param myMlPerceptron
//	 */
//	private static void train(NeuralNetwork<BackPropagation> myMlPerceptron) {
//
//		DataSet trainingSet = readData(_filePathTrain);
//
//		trainingSet.shuffle();
//
//		// learn the training set
//		MomentumBackpropagation learningRule = new MomentumBackpropagation();
//		learningRule.setLearningRate(LEARNING_RATE);
//		learningRule.setMomentum(MOMENTUM);
//		learningRule.setMaxIterations(1);
//		int iteration = 0;
//		do {
//			System.out.print("Iteration: "+iteration);
//			myMlPerceptron.learn(trainingSet, learningRule);
//			System.out.println(" ERROR: "+learningRule.getTotalNetworkError());
//			iteration++;
//		} while (learningRule.getTotalNetworkError() > MAX_ERROR_RATE);
//	}
//
//	private static void testNeuralNetwork(NeuralNetwork<?> nnet) {
//
//		DataSet testSet = readData(_filePathTest);
//
//		for(DataSetRow dataRow : testSet.getRows()) {
//			nnet.setInput(dataRow.getInput());
//			nnet.calculate();
//			double[ ] networkOutput = nnet.getOutput();
//			//System.out.print("Input: " + Arrays.toString(dataRow.getInput()) );
//			System.out.println(" Output Desired: " + Arrays.toString(dataRow.getDesiredOutput()) );
//			System.out.println(" Output Network: " + Arrays.toString(networkOutput) );
//			System.out.println();
//		}
//	}
//
//	/**
//	 * @param myPath
//	 * @return
//	 */
//	private static DataSet readData(Path myPath) {
//		DataSet trainingSet = new DataSet(234, 15);
//		// read file line by line via stream, split up at ; and add input and output to DataSet
//		try (Stream<String> linesStream = Files.lines(myPath)) {
//			linesStream.forEach(l -> {
//				if (l.endsWith(";")) {
//					l=l.substring(0, l.length()-1);
//				}
//				String[] subs = l.split(";");
//				double[] datarow = new double[subs.length];
//				for (int i=0; i<subs.length; i++) {
//					datarow[i] = Double.valueOf(subs[i]);
//				}
//				trainingSet.addRow(Arrays.copyOfRange(datarow, 0, 234), Arrays.copyOfRange(datarow, 234, 249));
//			});
//		} catch (IOException e) {
//			Tetris.fatalError("Dataset file '" + myPath + "' could not be loaded!");
//		}
//		return trainingSet;
//	}

}
