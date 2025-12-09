package edu.stockton.project;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

/** Main class for the Evolutionary Computation Project. */
public class Main {
  public static void main(String[] args) throws IOException {
    String inputImage;
    String outputPath;
    double scaleFactor = 3.0;
//    if (args.length > 0) {
//      inputImage = "src/main/java/edu/stockton/project/dog.jpg";
//    } else {
//      inputImage = args[0];
//    }
//    if (args.length > 1) {
//      outputPath = "src/main/java/edu/stockton/project/dog-out.jpg";
//    } else {
//      outputPath = args[1];
//    }

    inputImage = "src/main/java/edu/stockton/project/dogwhite.jpg";
    outputPath = "src/main/java/edu/stockton/project/dog-out";

    BufferedImage rawGrayscale = VoronoiStippler.loadGrayscaleImage(inputImage);

    double[][] points = VoronoiStippler.stipple(rawGrayscale);
    double[][] scaledPoints = scalePoints(points, scaleFactor);

    double[][] tour = TSPArtExample.generateTour(scaledPoints);

    int[] scaledDimensions = new int[] {
            (int) (rawGrayscale.getWidth() * scaleFactor),
            (int) (rawGrayscale.getHeight() * scaleFactor)
    };

    TSPArtExample.drawTour(
        tour, outputPath, scaledDimensions);
  }

  /**
   * Scale coordinates for higher resolution output.
   * @param points Points array of [x, y]
   * @param scale Scale factor
   * @return Scaled points
   */
  private static double[][] scalePoints(double[][] points, double scale) {
    double[] xPoints = points[0];
    double[] yPoints = points[1];

    double[] scaledX = new double[xPoints.length];
    double[] scaledY = new double[yPoints.length];

    for (int i = 0; i < xPoints.length; i++) {
      scaledX[i] = xPoints[i] * scale;
      scaledY[i] = yPoints[i] * scale;
    }

    return new double[][] {scaledX, scaledY};
  }
}
