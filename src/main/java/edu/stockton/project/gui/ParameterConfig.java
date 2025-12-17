package edu.stockton.project.gui;

/** Configuration holder for all EA and stippling parameters */
public class ParameterConfig {

  // Mode
  private boolean gridSearch;

  // Operators
  private String mutationOperator;
  private String crossoverOperator;

  // Grid search parameters
  private double crossoverMin;
  private double crossoverMax;
  private double crossoverStep;
  private double mutationMin;
  private double mutationMax;
  private double mutationStep;

  // Manual parameters
  private double manualCrossoverRate;
  private double manualMutationRate;

  // EA parameters
  private int populationSize;
  private int maxGenerations;
  private int eliteCount;

  // Stippling parameter
  private int stippleCount;

  // Output scaling
  private double scaleFactor;

  public ParameterConfig() {
    // Default values
    this.gridSearch = true;
    this.mutationOperator = "Reversal Mutation";
    this.crossoverOperator = "Enhanced Edge Recombination";
    this.crossoverMin = 0.1;
    this.crossoverMax = 0.9;
    this.crossoverStep = 0.2;
    this.mutationMin = 0.1;
    this.mutationMax = 0.9;
    this.mutationStep = 0.2;
    this.manualCrossoverRate = 0.5;
    this.manualMutationRate = 0.5;
    this.populationSize = 100;
    this.maxGenerations = 100;
    this.eliteCount = 1;
    this.stippleCount = 10000;
    this.scaleFactor = 1.0;
  }

  // Getters and Setters

  public boolean isGridSearch() {
    return gridSearch;
  }

  public void setGridSearch(boolean gridSearch) {
    this.gridSearch = gridSearch;
  }

  public String getMutationOperator() {
    return mutationOperator;
  }

  public void setMutationOperator(String mutationOperator) {
    this.mutationOperator = mutationOperator;
  }

  public String getCrossoverOperator() {
    return crossoverOperator;
  }

  public void setCrossoverOperator(String crossoverOperator) {
    this.crossoverOperator = crossoverOperator;
  }

  public double getCrossoverMin() {
    return crossoverMin;
  }

  public void setCrossoverMin(double crossoverMin) {
    this.crossoverMin = crossoverMin;
  }

  public double getCrossoverMax() {
    return crossoverMax;
  }

  public void setCrossoverMax(double crossoverMax) {
    this.crossoverMax = crossoverMax;
  }

  public double getCrossoverStep() {
    return crossoverStep;
  }

  public void setCrossoverStep(double crossoverStep) {
    this.crossoverStep = crossoverStep;
  }

  public double getMutationMin() {
    return mutationMin;
  }

  public void setMutationMin(double mutationMin) {
    this.mutationMin = mutationMin;
  }

  public double getMutationMax() {
    return mutationMax;
  }

  public void setMutationMax(double mutationMax) {
    this.mutationMax = mutationMax;
  }

  public double getMutationStep() {
    return mutationStep;
  }

  public void setMutationStep(double mutationStep) {
    this.mutationStep = mutationStep;
  }

  public double getManualCrossoverRate() {
    return manualCrossoverRate;
  }

  public void setManualCrossoverRate(double manualCrossoverRate) {
    this.manualCrossoverRate = manualCrossoverRate;
  }

  public double getManualMutationRate() {
    return manualMutationRate;
  }

  public void setManualMutationRate(double manualMutationRate) {
    this.manualMutationRate = manualMutationRate;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public void setPopulationSize(int populationSize) {
    this.populationSize = populationSize;
  }

  public int getMaxGenerations() {
    return maxGenerations;
  }

  public void setMaxGenerations(int maxGenerations) {
    this.maxGenerations = maxGenerations;
  }

  public int getEliteCount() {
    return eliteCount;
  }

  public void setEliteCount(int eliteCount) {
    this.eliteCount = eliteCount;
  }

  public int getStippleCount() {
    return stippleCount;
  }

  public void setStippleCount(int stippleCount) {
    this.stippleCount = stippleCount;
  }

  public double getScaleFactor() {
    return scaleFactor;
  }

  public void setScaleFactor(double scaleFactor) {
    this.scaleFactor = scaleFactor;
  }

  @Override
  public String toString() {
    return "ParameterConfig{"
        + "gridSearch="
        + gridSearch
        + ", mutationOperator='"
        + mutationOperator
        + '\''
        + ", crossoverOperator='"
        + crossoverOperator
        + '\''
        + ", populationSize="
        + populationSize
        + ", maxGenerations="
        + maxGenerations
        + ", eliteCount="
        + eliteCount
        + ", stippleCount="
        + stippleCount
        + ", scaleFactor="
        + scaleFactor
        + '}';
  }
}
