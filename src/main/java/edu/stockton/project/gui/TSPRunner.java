package edu.stockton.project.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.cicirello.permutations.Permutation;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.FitnessProportionalSelection;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.InverseCostFitnessFunction;
import org.cicirello.search.operators.permutations.EnhancedEdgeRecombination;
import org.cicirello.search.operators.permutations.PermutationInitializer;
import org.cicirello.search.operators.permutations.ReversalMutation;
import org.cicirello.search.problems.tsp.TSP;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

/** Handles TSP solving with progress reporting and configurable parameters for GUI integration. */
public class TSPRunner {

  /** Progress callback interface for reporting TSP solving progress. */
  @FunctionalInterface
  public interface ProgressCallback {
    void onProgress(int current, int total, String message);
  }

  /**
   * Generate TSP tour with progress reporting and configurable parameters.
   *
   * @param points double[2][n] array where [0] is x coords and [1] is y coords
   * @param config Parameter configuration from GUI
   * @param callback Progress callback (can be null)
   * @return double[n][2] array representing the tour
   */
  public static double[][] generateTour(
      double[][] points, ParameterConfig config, ProgressCallback callback) {

    double[] xPoints = points[0];
    double[] yPoints = points[1];

    TSP.Double problem = new TSP.Double(xPoints, yPoints);

    int populationSize = config.getPopulationSize();
    int maxGenerations = config.getMaxGenerations();
    int numElite = config.getEliteCount();

    double bestLength = Double.MAX_VALUE;
    Permutation bestPermutation = null;

    if (config.isGridSearch()) {
      // Grid search mode - test multiple parameter combinations
      bestPermutation =
          runGridSearch(
              xPoints,
              yPoints,
              problem,
              config,
              populationSize,
              maxGenerations,
              numElite,
              callback);

    } else {
      // Manual mode - single run with specified rates
      double crossoverRate = config.getManualCrossoverRate();
      double mutationRate = config.getManualMutationRate();

      if (callback != null) {
        callback.onProgress(
            0, 1, String.format("Running EA with C=%.2f M=%.2f", crossoverRate, mutationRate));
      }

      bestPermutation =
          runSingleEA(
              xPoints,
              yPoints,
              problem,
              crossoverRate,
              mutationRate,
              populationSize,
              maxGenerations,
              numElite);

      if (callback != null) {
        double finalLength = problem.value(bestPermutation);
        callback.onProgress(1, 1, String.format("Complete! Tour length: %.2f", finalLength));
      }
    }

    // Convert permutation to tour array
    return permutationToTour(bestPermutation, xPoints, yPoints);
  }

  /** Run grid search over parameter space */
  private static Permutation runGridSearch(
      double[] xPoints,
      double[] yPoints,
      TSP.Double problem,
      ParameterConfig config,
      int populationSize,
      int maxGenerations,
      int numElite,
      ProgressCallback callback) {

    double crossoverMin = config.getCrossoverMin();
    double crossoverMax = config.getCrossoverMax();
    double crossoverStep = config.getCrossoverStep();
    double mutationMin = config.getMutationMin();
    double mutationMax = config.getMutationMax();
    double mutationStep = config.getMutationStep();

    // Calculate total combinations
    int totalCombinations = 0;
    for (double c = crossoverMin; c <= crossoverMax; c += crossoverStep) {
      for (double m = mutationMin; m <= mutationMax; m += mutationStep) {
        totalCombinations++;
      }
    }

    int currentCombination = 0;
    double bestLength = Double.MAX_VALUE;
    Permutation bestPermutation = null;

    System.out.println("-------------------------------------------------");
    System.out.println("Grid Search - Testing " + totalCombinations + " combinations");
    System.out.println("-------------------------------------------------");

    // Test each combination
    for (double crossoverRate = crossoverMin;
        crossoverRate <= crossoverMax;
        crossoverRate += crossoverStep) {

      for (double mutationRate = mutationMin;
          mutationRate <= mutationMax;
          mutationRate += mutationStep) {

        if (callback != null) {
          String msg =
              String.format(
                  "Testing C=%.2f M=%.2f (%d/%d)",
                  crossoverRate, mutationRate, currentCombination + 1, totalCombinations);
          callback.onProgress(currentCombination, totalCombinations, msg);
        }

        Permutation result =
            runSingleEA(
                xPoints,
                yPoints,
                problem,
                crossoverRate,
                mutationRate,
                populationSize,
                maxGenerations,
                numElite);

        double length = problem.value(result);

        System.out.printf("C=%.2f M=%.2f -> Length: %.2f%n", crossoverRate, mutationRate, length);

        if (length < bestLength) {
          bestLength = length;
          bestPermutation = result;
        }

        currentCombination++;
      }
    }

    if (callback != null) {
      callback.onProgress(
          totalCombinations,
          totalCombinations,
          String.format("Best tour length: %.2f", bestLength));
    }

    System.out.println("-------------------------------------------------");
    System.out.printf("Best length found: %.2f%n", bestLength);
    System.out.println("-------------------------------------------------");

    return bestPermutation;
  }

