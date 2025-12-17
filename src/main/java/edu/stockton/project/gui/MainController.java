/*
 * AI DISCLOSURE:
 *   This file was created with help from Claude Sonnet 4.5.
 *   It was used to help create the backend logic for the GUI as seen in this file.
 *   All functions in this file specifically were generated.
 *
 *   Total lines of code ~= 2455
 *   Generated lines of code ~= 486
 *   AI generated code ~= 19%
 */

package edu.stockton.project.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/** Controller for the main TSP Art Generator GUI. */
public class MainController {

  // FXML Components - Control Panel
  @FXML private Button loadImageButton;
  @FXML private Label fileNameLabel;
  @FXML private ComboBox<String> mutationComboBox;
  @FXML private ComboBox<String> crossoverComboBox;
  @FXML private RadioButton gridSearchRadio;
  @FXML private RadioButton manualSelectRadio;
  @FXML private ToggleGroup parameterModeGroup;

  // Grid Search Controls
  @FXML private VBox gridSearchPanel;
  @FXML private Slider crossoverMinSlider;
  @FXML private Slider crossoverMaxSlider;
  @FXML private Spinner<Double> crossoverStepSpinner;
  @FXML private Slider mutationMinSlider;
  @FXML private Slider mutationMaxSlider;
  @FXML private Spinner<Double> mutationStepSpinner;
  @FXML private Label crossoverMinLabel;
  @FXML private Label crossoverMaxLabel;
  @FXML private Label mutationMinLabel;
  @FXML private Label mutationMaxLabel;

  // Manual Selection Controls
  @FXML private VBox manualSelectionPanel;
  @FXML private Slider manualCrossoverSlider;
  @FXML private Slider manualMutationSlider;
  @FXML private Label manualCrossoverLabel;
  @FXML private Label manualMutationLabel;

  // EA Parameters
  @FXML private Spinner<Integer> populationSpinner;
  @FXML private Spinner<Integer> generationsSpinner;
  @FXML private Spinner<Integer> eliteSpinner;
  @FXML private Spinner<Integer> stippleCountSpinner;
  @FXML private Spinner<Double> scaleFactorSpinner;

  // Action Button
  @FXML private Button runTspButton;

  // FXML Components - Display Area
  @FXML private ImageView stippledImageView;
  @FXML private ImageView tspArtImageView;
  @FXML private Label stepLabel;
  @FXML private Button previousStepButton;
  @FXML private Button nextStepButton;
  @FXML private Button saveImageButton;

  // FXML Components - Status Bar
  @FXML private Label statusLabel;
  @FXML private ProgressBar progressBar;
  @FXML private Button cancelButton;

  // State variables
  private BufferedImage originalImage;
  private BufferedImage stippledImage;
  private BufferedImage tspArtImage;
  private int currentStep = 0; // 0 = original, 1 = stippled, 2 = tsp
  private boolean isProcessing = false;
  private ProcessingTask currentTask;

  @FXML
  public void initialize() {
    // Set up parameter mode toggle group
    parameterModeGroup = new ToggleGroup();
    gridSearchRadio.setToggleGroup(parameterModeGroup);
    manualSelectRadio.setToggleGroup(parameterModeGroup);
    gridSearchRadio.setSelected(true);

    // Initialize ComboBoxes with operator options
    initializeOperatorOptions();

    // Initialize spinners
    initializeSpinners();

    // Initialize slider labels
    setupSliderListeners();

    // Set up panel visibility toggle
    setupPanelToggle();

    // Set initial button states
    updateButtonStates();

    // Set initial status
    statusLabel.setText("Ready");
    progressBar.setProgress(0);

    System.out.println("MainController initialized");
  }

  private void setupPanelToggle() {
    // Listen for toggle changes and switch panels
    parameterModeGroup
        .selectedToggleProperty()
        .addListener(
            (obs, oldToggle, newToggle) -> {
              if (newToggle == gridSearchRadio) {
                gridSearchPanel.setVisible(true);
                gridSearchPanel.setManaged(true);
                manualSelectionPanel.setVisible(false);
                manualSelectionPanel.setManaged(false);
              } else if (newToggle == manualSelectRadio) {
                gridSearchPanel.setVisible(false);
                gridSearchPanel.setManaged(false);
                manualSelectionPanel.setVisible(true);
                manualSelectionPanel.setManaged(true);
              }
            });

    // Set initial state
    gridSearchPanel.setVisible(true);
    gridSearchPanel.setManaged(true);
    manualSelectionPanel.setVisible(false);
    manualSelectionPanel.setManaged(false);
  }

