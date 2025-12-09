package edu.stockton.project;

import org.cicirello.permutations.Permutation;
import org.cicirello.search.Configurator;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.AdaptiveEvolutionaryAlgorithm;
import org.cicirello.search.evo.FitnessProportionalSelection;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.InverseCostFitnessFunction;
import org.cicirello.search.operators.permutations.EnhancedEdgeRecombination;
import org.cicirello.search.operators.permutations.PermutationInitializer;
import org.cicirello.search.operators.permutations.ReversalMutation;
import org.cicirello.search.problems.tsp.RandomTSPMatrix;

/** Lab assignment to introduce using the Chips-n-Salsa library for evolutionary computation. */
public final class EvolutionaryAlgorithmLab {

  /* private constructor to prevent instantiation. */
  private EvolutionaryAlgorithmLab() {}

  /**
   * Runs the lab assignment.
   *
   * @param args There are no command line arguments.
   */
  public static void main(String[] args) {
    Configurator.configureRandomGenerator(101);

    int numCities = 100;
    int maxDistance = 10000000;
    boolean symmetric = true;
    boolean triangleInequality = true;
    long seed = 42;

    RandomTSPMatrix.Integer problem =
        new RandomTSPMatrix.Integer(numCities, maxDistance, symmetric, triangleInequality, seed);

    System.out.println(problem);
    int populationSize = 100;
    int maxGenerations = 1000;
    int numElite = 10;

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
                new PermutationInitializer(numCities),
                new InverseCostFitnessFunction<Permutation>(problem),
                new FitnessProportionalSelection(),
                numElite);
        SolutionCostPair<Permutation> solution = ea.optimize(maxGenerations);
        Permutation solutionPermutation = solution.getSolution();
        String gaStr = String.format("C=%.1f; M=%.1f", crossoverRate, mutationRate);
        System.out.printf("%-25s%12d%n", gaStr, problem.value(solutionPermutation));
      }
    }
    System.out.println("-------------------------------------------------");
    System.out.println("Adaptive Evolutionary Algorithms");
    System.out.println("-------------------------------------------------");
    System.out.printf("%-25s%12s%n", "EA", "best-tour-length");
    System.out.println("-------------------------------------------------");

    AdaptiveEvolutionaryAlgorithm<Permutation> ea =
        new AdaptiveEvolutionaryAlgorithm<Permutation>(
            populationSize,
            new ReversalMutation(),
            new EnhancedEdgeRecombination(),
            new PermutationInitializer(numCities),
            new InverseCostFitnessFunction<Permutation>(problem),
            new FitnessProportionalSelection(),
            numElite);
    SolutionCostPair<Permutation> solution = ea.optimize(maxGenerations);
    Permutation solutionPermutation = solution.getSolution();
    System.out.printf("%-25s%12d%n", "Adaptive", problem.value(solutionPermutation));
  }
}
