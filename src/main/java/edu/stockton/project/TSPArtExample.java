package edu.stockton.project;

import java.awt.Color;
import java.io.IOException;
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

/** Test class for creating and drawing tours. */
public class TSPArtExample {
  /* Private constructor to prevent instantiation. */
  private TSPArtExample() {}

  public static double[][] generateTour(double[][] points) {
    int maxGenerations = 100000;

    double[] xPoints = points[0];
    double[] yPoints = points[1];

    // Print order of coordinates before EA
    StringBuilder printValue = new StringBuilder();
    for (int i = 0; i < xPoints.length; i++) {
      printValue.append("(").append(xPoints[i]).append(", ").append(yPoints[i]).append(") -> ");
    }

    System.out.println(printValue);

    TSP.Double problem = new TSP.Double(xPoints, yPoints);

    int populationSize = 100;
    int numElite = 10;

    Permutation bestPermutation = null;

    System.out.println("-------------------------------------------------");
    System.out.println("Evolutionary Algorithms");
    System.out.println("-------------------------------------------------");
    System.out.printf("%-25s%12s%n", "EA", "best-tour-length");
    System.out.println("-------------------------------------------------");
    double mutationRate = 0.3;
    double crossoverRate = 0.1;
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

    bestPermutation = solutionPermutation;

    // Obtain best coordinates
    double[][] tour = new double[xPoints.length][2];

    printValue = new StringBuilder();

    int j = 0;
    for (int i : bestPermutation.toArray()) {
      tour[j] = new double[] {xPoints[i], yPoints[i]};
      j++;

      // Print order of coordinates after EA
      printValue.append("(").append(xPoints[i]).append(", ").append(yPoints[i]).append(") -> ");
    }

    System.out.println(printValue);

    return tour;
  }

  /**
   * Draws a tour on a graph using the XChart library.
   *
   * @param tour The tour that will be drawn
   * @param outputPath The output path
   * @param dimensions The dimensions of the graph
   * @throws IOException Image cannot be written to outputPath
   */
  public static void drawTour(double[][] tour, String outputPath, int[] dimensions)
      throws IOException {
    // Extract x and y coordinates from the tour
    double[] xData = new double[tour.length + 1];
    double[] yData = new double[tour.length + 1];

    for (int i = 0; i < tour.length; i++) {
      xData[i] = tour[i][0];
      yData[i] = dimensions[1] - tour[i][1]; // Invert y coordinates
    }

    // Close the tour by connecting back to the first point
    xData[tour.length] = tour[0][0];
    yData[tour.length] = tour[0][1];

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

    XYSeries series = chart.addSeries("TSP Tour", xData, yData);
    series.setLineColor(Color.black);
    series.setLineStyle(SeriesLines.SOLID);
    series.setMarker(SeriesMarkers.NONE);
    series.setLineWidth(1.0f);

    // Save final image
    BitmapEncoder.saveBitmap(chart, outputPath, BitmapEncoder.BitmapFormat.PNG);
  }
}
