package edu.stockton.project;

import java.io.IOException;

/** Main class for the Evolutionary Computation Project. */
public class Main {
	public static void main(String[] args) throws IOException {
		String imagePath;
		if (args.length == 0) {
			imagePath = "src/main/java/edu/stockton/project/dog.jpg";
		} else {
			imagePath = args[0];
		}
		double[][] points = VoronoiStippler.stipple(imagePath);
		TSPArtExample.generateTour(points);
	}
}
