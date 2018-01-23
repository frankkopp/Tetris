package fko.tetris.AI.DQN;

import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** DQNTest */
public class DQNTest {

  private static final Logger LOG = LoggerFactory.getLogger(DQNTest.class);

  public static void main(String[] args) throws IOException {
    dqnTest();
  }

  public static QLearning.QLConfiguration qlConfiguration =
      new QLearning.QLConfiguration(
          123, // Random seed
          100000, // Max step By epoch
          80000, // Max step
          10000, // Max size of experience replay
          32, // size of batches
          100, // target update (hard)
          0, // num step noop warmup
          0.05, // reward scaling
          0.99, // gamma
          10.0, // td-error clipping
          0.1f, // min epsilon
          2000, // num step for eps greedy anneal
          true // double DQN
          );

  public static DQNFactoryStdDense.Configuration netConfiguration =
      DQNFactoryStdDense.Configuration.builder()
          .l2(0.01)
          .learningRate(1e-2)
          .numLayer(3)
          .numHiddenNodes(16)
          .build();

  public static void dqnTest() throws IOException {

    //record the training data in rl4j-data in a new folder
    DataManager manager = new DataManager();

    //define the mdp from toy (toy length)
    SimpleDQN mdp = new SimpleDQN(20);

    //define the training method
    Learning<SimpleDQNState, Integer, DiscreteSpace, IDQN> dql = new QLearningDiscreteDense<>(mdp, netConfiguration, qlConfiguration, manager);

    //enable some logging for debug purposes on toy mdp
    mdp.setFetchable(dql);

    //start the training
    dql.train();

    //useless on toy but good practice!
    mdp.close();

  }
}
