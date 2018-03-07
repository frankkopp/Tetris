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
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.joda.time.Interval;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** TrainingUI */
public class TrainingUI extends Application implements TrainingListener {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingUI.class);

  public static final int updateInterval = 1; // ms

  private Stage primaryStage;
  private Text score;
  private XYChart.Series scoreSeries;
  private XYChart.Series f1Series;

  private final MultiLayerNetwork multiLayerNetwork;
  private Evaluation evaluation;
  private final int interval;

  public TrainingUI(final MultiLayerNetwork network) {
    this(network, 1);
  }

  public TrainingUI(final MultiLayerNetwork network, int interval) {

    this.multiLayerNetwork = network;
    this.interval = interval;
    this.evaluation = null;

    LOG.info("Starting TrainingUI with interval {} iterations", interval);

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
  }

  private void updateUI(final int iteration) {

    if (multiLayerNetwork == null) {
      score.setText("N/A");
    } else {
      double score = multiLayerNetwork.score();
      this.score.setText("" + score);
      if (score > 0) {
        scoreSeries.getData().add(new XYChart.Data(iteration, score));
      }
      if (evaluation != null) {
        double f1 = evaluation.f1();
        evaluation.stats();
        if (f1 > 0) {
          f1Series.getData().add(new XYChart.Data(iteration, f1));
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
    // defining the axes
    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Iterations");
    yAxis.setLabel("Score");
    // creating the chart
    final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
    lineChart.setTitle("Training Score");
    lineChart.setCreateSymbols(false); // hide dots
    lineChart.setMinHeight(500);
    // defining a scoreSeries
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

    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.setTitle("Training Info");
    stage.setResizable(true);
    stage.show();
  }

  @Override
  public boolean invoked() {
    return false;
  }

  @Override
  public void invoke() {}

  @Override
  public void iterationDone(final Model model, final int iteration) {
    if (iteration % interval == 0) {
      Platform.runLater(() -> updateUI(iteration));
    }
  }

  @Override
  public void onEpochStart(final Model model) {

  }

  @Override
  public void onEpochEnd(final Model model) {

  }

  @Override
  public void onForwardPass(final Model model, final List<INDArray> activations) {

  }

  @Override
  public void onForwardPass(final Model model, final Map<String, INDArray> activations) {

  }

  @Override
  public void onGradientCalculation(final Model model) {

  }

  @Override
  public void onBackwardPass(final Model model) {

  }

  public Evaluation getEvaluation() {
    return evaluation;
  }

  public void setEvaluation(final Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}
