package edu.stockton.project;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Writes generator data to CSV files */
public class CSVwriter {

  private static final String CSV_FILE_PATH = "output3.csv";
  private List<String[]> data;
//  private List<Double> points;

  public CSVwriter(List<VoronoiStippler.Point2D> data) {
//    this.data = toStringCoordList(data);
//    this.points = toDoubleCoordList(data);
  }

  public static void writeCSV(List<VoronoiStippler.Point2D> data, String filePath) throws IOException {
    File file = new File(filePath);
    FileWriter fileWriter = new FileWriter(file);
    StringBuilder strList = new StringBuilder();
    for (VoronoiStippler.Point2D point : data) {
      double x = point.getX();
      double y = point.getY();
      String str = (x + "," + y + "\n");
      strList.append(str);
    }
    fileWriter.write(strList.toString());
    fileWriter.close();
  }

  /**
   *
   * @param filePath
   * @return {x[], y[]}
   * @throws FileNotFoundException
   */
  public static double[][] readCSV(String filePath, int length) throws FileNotFoundException {
    File file = new File(filePath);
    Scanner reader = new Scanner(file);
    double[] xList = new double[length];
    double[] yList = new double[length];
    double[][] returnValue = new double[][]{xList, yList};
    int i = 0;
    while (reader.hasNextLine()) {
      String line = reader.nextLine();
      String[] parts = line.split(",");
      double x = Double.parseDouble(parts[0]);
      double y = Double.parseDouble(parts[1]);
      xList[i] = x;
      yList[i] = y;
      i++;
    }
    reader.close();
    return returnValue;
  }

  /**
   * Writes all data to CSV file at CSV_FILE_PATH
   */
//  public void writeToFile() {
////    try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
////      writer.writeAll(data);
////      System.out.println("CSV file created successfully!");
////    } catch (IOException e) {
////      System.err.println("Error writing CSV file: " + e.getMessage());
////    }
//
//    writeCSV()
//  }

  /**
   * Converts List<VoronoiStippler.Point2D> to List<String[]>
   * @param data The List<VoronoiStippler.Point> list to be converted
   * @return The converted data as List<String[]>
   */
  private static List<String[]> toStringCoordList(List<VoronoiStippler.Point2D> data) {
    List<String[]> newList = new ArrayList<>();

    for (VoronoiStippler.Point2D point : data) {
      String[] pointAsString = new String[2];
      pointAsString[0] = String.valueOf(point.getX());
      pointAsString[1] = String.valueOf(point.getY());
      newList.add(pointAsString);
    }

    return newList;
  }

//  private static List<Double> toDoubleCoordList(List<VoronoiStippler.Point2D> data) {
//    List<Double> xPoints = new ArrayList<>();
//    List<Double> yPoints = new ArrayList<>();
//
//    for (VoronoiStippler.Point2D point : data) {
//      xPoints.add(point.getX());
//      yPoints.add(point.getY());
//    }
//
//    xPoints.addAll(yPoints);
//    return xPoints;
//  }

  public static long countLinesInCSV(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    return Files.lines(path).count();
  }
}
