package edu.stockton.project.gui;

import edu.stockton.project.VoronoiStippler;
import java.awt.image.BufferedImage;
import javafx.concurrent.Task;

/**
 * Background task for processing images with stippling and TSP Reports progress to the UI during
 * execution
 */
public class ProcessingTask extends Task<ProcessingResult> {

  private final BufferedImage inputImage;
  private final ParameterConfig config;

  public ProcessingTask(BufferedImage inputImage, ParameterConfig config) {
    this.inputImage = inputImage;
    this.config = config;
  }

  @Override
  protected ProcessingResult call() throws Exception {
    ProcessingResult result = new ProcessingResult();

    try {
      // Phase 1: Stippling (0% - 50%)
      updateMessage("Initializing stippling...");
      updateProgress(0, 100);

      VoronoiStippler stippler = new VoronoiStippler(inputImage, config.getStippleCount());

      updateMessage("Initializing stipple points...");
      updateProgress(5, 100);
      stippler.initializeGenerators(config.getStippleCount());

      updateMessage("Running Lloyd's algorithm...");
      updateProgress(10, 100);

      // Run Lloyd's with progress updates
      int numIterations = 50;
      for (int i = 0; i < numIterations; i++) {
        if (isCancelled()) {
          return null;
        }

        stippler.iterateLloydSingleIteration();

        // Progress from 10% to 45%
        double progress = 10 + (35.0 * i / numIterations);
        updateProgress(progress, 100);
        updateMessage(String.format("Lloyd's algorithm: iteration %d/%d", i + 1, numIterations));
      }

      updateMessage("Rendering stippled image...");
      updateProgress(45, 100);
      result.stippledImage = stippler.renderStipples(1f);
      result.stipplePoints = stippler.getStipplePointsArray();

      updateProgress(50, 100);

      // Scale points if scale factor > 1.0
      double scaleFactor = config.getScaleFactor();
      double[][] scaledPoints = result.stipplePoints;

      if (scaleFactor > 1.0) {
        updateMessage("Scaling coordinates...");
        scaledPoints = scalePoints(result.stipplePoints, scaleFactor);
      }

      // Phase 2: TSP Solving (50% - 100%)
      updateMessage("Solving TSP...");
      updateProgress(55, 100);

      result.tspTour =
          TSPRunner.generateTour(
              scaledPoints,
              config,
              (current, total, message) -> {
                if (isCancelled()) {
                  return;
                }
                // Progress from 55% to 95%
                double progress = 55 + (40.0 * current / total);
                updateProgress(progress, 100);
                updateMessage("TSP: " + message);
              });

      updateMessage("Drawing TSP art...");
      updateProgress(95, 100);

      // Scale dimensions for output
      int outputWidth = (int) (inputImage.getWidth() * scaleFactor);
      int outputHeight = (int) (inputImage.getHeight() * scaleFactor);
      int[] dimensions = new int[] {outputWidth, outputHeight};

      result.tspArtImage = TSPRunner.drawTourToImage(result.tspTour, dimensions);

      updateMessage("Complete!");
      updateProgress(100, 100);

      return result;

    } catch (Exception e) {
      updateMessage("Error: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Scale coordinates for higher resolution output
   *
   * @param points Points array [x[], y[]]
   * @param scale Scale factor
   * @return Scaled points
   */
  private double[][] scalePoints(double[][] points, double scale) {
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

/** Result container for processing output */
class ProcessingResult {
  public BufferedImage stippledImage;
  public BufferedImage tspArtImage;
  public double[][] stipplePoints;
  public double[][] tspTour;
}
