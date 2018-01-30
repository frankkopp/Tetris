package fko.tetris.AI;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NeuralNetworkBotTrainer {

  private static final Logger LOG = LoggerFactory.getLogger(NeuralNetworkBotTrainer.class);

  // paths to training data
  private static final String folderPathPlain = "./var/";
  private static final String fileNamePlainTrain = "trainingdata_train_zoomed.csv";
  private static final String fileNamePlainTest = "trainingdata_test_zoomed.csv";

  // where to store the trained network
  public static final String NN_SAVE_FILE = folderPathPlain + "tetris_nn_model_30012018.zip";

  // with webserver UI
  public static final boolean WITH_UI = false;

  private MultiLayerNetwork multiLayerNetwork = null;
  private Evaluation evaluation = null;

  /**
   * Run this to train the NeuralNetworkBot and save the Network to file.
   *
   * @param args
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    new NeuralNetworkBotTrainer();
  }

  public NeuralNetworkBotTrainer() throws IOException, InterruptedException {

    TrainingUI ui = new TrainingUI(multiLayerNetwork);

    // Configuration
    int height =
        9; // +2 current tetrimino, +2 next plus 5 rows either with at least one non-zero or the
    // lowest 5 rows. "5" is a parameter but data needs to be prepared with it (was 22
    // matrix, +2 current tetrimino, +2 next)
    int width = 10; // tetris is 10 blocks wide
    int channels = 1; // we only need 1 color (black & white) - color has no real meaning in tetris
    int outputNum = 44; // 4 turns and 11 moves (-5, 0, +5)
    int batchSize = 64;
    int iterations = 2;
    int nEpochs = 50;

    int seed = 1234;

    // get data and train model
    LOG.info("Data load and vectorization...");

    // First: get the dataset using the record reader. CSVRecordReader handles loading/parsing
    int numLinesToSkip = 0;
    char delimiter = ';';

    LOG.info("Reading train file {}{}", folderPathPlain, fileNamePlainTrain);
    RecordReader trainDataCSVReader = new CSVRecordReader(numLinesToSkip, delimiter);
    trainDataCSVReader.initialize(new FileSplit(new File(folderPathPlain + fileNamePlainTrain)));
    DataSetIterator trainIter =
        new RecordReaderDataSetIterator(trainDataCSVReader, batchSize, height * width, 44);

    LOG.info("Reading test file {}{}", folderPathPlain, fileNamePlainTest);
    RecordReader testDataCSVReader = new CSVRecordReader(numLinesToSkip, delimiter);
    testDataCSVReader.initialize(new FileSplit(new File(folderPathPlain + fileNamePlainTest)));
    DataSetIterator testIter =
        new RecordReaderDataSetIterator(testDataCSVReader, batchSize, height * width, 44);

    LOG.info("Build model....");

    // Configuration
    MultiLayerConfiguration conf =
        getConvolutionalNetwork(height, width, channels, outputNum, seed, iterations);
    // getAlexnetModel(height, width, channels, outputNum, seed, iterations);
    // getDenseNNetwork(height, width, channels, outputNum, seed, iterations);

    LOG.debug("Model configured");

    multiLayerNetwork = new MultiLayerNetwork(conf);
    multiLayerNetwork.init();
    multiLayerNetwork.setListeners(new ScoreIterationListener(100)); // console
    if (WITH_UI) {
      UIServer uiServer = UIServer.getInstance();
      StatsStorage statsStorage = new InMemoryStatsStorage();
      uiServer.attach(statsStorage);
      multiLayerNetwork.addListeners(new StatsListener(statsStorage));
    }

    LOG.debug("Total num of params: {}", multiLayerNetwork.numParams());

    // debugging put - print the number of examples per set
    int trainingExamples = 0;
    while (trainIter.hasNext()) {
      trainIter.next();
      trainingExamples += trainIter.batch();
    }
    LOG.debug("Number of Train examples {}", trainingExamples);

    int testExamples = 0;
    while (testIter.hasNext()) {
      testIter.next();
      testExamples += testIter.batch();
    }
    LOG.debug("Number of Test examples {}", testExamples);

    trainIter.reset();
    testIter.reset();

    int iterationsPerEpoche = (trainingExamples / batchSize) * iterations;
    int totalIterations = iterationsPerEpoche * nEpochs;
    LOG.info("Training {} iterations", totalIterations);

    // evaluation while training (the score should go down)
    for (int i = 0; i < nEpochs; i++) {
      Instant startTime = Instant.now();

      LOG.info("Starting training epoch {} of {} with {} iterations", i + 1, nEpochs, iterationsPerEpoche);

      // MultipleEpochsIterator mTrainIter = new MultipleEpochsIterator(1, trainIter);
      // mTrainIter.trackEpochs();
      multiLayerNetwork.fit(trainIter);

      LOG.info("Starting evaluating");
      evaluation = multiLayerNetwork.evaluate(testIter);
      LOG.info(evaluation.stats());

      trainIter.reset();
      testIter.reset();
      LOG.info(
          "Completed epoch {} of {} ({} iterations of {} total iterations",
          i + 1,
          nEpochs,
          iterationsPerEpoche * (i + 1),
          totalIterations);

      LOG.info("Writing model to file {}", NN_SAVE_FILE);
      ModelSerializer.writeModel(multiLayerNetwork, new File(NN_SAVE_FILE), true);

      Instant endTime = Instant.now();

      LOG.info("Epoch {} took {}", i + 1, Duration.between(startTime, endTime));
      LOG.info(
          "Estimated remaining time: {}",
          Duration.between(startTime, endTime).multipliedBy(nEpochs - 1 - i));
    }

    System.exit(0);
  }

  /**
   * Test the network: 1. does the initial loss make sense (log(1/y) 2. does it overfit for a small
   * sample of the data 3. does it converge at all
   *
   * @param height
   * @param width
   * @param channels
   * @param outputNum
   * @param seed
   * @param iterations
   * @return configured network
   */
  private MultiLayerConfiguration getConvolutionalNetwork(
      final int height,
      final int width,
      final int channels,
      final int outputNum,
      final int seed,
      final int iterations) {

    final WeightInit weightInit = WeightInit.XAVIER;
    final OptimizationAlgorithm optimizationAlgo =
        OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
    final Updater updater = Updater.ADADELTA; // Updater.ADAM;
    final LossFunctions.LossFunction lossFunction =
        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD;
    final double dropOut = 0.5;

    LOG.info("Build model....");

    Map<Integer, Double> lrSchedule = new HashMap<>();
    lrSchedule.put(0, 0.1); // iteration #, learning rate
    lrSchedule.put(200, 0.05);
    lrSchedule.put(600, 0.01);
    lrSchedule.put(1000, 0.005);
    lrSchedule.put(1500, 0.001);
    lrSchedule.put(2500, 0.0005);

    return new NeuralNetConfiguration.Builder()
        .seed(seed)
        .iterations(iterations)
        .miniBatch(true)
        .regularization(true)
        .l2(0.0005)
        .learningRate(0.1)
        .learningRateDecayPolicy(LearningRatePolicy.Schedule)
        .learningRateSchedule(lrSchedule) // overrides the rate set in learningRate
        .weightInit(weightInit)
        .optimizationAlgo(optimizationAlgo)
        .updater(updater)
        .list()
        .layer(
            0,
            new ConvolutionLayer.Builder(2, 2)
                .nIn(channels)
                .stride(1, 1)
                .padding(1, 1)
                .nOut(40)
                .activation(Activation.IDENTITY)
                .build())
        .layer(
            1,
            new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(1, 1)
                .build())
        .layer(
            2,
            new ConvolutionLayer.Builder(4, 4)
                .stride(1, 1) // nIn need not specified in later layers
                .nOut(80)
                .activation(Activation.IDENTITY)
                .build())
        .layer(
            3,
            new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
        .layer(
            4,
            new DenseLayer.Builder()
                .activation(Activation.RELU)
                // .dropOut(dropOut)
                .nOut(320)
                .build())
        .layer(
            5,
            new OutputLayer.Builder(lossFunction)
                .nOut(outputNum)
                .activation(Activation.SOFTMAX)
                .build())
        .setInputType(InputType.convolutionalFlat(height, width, channels))
        .backprop(true)
        .pretrain(false)
        .build();
  }

  private MultiLayerConfiguration getDenseNNetwork(
      final int height,
      final int width,
      final int channels,
      final int outputNum,
      final int seed,
      final int iterations) {

    final WeightInit weightInit = WeightInit.XAVIER;
    final OptimizationAlgorithm optimizationAlgo =
        OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
    final Nesterovs updater = new Nesterovs(0.98); //
    final LossFunctions.LossFunction lossFunction =
        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD;

    Map<Integer, Double> lrSchedule = new HashMap<>();
    lrSchedule.put(0, 0.1); // iteration #, learning rate
    lrSchedule.put(200, 0.05);
    lrSchedule.put(600, 0.03);
    lrSchedule.put(800, 0.01);
    lrSchedule.put(1000, 0.005);
    lrSchedule.put(1500, 0.001);

    return new NeuralNetConfiguration.Builder()
        .seed(seed)
        .optimizationAlgo(optimizationAlgo)
        .iterations(iterations)
        .activation(Activation.RELU)
        .weightInit(weightInit)
        .learningRate(0.1)
        .learningRateDecayPolicy(LearningRatePolicy.Schedule)
        .learningRateSchedule(lrSchedule)
        .updater(updater)
        .regularization(true)
        .l2(0.0005)
        .optimizationAlgo(optimizationAlgo)
        .list()
        .layer(0, new DenseLayer.Builder().nIn(height * width).nOut(512).build())
        .layer(1, new DenseLayer.Builder().nIn(512).nOut(256).build())
        .layer(2, new DenseLayer.Builder().nIn(256).nOut(256).build())
        .layer(3, new DenseLayer.Builder().nIn(256).nOut(128).build())
        .layer(4, new DenseLayer.Builder().nIn(128).nOut(88).build())
        .layer(
            5,
            new OutputLayer.Builder(lossFunction)
                .activation(Activation.SOFTMAX)
                .nIn(88)
                .nOut(outputNum)
                .build())
        .pretrain(false)
        .backprop(true)
        .build();
  }

  private MultiLayerConfiguration getAlexnetModel(
      final int height,
      final int width,
      final int channels,
      final int outputNum,
      final int seed,
      final int iterations) {
    /**
     * AlexNet model interpretation based on the original paper ImageNet Classification with Deep
     * Convolutional Neural Networks and the imagenetExample code referenced.
     * http://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf
     */
    double nonZeroBias = 1;
    double dropOut = 0.5;

    MultiLayerConfiguration conf =
        new NeuralNetConfiguration.Builder()
            .seed(seed)
            .weightInit(WeightInit.DISTRIBUTION)
            .dist(new NormalDistribution(0.0, 0.01))
            .activation(Activation.RELU)
            .updater(new Nesterovs(0.9))
            .iterations(iterations)
            .gradientNormalization(
                GradientNormalization
                    .RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .learningRate(1e-2)
            .biasLearningRate(1e-2 * 2)
            .learningRateDecayPolicy(LearningRatePolicy.Step)
            .lrPolicyDecayRate(0.1)
            .lrPolicySteps(100000)
            .regularization(true)
            .l2(5 * 1e-4)
            .list()
            .layer(
                0,
                new ConvolutionLayer.Builder(new int[] {6, 6}, new int[] {4, 4}, new int[] {2, 2})
                    .name("cnn1")
                    .nIn(channels)
                    .nOut(96)
                    .biasInit((double) 0)
                    .build())
            .layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
            .layer(
                2,
                new SubsamplingLayer.Builder(new int[] {1, 1}, new int[] {1, 1})
                    .name("maxpool1")
                    .build())
            .layer(
                3,
                new ConvolutionLayer.Builder(new int[] {5, 5}, new int[] {1, 1}, new int[] {2, 2})
                    .name("cnn2")
                    .nOut(256)
                    .biasInit(nonZeroBias)
                    .build())
            .layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
            .layer(
                5,
                new SubsamplingLayer.Builder(new int[] {1, 1}, new int[] {1, 1})
                    .name("maxpool2")
                    .build())
            .layer(
                6,
                new ConvolutionLayer.Builder(new int[] {3, 3}, new int[] {1, 1}, new int[] {1, 1})
                    .name("cnn3")
                    .nOut(384)
                    .biasInit((double) 0)
                    .build())
            .layer(
                7,
                new ConvolutionLayer.Builder(new int[] {3, 3}, new int[] {1, 1}, new int[] {1, 1})
                    .name("cnn4")
                    .nOut(384)
                    .biasInit(nonZeroBias)
                    .build())
            .layer(
                8,
                new ConvolutionLayer.Builder(new int[] {3, 3}, new int[] {1, 1}, new int[] {1, 1})
                    .name("cnn5")
                    .nOut(256)
                    .biasInit(nonZeroBias)
                    .build())
            .layer(
                9,
                new SubsamplingLayer.Builder(new int[] {3, 3}, new int[] {2, 2})
                    .name("maxpool3")
                    .build())
            .layer(
                10,
                new DenseLayer.Builder()
                    .name("ffn1")
                    .nOut(4096)
                    .biasInit(nonZeroBias)
                    .dropOut(dropOut)
                    .dist(new GaussianDistribution(0, 0.005))
                    .build())
            .layer(
                11,
                new DenseLayer.Builder()
                    .name("ffn2")
                    .nOut(4096)
                    .biasInit(nonZeroBias)
                    .dropOut(dropOut)
                    .dist(new GaussianDistribution(0, 0.005))
                    .build())
            .layer(
                12,
                new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                    .name("output")
                    .nOut(outputNum)
                    .activation(Activation.SOFTMAX)
                    .build())
            .backprop(true)
            .pretrain(false)
            .setInputType(InputType.convolutional(height, width, channels))
            .build();

    return conf;
  }

  public class TrainingUI extends Application {

    public static final int updateInterval = 1; // ms

    private Stage primaryStage;
    private Text score;
    private XYChart.Series scoreSeries;
    private XYChart.Series f1Series;

    private long timeCounter = 0;

    public TrainingUI(final MultiLayerNetwork multiLayerNetwork) {

      // Startup the JavaFX platform
      Platform.setImplicitExit(false);
      PlatformImpl.startup(
          () -> {
            primaryStage = new Stage();
            start(primaryStage);
          });

      // wait for the UI to show before returning
      do {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignore) {
        }
      } while (primaryStage == null || !primaryStage.isShowing());

      // ui updater
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(
          () -> {
            Platform.runLater(() -> updateUI());
          },
          0,
          updateInterval,
          TimeUnit.SECONDS);
    }

    private void updateUI() {

      if (multiLayerNetwork == null) {
        score.setText("N/A");
      } else {
        double score = multiLayerNetwork.score();
        this.score.setText("" + score);
        timeCounter += updateInterval;
        if (score>0) {
          scoreSeries.getData().add(new XYChart.Data(timeCounter, score));
        }
        if (evaluation != null) {
          double f1 = evaluation.f1();
          evaluation.stats();
          if (f1>0) {
            f1Series.getData().add(new XYChart.Data(timeCounter, f1));
          }
        }
      }
    }

    @Override
    public void start(final Stage stage) {

      // raw score out put row
      HBox scoreRow = new HBox();
      scoreRow.setAlignment(Pos.CENTER);
      Text label = new Text("Score: ");
      score = new Text("n/a");
      scoreRow.getChildren().addAll(label, score);

      // graph
      //defining the axes
      final NumberAxis xAxis = new NumberAxis();
      final NumberAxis yAxis = new NumberAxis();
      xAxis.setLabel("time");
      yAxis.setLabel("score");
      //creating the chart
      final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
      lineChart.setTitle("Training Score");
      lineChart.setCreateSymbols(false); //hide dots
      //defining a scoreSeries
      scoreSeries = new XYChart.Series();
      scoreSeries.setName("Loss Score");
      f1Series = new XYChart.Series();
      f1Series.setName("F1 Score");
      lineChart.getData().addAll(scoreSeries, f1Series);

      // horizontal box with all rows
      VBox root = new VBox();
      root.setAlignment(Pos.CENTER);
      root.getChildren().addAll(scoreRow);
      root.getChildren().addAll(lineChart);

      Scene scene = new Scene(root, 520, 300);
      stage.setScene(scene);
      stage.setTitle("Training Info");
      stage.setResizable(true);
      stage.show();
    }
  }
}
