package edu.stockton.project;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Weighted Voronoi Stippler based on Secord (2002) Generates stipple drawings from grayscale images
 * using Lloyd's algorithm
 */
public class VoronoiStippler {
  private final BufferedImage image;
  private final int width;
  private final int height;
  private final float[][] density; // density[y][x] = 1 - normalized_brightness
  private List<Point2D> generators;
  private final int numStipples;
  private final Random random = new Random(42);

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

  public VoronoiStippler(BufferedImage image) {
    this.image = image;
    this.width = image.getWidth();
    this.height = image.getHeight();
    this.density = computeDensityFunction(image);
    this.numStipples = 10000;
  }

  public int getNumStipples() {
    return numStipples;
  }

  /**
   * Compute density function: ρ(x,y) = 1 - normalized_brightness Higher values attract more
   * stipples (dark areas)
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

  /** Initialize generators using rejection sampling weighted by density */
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
   * Compute Voronoi diagram: for each pixel, find closest generator Returns: voronoi[y][x] = index
   * of closest generator
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

  /** Compute weighted centroid of each Voronoi region Weights are given by the density function */
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

  /** Lloyd's algorithm: iteratively relax generators to centroids */
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

  /** Get the final stipple positions */
  public List<Point2D> getStipples() {
    return new ArrayList<>(generators);
  }

  /** Render stipples to a BufferedImage */
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

  /** Draw a filled circle (Bresenham-like approximation) */
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

  
  public static double[][] stipple(String imagePath) throws IOException {
    // Load a grayscale image

    if (!new File(imagePath).exists()) {
      throw new Error("Image not found: " + imagePath + "\n"
       + "Please provide an input image file.");
    }

    BufferedImage image = ImageIO.read(new File(imagePath));

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
    VoronoiStippler stippler = new VoronoiStippler(image);

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

    //Format return data
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

    return new double[][]{xList, yList};
  }
}
