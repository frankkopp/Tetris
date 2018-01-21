package fko.tetris.AI;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class NeuralNetworkBotTrainer {

  private static final Logger LOG = LoggerFactory.getLogger(NeuralNetworkBotTrainer.class);

  private static final String folderPathPlain = "./var/";
  private static final String fileNamePlainTrain = "trainingdata_train.csv";
  private static final String fileNamePlainTest  = "trainingdata_test.csv";

  /**
   * Run this to train the NeuralNetworkBot and save the Network to file.
   *
   * @param args
   */
  public static void main(String[] args) throws IOException, InterruptedException {

    // Initialize the user interface backend
    UIServer uiServer = UIServer.getInstance();

    // Configure where the network information (gradients, score vs. time etc) is to be stored.
    // Here: store in memory.
    StatsStorage statsStorage =
        new InMemoryStatsStorage(); // Alternative: new FileStatsStorage(File), for saving and
    // loading later

    // Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to
    // be visualized
    uiServer.attach(statsStorage);

    // Configuration
    int height      = 26;   // 22 matrix, +2 current tetrimino, +2 next
    int width       = 10;
    int channels    = 1;
    int outputNum   = 44;
    int batchSize   = 100;
    int nEpochs     = 2;
    int iterations  = 2;

    int seed = 1234;

    // get data and train model
    LOG.info("Data load and vectorization...");

    // First: get the dataset using the record reader. CSVRecordReader handles loading/parsing
    int numLinesToSkip = 0;
    char delimiter = ';';

    LOG.info("Reading train file {}{}", folderPathPlain, fileNamePlainTrain);
    RecordReader trainDataCSVReader = new CSVRecordReader(numLinesToSkip, delimiter);
    trainDataCSVReader.initialize(new FileSplit(new File(folderPathPlain + fileNamePlainTrain)));
    DataSetIterator trainIter = new RecordReaderDataSetIterator(trainDataCSVReader, batchSize, height*width, 44);

    LOG.info("Reading test file {}{}", folderPathPlain, fileNamePlainTest);
    RecordReader testDataCSVReader = new CSVRecordReader(numLinesToSkip, delimiter);
    testDataCSVReader.initialize(new FileSplit(new File(folderPathPlain + fileNamePlainTest)));
    DataSetIterator testIter = new RecordReaderDataSetIterator(testDataCSVReader, batchSize, height*width, 44);

    LOG.info("Build model....");
    Map<Integer, Double> lrSchedule = new HashMap<>();
    lrSchedule.put(0,    0.1); // iteration #, learning rate
    lrSchedule.put(200,  0.07);
    lrSchedule.put(600,  0.03);
    lrSchedule.put(800,  0.01);
    lrSchedule.put(1000, 0.005);

    MultiLayerConfiguration conf =
            new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .iterations(iterations)
                    .miniBatch(true)
                    .regularization(true)
                    .l2(0.0005)
                    .learningRate(.01)
                    .learningRateDecayPolicy(LearningRatePolicy.Schedule)
                    .learningRateSchedule(lrSchedule) // overrides the rate set in learningRate
                    .weightInit(WeightInit.XAVIER)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(Updater.NESTEROVS)
                    .list()

                    .layer(
                            0,
                            new ConvolutionLayer.Builder(4, 4)
                                    .nIn(channels)
                                    .stride(1, 1)
                                    .nOut(20)
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
//                                    .nIn(100)
                                    .stride(1, 1) // nIn need not specified in later layers
                                    .nOut(50)
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
//                                    .nIn(200)
                                    .activation(Activation.RELU)
                                    .nOut(500)
                                    .build())
                    .layer(
                            5,
                            new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//                                    .nIn(400)
                                    .nOut(outputNum)
                                    .activation(Activation.SOFTMAX)
                                    .build())

                    .setInputType(InputType.convolutionalFlat(height, width, channels))

                    .backprop(true)
                    .pretrain(false)
                    .build();

    LOG.debug("Model configured");

    MultiLayerNetwork net = new MultiLayerNetwork(conf);
    net.init();
    net.setListeners(new ScoreIterationListener(100)); // console
    net.addListeners(new StatsListener(statsStorage)); // web ui

    LOG.debug("Total num of params: {}", net.numParams());

    // debugging put - print the number of examples per set
    if (LOG.isDebugEnabled()) {
      final AtomicInteger d = new AtomicInteger();
      while (trainIter.hasNext()) {
        trainIter.next();
        d.getAndAdd(trainIter.batch());
      }
      LOG.debug("Number of Train examples {}",d);

      d.set(0);
      while (testIter.hasNext()) {
        testIter.next();
        d.getAndAdd(testIter.batch());
      }
      LOG.debug("Number of Test examples {}",d);

      trainIter.reset();
      testIter.reset();
    }

    // evaluation while training (the score should go down)
    for (int i = 0; i < nEpochs; i++) {
      LOG.info("Starting epoch {}", i+1);
      LOG.info("Starting training");
      net.fit(trainIter);
      LOG.info("Starting evaluating");
      Evaluation eval = net.evaluate(testIter);
      LOG.info(eval.stats());
      trainIter.reset();
      testIter.reset();
      LOG.info("Completed epoch {}", i+1);
    }

    String saveFile = folderPathPlain + "/tetris_nn_model.zip";
    LOG.info("Finished training. Writing model to file {}", saveFile);
    ModelSerializer.writeModel(net, new File(saveFile), true);

    System.exit(0);
  }
}
