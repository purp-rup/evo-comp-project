package edu.stockton.project;

import org.cicirello.permutations.Permutation;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.FitnessProportionalSelection;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.InverseCostFitnessFunction;
import org.cicirello.search.operators.permutations.EnhancedEdgeRecombination;
import org.cicirello.search.operators.permutations.PermutationInitializer;
import org.cicirello.search.operators.permutations.ReversalMutation;
import org.cicirello.search.problems.tsp.TSP;
import java.io.IOException;

public class TSPArtExample
{
    /* Private constructor to prevent instantiation. */
    private TSPArtExample() {}

    public static void generateTour(double[][] points) {
        // int numCities = (int) CSVwriter.countLinesInCSV("output3.csv");
        int maxGenerations = 100;

        double[] xPoints;
        double[] yPoints;

        xPoints = points[0];
        yPoints = points[1];

        TSP.Double problem =
                new TSP.Double(xPoints, yPoints);

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
                double soultionLength = problem.value(solutionPermutation);
                String gaStr = String.format("C=%.1f; M=%.1f", crossoverRate, mutationRate);
                System.out.printf("%-25s%12f%n", gaStr, soultionLength);
                System.out.println(solutionPermutation);

                if (bestLength > soultionLength) {
                    bestPermutation = solutionPermutation;
                    bestLength = soultionLength;
                }
            }
        }

        // Obtain best coordinates
        int j = 0;
        double[][] orderedPoints = new double[xPoints.length][2];
        for (int i : bestPermutation.toArray()) {
            orderedPoints[j] = new double[]{xPoints[i], yPoints[i]};
        }
    }
}
