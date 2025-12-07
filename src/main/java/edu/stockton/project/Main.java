package edu.stockton.project;

import java.io.IOException;

/** Main class for the Evolutionary Computation Project. */
public class Main {
  public static void main(String[] args) throws IOException {
    String inputImage;
    String outputPath;
    if (args.length > 0) {
      inputImage = args[0];
    } else {
      inputImage = "src/main/java/edu/stockton/project/dog.jpg";
    }
    if (args.length > 1) {
      outputPath = args[1];
    } else {
      outputPath = "src/main/java/edu/stockton/project/dog-out.jpg";
    }

    VoronoiStippler stippler = new VoronoiStippler(VoronoiStippler.loadImage(inputImage));
    double[][] points = stippler.stipple();
    double[][] tour = TSPArtExample.generateTour(points);
    TSPArtExample.drawTour(tour, outputPath, stippler);
  }
}