  /** Run a single EA with specified parameters */
  private static Permutation runSingleEA(
      double[] xPoints,
      double[] yPoints,
      TSP.Double problem,
      double crossoverRate,
      double mutationRate,
      int populationSize,
      int maxGenerations,
      int numElite) {

    GenerationalEvolutionaryAlgorithm<Permutation> ea =
        new GenerationalEvolutionaryAlgorithm<>(
            populationSize,
            new ReversalMutation(),
            mutationRate,
            new EnhancedEdgeRecombination(),
            crossoverRate,
            new PermutationInitializer(xPoints.length),
            new InverseCostFitnessFunction<>(problem),
            new FitnessProportionalSelection(),
            numElite);

    SolutionCostPair<Permutation> solution = ea.optimize(maxGenerations);
    return solution.getSolution();
  }

  /** Convert permutation to tour coordinate array */
  private static double[][] permutationToTour(
      Permutation permutation, double[] xPoints, double[] yPoints) {

    int[] order = permutation.toArray();
    double[][] tour = new double[order.length][2];

    for (int i = 0; i < order.length; i++) {
      int cityIndex = order[i];
      tour[i][0] = xPoints[cityIndex];
      tour[i][1] = yPoints[cityIndex];
    }

    return tour;
  }

  /**
   * Draw TSP tour using XChart library (same as TSPArtExample) Returns BufferedImage instead of
   * saving to file
   *
   * @param tour Tour as double[n][2] array
   * @param dimensions int[2] array with [width, height]
   * @return BufferedImage with the tour drawn using XChart
   */
  public static BufferedImage drawTourToImage(double[][] tour, int[] dimensions) {
    // Extract x and y coordinates from the tour
    double[] xData = new double[tour.length + 1];
    double[] yData = new double[tour.length + 1];

    for (int i = 0; i < tour.length; i++) {
      xData[i] = tour[i][0];
      yData[i] = dimensions[1] - tour[i][1]; // Invert y coordinates
    }

    // Close the tour by connecting back to the first point
    xData[tour.length] = tour[0][0];
    yData[tour.length] = dimensions[1] - tour[0][1]; // Also invert for closing point

    // Create chart
    XYChart chart = new XYChartBuilder().width(dimensions[0]).height(dimensions[1]).build();

    // Customize chart
    chart.getStyler().setChartBackgroundColor(Color.white);
    chart.getStyler().setPlotBackgroundColor(Color.white);
    chart.getStyler().setPlotBorderVisible(false);
    chart.getStyler().setLegendVisible(false);
    chart.getStyler().setAxisTicksVisible(false);
    chart.getStyler().setAxisTicksLineVisible(false);
    chart.getStyler().setAxisTitlesVisible(false);
    chart.getStyler().setChartTitleVisible(false);
    chart.getStyler().setPlotGridLinesVisible(false);

    // Set Axis bounds to match dimensions
    chart.getStyler().setXAxisMin(0.0);
    chart.getStyler().setXAxisMax((double) dimensions[0]);
    chart.getStyler().setYAxisMin(0.0);
    chart.getStyler().setYAxisMax((double) dimensions[1]);

    // Add series with styling
    XYSeries series = chart.addSeries("TSP Tour", xData, yData);
    series.setLineColor(Color.black);
    series.setLineStyle(SeriesLines.SOLID);
    series.setMarker(SeriesMarkers.NONE);
    series.setLineWidth(1.0f);

    // Convert chart to BufferedImage instead of saving to file
    return BitmapEncoder.getBufferedImage(chart);
  }
}