  private void initializeOperatorOptions() {
    // Mutation operators from Chips-n-Salsa
    mutationComboBox
        .getItems()
        .addAll(
            "Reversal Mutation",
            "Swap Mutation",
            "Insertion Mutation",
            "Rotation Mutation",
            "Scramble Mutation",
            "Block Move Mutation");
    mutationComboBox.setValue("Reversal Mutation");

    // Crossover operators from Chips-n-Salsa
    crossoverComboBox
        .getItems()
        .addAll(
            "Enhanced Edge Recombination",
            "Order Crossover (OX)",
            "Partially Matched Crossover (PMX)",
            "Cycle Crossover (CX)",
            "Uniform Order-Based Crossover (UOBX)",
            "Non-Wrapping Order Crossover (NWOX)",
            "Uniform Partially Matched Crossover (UPMX)",
            "Position Based Crossover (PBX)");
    crossoverComboBox.setValue("Enhanced Edge Recombination");
  }

  private void initializeSpinners() {
    // Population size: 10-10000, default 100
    SpinnerValueFactory<Integer> popFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 10000, 100, 10);
    populationSpinner.setValueFactory(popFactory);

    // Max generations: 10-1000000, default 100000
    SpinnerValueFactory<Integer> genFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000000, 100000, 10);
    generationsSpinner.setValueFactory(genFactory);

    // Elite count: 0-50, default 1
    SpinnerValueFactory<Integer> eliteFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 1, 1);
    eliteSpinner.setValueFactory(eliteFactory);

    // Stipple count: 10-50000, default 1000
    SpinnerValueFactory<Integer> stippleFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 50000, 1000, 1000);
    stippleCountSpinner.setValueFactory(stippleFactory);

    // Scale factor: 1.0-5.0, default 1.0, step 0.5
    SpinnerValueFactory<Double> scaleFactory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 5.0, 1.0, 0.5);
    scaleFactorSpinner.setValueFactory(scaleFactory);

    // Crossover/Mutation step spinners (for grid search)
    SpinnerValueFactory<Double> crossoverStepFactory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0.05, 0.5, 0.2, 0.05);
    crossoverStepSpinner.setValueFactory(crossoverStepFactory);

    SpinnerValueFactory<Double> mutationStepFactory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0.05, 0.5, 0.2, 0.05);
    mutationStepSpinner.setValueFactory(mutationStepFactory);
  }

  private void setupSliderListeners() {
    // Grid search sliders
    crossoverMinSlider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> crossoverMinLabel.setText(String.format("%.2f", newVal)));
    crossoverMaxSlider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> crossoverMaxLabel.setText(String.format("%.2f", newVal)));
    mutationMinSlider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> mutationMinLabel.setText(String.format("%.2f", newVal)));
    mutationMaxSlider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> mutationMaxLabel.setText(String.format("%.2f", newVal)));

    // Manual selection sliders
    manualCrossoverSlider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> manualCrossoverLabel.setText(String.format("%.2f", newVal)));
    manualMutationSlider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> manualMutationLabel.setText(String.format("%.2f", newVal)));

    // Initialize labels
    crossoverMinLabel.setText(String.format("%.2f", crossoverMinSlider.getValue()));
    crossoverMaxLabel.setText(String.format("%.2f", crossoverMaxSlider.getValue()));
    mutationMinLabel.setText(String.format("%.2f", mutationMinSlider.getValue()));
    mutationMaxLabel.setText(String.format("%.2f", mutationMaxSlider.getValue()));
    manualCrossoverLabel.setText(String.format("%.2f", manualCrossoverSlider.getValue()));
    manualMutationLabel.setText(String.format("%.2f", manualMutationSlider.getValue()));
  }

  @FXML
  private void onLoadImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));

    File selectedFile = fileChooser.showOpenDialog(loadImageButton.getScene().getWindow());

    if (selectedFile != null) {
      try {
        originalImage = ImageIO.read(selectedFile);
        fileNameLabel.setText(selectedFile.getName());

        // Clear previous results
        stippledImage = null;
        tspArtImage = null;

        // Display original image
        currentStep = 0;
        displayCurrentStep();
        updateButtonStates();

        statusLabel.setText("Image loaded: " + selectedFile.getName());

      } catch (IOException e) {
        showError("Failed to load image", e.getMessage());
      }
    }
  }

  @FXML
  private void onRunTSP() {
    if (originalImage == null) {
      showError("No Image", "Please load an image first.");
      return;
    }

    if (isProcessing) {
      showError("Already Processing", "Please wait for the current operation to complete.");
      return;
    }

    // Build configuration from UI
    ParameterConfig config = buildConfigFromUI();

    // Create and configure the background task
    currentTask = new ProcessingTask(originalImage, config);

    // Bind progress and status
    progressBar.progressProperty().bind(currentTask.progressProperty());
    statusLabel.textProperty().bind(currentTask.messageProperty());

    // Handle task completion
    currentTask.setOnSucceeded(
        event -> {
          ProcessingResult result = currentTask.getValue();
          if (result != null) {
            stippledImage = result.stippledImage;
            tspArtImage = result.tspArtImage;

            // Display results
            currentStep = 2; // Jump to final result
            displayResults();
            updateButtonStates();

            statusLabel.textProperty().unbind();
            statusLabel.setText("Processing complete!");
          }
          isProcessing = false;
          currentTask = null;
          progressBar.progressProperty().unbind();
          cancelButton.setVisible(false);
          updateButtonStates();
        });

    // Handle task failure
    currentTask.setOnFailed(
        event -> {
          Throwable error = currentTask.getException();
          showError("Processing Error", "An error occurred: " + error.getMessage());
          error.printStackTrace();

          isProcessing = false;
          currentTask = null;
          statusLabel.textProperty().unbind();
          statusLabel.setText("Processing failed");
          progressBar.progressProperty().unbind();
          progressBar.setProgress(0);
          cancelButton.setVisible(false);
          updateButtonStates();
        });

    // Handle task cancellation
    currentTask.setOnCancelled(
        event -> {
          isProcessing = false;
          currentTask = null;
          statusLabel.textProperty().unbind();
          statusLabel.setText("Processing cancelled");
          progressBar.progressProperty().unbind();
          progressBar.setProgress(0);
          cancelButton.setVisible(false);
          updateButtonStates();
        });

    // Start processing
    isProcessing = true;
    cancelButton.setVisible(true);
    cancelButton.setManaged(true);
    updateButtonStates();

    Thread thread = new Thread(currentTask);
    thread.setDaemon(true);
    thread.start();
  }

  /** Build ParameterConfig from current UI state */
  private ParameterConfig buildConfigFromUI() {
    ParameterConfig config = new ParameterConfig();

    // Operators
    config.setMutationOperator(mutationComboBox.getValue());
    config.setCrossoverOperator(crossoverComboBox.getValue());

    // Mode
    config.setGridSearch(gridSearchRadio.isSelected());

    // Grid search parameters
    config.setCrossoverMin(crossoverMinSlider.getValue());
    config.setCrossoverMax(crossoverMaxSlider.getValue());
    config.setCrossoverStep(crossoverStepSpinner.getValue());
    config.setMutationMin(mutationMinSlider.getValue());
    config.setMutationMax(mutationMaxSlider.getValue());
    config.setMutationStep(mutationStepSpinner.getValue());

    // Manual parameters
    config.setManualCrossoverRate(manualCrossoverSlider.getValue());
    config.setManualMutationRate(manualMutationSlider.getValue());

    // Common parameters
    config.setPopulationSize(populationSpinner.getValue());
    config.setMaxGenerations(generationsSpinner.getValue());
    config.setEliteCount(eliteSpinner.getValue());
    config.setStippleCount(stippleCountSpinner.getValue());
    config.setScaleFactor(scaleFactorSpinner.getValue());

    return config;
  }

  /** Display both stippled and TSP art results side by side */
  private void displayResults() {
    if (stippledImage != null) {
      Image fxStippled = SwingFXUtils.toFXImage(stippledImage, null);
      stippledImageView.setImage(fxStippled);
    }

    if (tspArtImage != null) {
      Image fxTspArt = SwingFXUtils.toFXImage(tspArtImage, null);
      tspArtImageView.setImage(fxTspArt);
    }

    stepLabel.setText("Step 3 of 3: TSP Art Complete");
  }

  @FXML
  private void onPreviousStep() {
    if (currentStep > 0) {
      currentStep--;
      displayCurrentStep();
      updateButtonStates();
    }
  }

  @FXML
  private void onNextStep() {
    int maxStep = 2;
    if (tspArtImage != null) maxStep = 2;
    else if (stippledImage != null) maxStep = 1;

    if (currentStep < maxStep) {
      currentStep++;
      displayCurrentStep();
      updateButtonStates();
    }
  }

  @FXML
  private void onSaveImage() {
    BufferedImage imageToSave;
    String defaultName;

    // Determine which image to save based on current step
    switch (currentStep) {
      case 0:
        imageToSave = originalImage;
        defaultName = "original.png";
        break;
      case 1:
        imageToSave = stippledImage;
        defaultName = "stippled.png";
        break;
      case 2:
        imageToSave = tspArtImage;
        defaultName = "tsp-art.png";
        break;
      default:
        System.err.println("Invalid step: " + currentStep);
        return;
    }

    if (imageToSave == null) {
      showError("No Image", "No image available to save.");
      return;
    }

    // Create file chooser
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Image");
    fileChooser.setInitialFileName(defaultName);
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("PNG Image", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Image", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));

    // Show save dialog
    File saveFile = fileChooser.showSaveDialog(saveImageButton.getScene().getWindow());

    if (saveFile != null) {
      try {
        // Determine format from extension
        String fileName = saveFile.getName().toLowerCase();
        String format = "png"; // default
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
          format = "jpg";
        }

        // Save the image
        ImageIO.write(imageToSave, format, saveFile);
        statusLabel.setText("Image saved: " + saveFile.getName());

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Image Saved");
        alert.setHeaderText(null);
        alert.setContentText("Image saved successfully to:\n" + saveFile.getAbsolutePath());
        alert.showAndWait();

      } catch (IOException e) {
        showError("Save Failed", "Failed to save image: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @FXML
  private void onCancel() {
    if (currentTask != null && isProcessing) {
      currentTask.cancel();
      statusLabel.setText("Cancelling...");
    }
  }

  private void displayCurrentStep() {
    BufferedImage imageToDisplay = null;
    String stepName = "";

    switch (currentStep) {
      case 0:
        imageToDisplay = originalImage;
        stepName = "Original";
        break;
      case 1:
        imageToDisplay = stippledImage;
        stepName = "Stippled";
        break;
      case 2:
        imageToDisplay = tspArtImage;
        stepName = "TSP Art";
        break;
      default:
        System.err.println("Invalid step: " + currentStep);
        break;
    }

    stepLabel.setText("Step " + (currentStep + 1) + " of 3: " + stepName);

    if (imageToDisplay != null) {
      Image fxImage = SwingFXUtils.toFXImage(imageToDisplay, null);

      // For now, display in the stippled view
      // Later we'll have better layout logic
      stippledImageView.setImage(fxImage);
      stippledImageView.setPreserveRatio(true);
    } else {
      stippledImageView.setImage(null);
    }
  }

  private void updateButtonStates() {
    previousStepButton.setDisable(currentStep == 0);

    int maxStep = 0;
    if (originalImage != null) maxStep = 0;
    if (stippledImage != null) maxStep = 1;
    if (tspArtImage != null) maxStep = 2;

    nextStepButton.setDisable(currentStep >= maxStep);
    runTspButton.setDisable(originalImage == null || isProcessing);
    saveImageButton.setDisable(
        currentStep == 0
            || (currentStep == 1 && stippledImage == null)
            || (currentStep == 2 && tspArtImage == null));
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
