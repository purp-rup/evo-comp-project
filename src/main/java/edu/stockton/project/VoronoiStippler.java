package edu.stockton.project;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Weighted Voronoi Stippler based on Secord (2002). Generates stipple drawings from grayscale
 * images using Lloyd's algorithm.
 */
public class VoronoiStippler {
  private final BufferedImage image;
  private final int width;
  private final int height;
  private final float[][] density; // density[y][x] = 1 - normalized_brightness
  private List<Point2D> generators;
  private final int numStipples;
  private final Random random = new Random(42);

  /**
   * Constructor that accepts custom stipple counts for user input.
   *
   * @param image The image that will be stippled
   * @param numStipples The number of stipples that will be generated
   */
  public VoronoiStippler(BufferedImage image, int numStipples) {
    this.image = image;
    this.width = image.getWidth();
    this.height = image.getHeight();
    this.density = computeDensityFunction(image);
    this.numStipples = numStipples;
  }

  /** Stores x-y coords in a single object. */
  public static class Point2D {
    public double x, y;

    public Point2D(double x, double y) {
      this.x = x;
      this.y = y;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    @Override
    public String toString() {
      return String.format("(%.2f, %.2f)", x, y);
    }
  }

  /**
   * Compute density function for weighted Voronoi stippling. Represents how many stipples should be
   * placed in each region of the image.
   *
   * @param img The BufferedImage to compute density for
   * @return A 2D array of density values where density[y][x] is in the range [0, 1]
   */
  private float[][] computeDensityFunction(BufferedImage img) {
    float[][] density = new float[height][width];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = img.getRGB(x, y);

        // Extract RGB
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // Convert to grayscale (luminance formula)
        float gray = (r * 0.299f + g * 0.587f + b * 0.114f) / 255f;

        // Density: 1 = black (many stipples), 0 = white (few stipples)
        density[y][x] = 1f - gray;
      }
    }

