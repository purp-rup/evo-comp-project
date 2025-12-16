# TSP Art Generator

## Highlights
- **Automated Image Processing**: Converts any input image into a set of points for TSP tour generation.
- **Voronoi Stippling**: Uses a weighted voronoi stippling algorithm to extract points from images.
- **Customizable Parameters**: Adjustable settings for number of cities and algorithm-specific parameters to control output fidelity.
- **Visualization**: Image display showing each step of the process.
- **User-Friendly Interface**: Intuitive GUI built with Swing for easy algorithm selection and parameter configuration.

## Overview
The TSP Art Generator is a Java application that creates art using Traveling Salesman Problem (TSP) tours. Unlike traditional TSP art implementations that require predetermined city lists, this tool automates the entire process by accepting any image as input, stippling it, and running your evolutionary algorithm configuration.

## Design
This application was developed using Java 21, leveraging the Chips-n-Salsa library for evolutionary algorithm implementations. The graphical user interface is built using JavaFX, providing an accessible platform for algorithm customization, parameter configuration, and tour visualization.

## Authors
This project was developed by [Kenji Mercado](https://github.com/kmercad), [Joshua Galardi](https://github.com/kirjorjos), and [Andrew Miraglia](https://github.com/purp-rup).

#  How to Run
1. Clone the repository in your preferred IDE
2. Then, run the following commands in your terminal:
```console
mvn clean compile
mvn javafx:run
```

