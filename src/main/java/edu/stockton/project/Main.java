package edu.stockton.project;

import java.awt.image.BufferedImage;
import java.io.IOException;

/** Main class for the Evolutionary Computation Project. */
public class Main {
  public static void main(String[] args) throws IOException {
    String inputImage;
    String outputPath;
    if (args.length > 0) {
      inputImage = "src/main/java/edu/stockton/project/dog.jpg";
    } else {
      inputImage = args[0];
    }
    if (args.length > 1) {
      outputPath = "src/main/java/edu/stockton/project/dog-out.jpg";
    } else {
      outputPath = args[1];
    }

    inputImage = "src/main/java/edu/stockton/project/dog.jpg";

    outputPath = "src/main/java/edu/stockton/project/dog-out.jpg";
    BufferedImage rawGrayscale = VoronoiStippler.loadGrayscaleImage(inputImage);
    double[][] points = VoronoiStippler.stipple(rawGrayscale);
    double[][] tour = TSPArtExample.generateTour(points);
    TSPArtExample.drawTour(
        tour, outputPath, new int[] {rawGrayscale.getWidth(), rawGrayscale.getHeight()});
  }
}
