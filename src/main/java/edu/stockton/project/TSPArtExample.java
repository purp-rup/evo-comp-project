package edu.stockton.project;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

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
    int maxGenerations = 100;
    int populationSize = 10;
    int numElite = 1;
    double[] xPoints = points[0];
    double[] yPoints = points[1];
    TSP.Double problem = new TSP.Double(xPoints, yPoints);
    ArrayList<SoultionHolder> potentialSoultions = new ArrayList<SoultionHolder>();

    System.out.println("-------------------------------------------------");
    System.out.println("Evolutionary Algorithms");
    System.out.println("-------------------------------------------------");
    System.out.printf("%-25s%12s%n", "EA", "best-tour-length");
    System.out.println("-------------------------------------------------");
    for (double crossoverRate = 0.1; crossoverRate < 0.95; crossoverRate += 0.2) {
      for (double mutationRate = 0.1; mutationRate < 0.95; mutationRate += 0.2) {
        potentialSoultions.add(findBestPermutation(populationSize, mutationRate, crossoverRate, xPoints, problem, numElite, maxGenerations));
      }
    }

    Permutation bestPermutation = potentialSoultions.stream()
        .min(Comparator.comparingDouble(SoultionHolder::getLength)) // find the element with the lowest length
        .get().getPermutation(); // get the permutation of that element

    return permutationToTour(xPoints, yPoints, bestPermutation);
  }

  private static SoultionHolder findBestPermutation(int populationSize, double mutationRate, double crossoverRate, double[] xPoints, TSP.Double problem, int numElite, int maxGenerations) {
    Permutation solutionPermutation = calculateSoultionPermutation(populationSize, mutationRate, crossoverRate, xPoints, problem, numElite, maxGenerations);
    double solutionLength = problem.value(solutionPermutation);
    String gaStr = String.format("C=%.1f; M=%.1f", crossoverRate, mutationRate);
    System.out.printf("%-25s%12f%n", gaStr, solutionLength);
    System.out.println(solutionPermutation);
    return new SoultionHolder(solutionPermutation, solutionLength);
  }

  private static Permutation calculateSoultionPermutation(int populationSize, double mutationRate, double crossoverRate, double[] xPoints, TSP.Double problem, int numElite, int maxGenerations) {
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
         return solution.getSolution();
  }

  private static double[][] permutationToTour(double[] xPoints, double[] yPoints, Permutation bestPermutation) {
    int j = 0;
    double[][] tour = new double[xPoints.length][2];
    for (int i : bestPermutation.toArray()) {
      tour[j] = new double[] {xPoints[i], yPoints[i]};
    }
    return tour;
  }

  private static class SoultionHolder {
    private Permutation permutation;
    private double length;

    public SoultionHolder(Permutation permutation, double length) {
        this.permutation = permutation;
        this.length = length;
    }

    public double getLength() {
        return length;
    }

    public Permutation getPermutation() {
        return permutation;
    }
  }


  public static void drawTour(double[][] tour, String outputPath, VoronoiStippler stippler) throws IOException {
    int height = stippler.getHeight();
    int width = stippler.getWidth();
    BufferedImage image =
        new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D g2d = image.createGraphics();

    drawLine(tour[0], tour[1], g2d);
    for (int i = 1; i < tour.length; i++) {
      drawLine(tour[i - 1], tour[i], g2d);
    }
    drawLine(tour[tour.length - 1], tour[0], g2d);

    g2d.dispose();

    ImageIO.write(image, "png", new File(outputPath));
    System.out.println("Image saved to " + outputPath);
  }

  private static void drawLine(double[] point1, double[] point2, Graphics2D image) {
    int x1 = (int) point1[0];
    int x2 = (int) point2[0];
    int y1 = (int) point1[1];
    int y2 = (int) point2[1];
    image.drawLine(x1, y1, x2, y2);
  }
}
