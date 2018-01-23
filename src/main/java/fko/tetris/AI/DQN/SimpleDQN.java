package fko.tetris.AI.DQN;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.learning.NeuralNetFetchable;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.mdp.toy.SimpleToy;
import org.deeplearning4j.rl4j.mdp.toy.SimpleToyState;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.json.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** SimpleDQN */
public class SimpleDQN implements MDP<SimpleDQNState, Integer, DiscreteSpace> {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleDQN.class);

  private final int maxStep;

  private SimpleDQNState simpleDQNState;

  private NeuralNetFetchable<IDQN> fetchable;

  private ObservationSpace<SimpleDQNState> observationSpace = new ArrayObservationSpace(new int[]{1});

  private DiscreteSpace actionSpace = new DiscreteSpace(2);


  public SimpleDQN(final int maxStep) {
      this.maxStep = maxStep;
  }

  @Override
  public ObservationSpace<SimpleDQNState> getObservationSpace() {
    return this.observationSpace;
  }

  @Override
  public DiscreteSpace getActionSpace() {
    return this.actionSpace;
  }

  @Override
  public SimpleDQNState reset() {
    if (this.fetchable != null) {
      this.printTest(this.maxStep);
    }

    return this.simpleDQNState = new SimpleDQNState(0, 0);
  }

  @Override
  public void close() {

  }

  @Override
  public StepReply<SimpleDQNState> step(final Integer action) {
    double reward = this.simpleDQNState.getStep() % 2 == 0 ? (double)(1 - action) : (double) action;
    this.simpleDQNState = new SimpleDQNState(this.simpleDQNState.getI() + 1, this.simpleDQNState.getStep() + 1);
    return new StepReply(this.simpleDQNState, reward, this.isDone(), new JSONObject("{}"));
  }

  @Override
  public boolean isDone() {
    return this.simpleDQNState.getStep() == this.maxStep;
  }

  @Override
  public MDP<SimpleDQNState, Integer, DiscreteSpace> newInstance() {
    SimpleDQN simpleDQN = new SimpleDQN(this.maxStep);
    simpleDQN.setFetchable(this.fetchable);
    return simpleDQN;
  }

  public void printTest(int maxStep) {
    INDArray input = Nd4j.create(maxStep, 1);

    for(int i = 0; i < maxStep; ++i) {
      input.putRow(i, Nd4j.create((new SimpleToyState(i, i)).toArray()));
    }

    INDArray output = ((IDQN)this.fetchable.getNeuralNet()).output(input);
    LOG.info(output.toString());
  }

  public void setFetchable(NeuralNetFetchable<IDQN> fetchable) {
    this.fetchable = fetchable;
  }
}
