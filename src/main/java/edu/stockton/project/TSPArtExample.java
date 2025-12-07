package edu.stockton.project;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.cicirello.permutations.Permutation;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.FitnessProportionalSelection;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.InverseCostFitnessFunction;
import org.cicirello.search.operators.permutations.EnhancedEdgeRecombination;
import org.cicirello.search.operators.permutations.PermutationInitializer;
import org.cicirello.search.operators.permutations.ReversalMutation;
import org.cicirello.search.problems.tsp.TSP;

public class TSPArtExample {
  /* Private constructor to prevent instantiation. */
  private TSPArtExample() {}

  public static double[][] generateTour(double[][] points) {
    // int numCities = (int) CSVwriter.countLinesInCSV("output3.csv");
    int maxGenerations = 100;

    double[] xPoints;
    double[] yPoints;

    xPoints = points[0];
    yPoints = points[1];

    TSP.Double problem = new TSP.Double(xPoints, yPoints);

    int populationSize = 10;
    int numElite = 1;

    double bestLength = Double.MAX_VALUE;
    Permutation bestPermutation = null;

    System.out.println("-------------------------------------------------");
    System.out.println("Evolutionary Algorithms");
    System.out.println("-------------------------------------------------");
    System.out.printf("%-25s%12s%n", "EA", "best-tour-length");
    System.out.println("-------------------------------------------------");
    for (double crossoverRate = 0.1; crossoverRate < 0.95; crossoverRate += 0.2) {
      for (double mutationRate = 0.1; mutationRate < 0.95; mutationRate += 0.2) {
        GenerationalEvolutionaryAlgorithm<Permutation> ea =
            new GenerationalEvolutionaryAlgorithm<Permutation>(
                populationSize,
                new ReversalMutation(),
                mutationRate,
                new EnhancedEdgeRecombination(),
                crossoverRate,
                new PermutationInitializer(xPoints.length),
                new InverseCostFitnessFunction<Permutation>(problem),
                new FitnessProportionalSelection(),
                numElite);
        SolutionCostPair<Permutation> solution = ea.optimize(maxGenerations);
        Permutation solutionPermutation = solution.getSolution();
        double solutionLength = problem.value(solutionPermutation);
        String gaStr = String.format("C=%.1f; M=%.1f", crossoverRate, mutationRate);
        System.out.printf("%-25s%12f%n", gaStr, solutionLength);
        System.out.println(solutionPermutation);

        if (bestLength > solutionLength) {
          bestPermutation = solutionPermutation;
          bestLength = solutionLength;
        }
      }
    }

    // Obtain best coordinates
    int j = 0;
    double[][] tour = new double[xPoints.length][2];
    for (int i : bestPermutation.toArray()) {
      tour[j] = new double[] {xPoints[i], yPoints[i]};
    }

    return tour;
  }

  public static void drawTour(double[][] tour, String outputPath, int[] dimensions)
      throws IOException {
    BufferedImage image =
        new BufferedImage(dimensions[0], dimensions[1], BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D g2d = image.createGraphics();

    drawLine(tour[0], tour[1], g2d);
    for (int i = 1; i < tour.length; i++) {
      drawLine(tour[i - 1], tour[i], g2d);
    }
    drawLine(tour[tour.length - 1], tour[0], g2d);

    g2d.dispose();

    ImageIO.write(image, "png", new File(outputPath));
    System.out.println("Image saved to " + outputPath);

    //    Graphics2D image =
    //        new BufferedImage(dimensions[0], dimensions[1], BufferedImage.TYPE_BYTE_GRAY)
    //            .createGraphics();

    //    drawLine(tour[0], tour[1], image);
    //    for (int i = 1; i < tour.length; i++) {
    //      drawLine(tour[i - 1], tour[i], image);
    //    }
    //    drawLine(tour[tour.length - 1], tour[0], image);
  }

  private static void drawLine(double[] point1, double[] point2, Graphics2D image) {
    double x1 = point1[0];
    double x2 = point2[0];
    double y1 = point1[1];
    double y2 = point2[1];
    image.drawLine(x1, y1, x2, y2);
  }
}
