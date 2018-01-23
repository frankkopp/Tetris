package fko.tetris.AI.DQN;

import org.deeplearning4j.rl4j.mdp.toy.SimpleToyState;
import org.deeplearning4j.rl4j.space.Encodable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleDQNState
 */
public class SimpleDQNState implements Encodable {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleDQNState.class);

  int i;
  int step;

  public SimpleDQNState(int i, int step) {
    this.i = i;
    this.step = step;
  }

  @Override
  public double[] toArray() {
    double[] ar = new double[1];
    ar[0] = (20 - i);
    return ar;
  }

  public int getI() {
    return this.i;
  }

  public int getStep() {
    return this.step;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof SimpleDQNState)) {
      return false;
    } else {
      SimpleDQNState other = (SimpleDQNState)o;
      if (this.getI() != other.getI()) {
        return false;
      } else {
        return this.getStep() == other.getStep();
      }
    }
  }

  public int hashCode() {
    int PRIME = 13;
    int result = 1;
    result = result * 59 + this.getStep();
    return result;
  }

  public String toString() {
    return "SimpleDQNState(i=" + this.getI() + ", step=" + this.getStep() + ")";
  }

}
