package fko.tetris.AI;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TrainingDataTest {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingDataTest.class);

  @Test
  void zoomLines() {

    String line =
              "0.0;0.0;0.0;0.0;1.0;1.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;1.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;1.0;1.0;1.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;0.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;0.0;1.0;1.0;0.0;0.0;0.0;0.0;0.0;"
            + "0.0;0.0;1.0;1.0;1.0;1.0;1.0;0.0;0.0;0.0;"
            + "0.0;1.0;1.0;1.0;1.0;1.0;1.0;0.0;1.0;0.0;"
            + "31";

    String newLine = TrainingData.zoomLines(line, 5);

    System.out.println(line);
    printMatrix(line);
    System.out.println();
    System.out.println(newLine);
    printMatrix(newLine);
  }

  private void printMatrix(final String line) {
    String[] elements = line.split(";");
    int nLines = 0;
    for (int i = 0; i < elements.length; i++) {
      if (i % 10 == 0) {
        System.out.println();
        System.out.printf("%2d. ", nLines++);
      }
      if (!elements[i].isEmpty()) {
        System.out.print(Float.valueOf(elements[i]).intValue() + " ");
      }
    }
    System.out.println();
  }
}
