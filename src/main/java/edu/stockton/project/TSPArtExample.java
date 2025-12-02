package edu.stockton.project;

import org.cicirello.search.Configurator;
import org.cicirello.search.problems.tsp.TSP;
import org.cicirello.search.problems.tsp.TSP.Double;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TSPArtExample
{
    /* Private constructor to prevent instantiation. */
    private TSPArtExample() {}

    public static void main(String[] args) throws IOException {
        int numCities = (int) CSVwriter.countLinesInCSV("output.csv");
        int maxGenerations = 1000;

        double[] xPoints;
        double[] yPoints;

//        TSP.Double problem =
//                new TSP.Double();
    }
}