    return density;
  }

  /**
   * Initialize generators using rejection sampling weighted by density.
   *
   * @param numStipples The number of stipples/points to create
   */
  public void initializeGenerators(int numStipples) {
    generators = new ArrayList<>(numStipples);

    // Compute total weight for rejection sampling
    float totalWeight = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        totalWeight += density[y][x];
      }
    }

    // Rejection sampling
    int added = 0;
    int attempts = 0;
    int maxAttempts = numStipples * 100;

    while (added < numStipples && attempts < maxAttempts) {
      int x = random.nextInt(width);
      int y = random.nextInt(height);
      float threshold = random.nextFloat();

      // Accept with probability proportional to density
      if (threshold < density[y][x]) {
        generators.add(new Point2D(x, y));
        added++;
      }
      attempts++;
    }

    System.out.println("Initialized " + generators.size() + " generators");
  }

  /**
   * Compute Voronoi diagram: for each pixel, find the closest generator.
   *
   * @return voronoi[y][x] = index of closest generator
   */
  private int[][] computeVoronoiDiagram() {
    int[][] voronoi = new int[height][width];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        double minDist = Double.MAX_VALUE;
        int closestGen = 0;

        // Find the closest generator using Euclidean distance
        for (int i = 0; i < generators.size(); i++) {
          Point2D gen = generators.get(i);
          double dx = x - gen.x;
          double dy = y - gen.y;
          double dist = dx * dx + dy * dy;

          if (dist < minDist) {
            minDist = dist;
            closestGen = i;
          }
        }

        voronoi[y][x] = closestGen;
      }
    }

    return voronoi;
  }

  /**
   * Compute weighted centroid of each Voronoi region. Weights are given by the density function.
   *
   * @param voronoi Voronoi Diagram
   * @return New positions of generators
   */
  private List<Point2D> computeWeightedCentroids(int[][] voronoi) {
    double[] weightX = new double[generators.size()];
    double[] weightY = new double[generators.size()];
    double[] totalWeight = new double[generators.size()];

    // Accumulate weighted positions
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int genIndex = voronoi[y][x];
        float weight = density[y][x];

        weightX[genIndex] += x * weight;
        weightY[genIndex] += y * weight;
        totalWeight[genIndex] += weight;
      }
    }

    // Compute centroids
    List<Point2D> newGenerators = new ArrayList<>();
    for (int i = 0; i < generators.size(); i++) {
      if (totalWeight[i] > 0) {
        double centerX = weightX[i] / totalWeight[i];
        double centerY = weightY[i] / totalWeight[i];
        newGenerators.add(new Point2D(centerX, centerY));
      } else {
        // Region disappeared, keep original position
        newGenerators.add(new Point2D(generators.get(i).x, generators.get(i).y));
      }
    }

    return newGenerators;
  }

  /**
   * Iteratively relax generators to centroids using Lloyd's algorithm.
   *
   * @param numIterations The maximum number of iterations
   */
  public void iterateLloyd(int numIterations) {
    for (int iteration = 0; iteration < numIterations; iteration++) {
      // Compute Voronoi diagram
      int[][] voronoi = computeVoronoiDiagram();

      // Compute new generator positions at centroids
      List<Point2D> newGenerators = computeWeightedCentroids(voronoi);

      // Calculate average movement for convergence check
      double totalMovement = 0;
      for (int i = 0; i < generators.size(); i++) {
        Point2D old = generators.get(i);
        Point2D neu = newGenerators.get(i);
        double dx = neu.x - old.x;
        double dy = neu.y - old.y;
        totalMovement += Math.sqrt(dx * dx + dy * dy);
      }

      generators = newGenerators;

      double avgMovement = totalMovement / generators.size();
      System.out.printf("Iteration %d: avg movement = %.4f%n", iteration + 1, avgMovement);

      if (avgMovement < 0.1) {
        System.out.println("Converged!");
        break;
      }
    }
  }

  /**
   * Performs a single iteration of Lloyd's algorithm for progress reporting.
   *
   * @return The average movement
   */
  public double iterateLloydSingleIteration() {
    // Compute Voronoi diagram
    int[][] voronoi = computeVoronoiDiagram();

    // Compute new generator positions at centroids
    List<Point2D> newGenerators = computeWeightedCentroids(voronoi);

    // Calculate average movement for convergence check
    double totalMovement = 0;
    for (int i = 0; i < generators.size(); i++) {
      Point2D old = generators.get(i);
      Point2D neu = newGenerators.get(i);
      double dx = neu.x - old.x;
      double dy = neu.y - old.y;
      totalMovement += Math.sqrt(dx * dx + dy * dy);
    }

    generators = newGenerators;

    return totalMovement / generators.size();
  }

  /**
   * Get stipple points as a 2D array for TSP processing.
   *
   * @return double[][] where [0] is x coords and [1] is y coords
   */
  public double[][] getStipplePointsArray() {
    final int size = generators.size();
    double[] xList = new double[size];
    double[] yList = new double[size];

    for (int i = 0; i < size; i++) {
      Point2D point = generators.get(i);
      xList[i] = point.getX();
      yList[i] = point.getY();
    }

    return new double[][] {xList, yList};
  }

  /**
   * Renders stipples to a BufferedImage.
   *
   * @param stippleRadius The radius of each stipple
   * @return The stippled BufferedImage
   */
  public BufferedImage renderStipples(float stippleRadius) {
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    // Fill with white background
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        result.setRGB(i, j, 0xFFFFFF);
      }
    }

    // Draw black stipples
    for (Point2D stipple : generators) {
      drawCircle(result, (int) stipple.x, (int) stipple.y, (int) stippleRadius, 0x000000);
    }

    return result;
  }

  /**
   * Draws a filled circle on a BufferedImage (Bresenham-like approximation).
   *
   * @param img The BufferedImage being drawn on
   * @param cx The x-coord of the circle's center
   * @param cy The y-coord of the circle's center
   * @param radius The radius of the circle in pixels
   * @param color The color of the circle
   */
  private void drawCircle(BufferedImage img, int cx, int cy, int radius, int color) {
    int r2 = radius * radius;

    for (int dy = -radius; dy <= radius; dy++) {
      for (int dx = -radius; dx <= radius; dx++) {
        if (dx * dx + dy * dy <= r2) {
          int x = cx + dx;
          int y = cy + dy;
          if (x >= 0 && x < width && y >= 0 && y < height) {
            img.setRGB(x, y, color);
          }
        }
      }
    }
  }

  /**
   * Reads a grayscale image.
   *
   * @param imagePath The location of the image as a path
   * @return BufferedImage from the imagePath
   * @throws IOException Image cannot be found
   */
  public static BufferedImage loadGrayscaleImage(String imagePath) throws IOException {
    if (!new File(imagePath).exists()) {
      throw new Error(
          "Image not found: " + imagePath + "\n" + "Please provide an input image file.");
    }

    return ImageIO.read(new File(imagePath));
  }

  /**
   * Stipples image and outputs to project directory.
   *
   * @param image The image to be stippled
   * @return A 2D array of the x-y coordinates for each stipple
   * @throws IOException Image cannot be output to directory
   */
  public static double[][] stipple(BufferedImage image) throws IOException {
    // Convert to grayscale if necessary
    if (image.getType() != BufferedImage.TYPE_INT_RGB) {
      BufferedImage gray =
          new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      for (int i = 0; i < image.getWidth(); i++) {
        for (int j = 0; j < image.getHeight(); j++) {
          int argb = image.getRGB(i, j);
          gray.setRGB(i, j, argb);
        }
      }
      image = gray;
    }

    // Create stippler
    VoronoiStippler stippler = new VoronoiStippler(image, 2000);

    // Parameters
    int numIterations = 50;
    float stippleRadius = 1f;

    System.out.println("Initializing stipples...");
    stippler.initializeGenerators(stippler.numStipples);

    System.out.println("Running Lloyd's algorithm...");
    long startTime = System.currentTimeMillis();
    stippler.iterateLloyd(numIterations);
    long elapsed = System.currentTimeMillis() - startTime;
    System.out.printf("Completed in %.2f seconds%n", elapsed / 1000.0);

    System.out.println("Rendering output...");
    BufferedImage output = stippler.renderStipples(stippleRadius);

    // Save result
    File outputFile = new File("output_stippled2.png");

    // Format return data
    final int size = stippler.generators.size();
    double[] xList = new double[size];
    double[] yList = new double[size];
    for (int i = 0; i < size; i++) {
      Point2D point = stippler.generators.get(i);
      xList[i] = point.getX();
      yList[i] = point.getY();
    }

    ImageIO.write(output, "png", outputFile);
    System.out.println("Saved to: " + outputFile.getAbsolutePath());

    return new double[][] {xList, yList};
  }
}
