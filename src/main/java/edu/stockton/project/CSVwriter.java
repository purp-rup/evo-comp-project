package edu.stockton.project;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Writes generator data to CSV files */
public class CSVwriter {

  private static final String CSV_FILE_PATH = "output3.csv";
  private List<String[]> data;

  public CSVwriter(List<VoronoiStippler.Point2D> data) {
    this.data = toStringCoordList(data);
  }

  /**
   * Writes all data to CSV file at CSV_FILE_PATH
   */
  public void writeToFile() {
    try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
      writer.writeAll(data);
      System.out.println("CSV file created successfully!");
    } catch (IOException e) {
      System.err.println("Error writing CSV file: " + e.getMessage());
    }
  }

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

  public static long countLinesInCSV(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    return Files.lines(path).count();
  }
}
