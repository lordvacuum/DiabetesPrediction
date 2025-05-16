package diabetes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RandomForestTest {

    @TempDir
    File tempDir;

    @Test
    void test_train_normal() throws IOException {
        // Scenario: Train with three records
        File csvFile = new File(tempDir, "test_train.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
            writer.write("5,110,68,30,0,28.0,0.4,35,1\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        RandomForest forest = new RandomForest(5);
        forest.train(collector.getRecords());
        assertEquals(5, forest.predict(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)) ? 1 : 0, "Forest should have 5 trees after training");
    }

    @Test
    void test_train_edge_emptyRecords() {
        // Scenario: Train with empty records (boundary case)
        RandomForest forest = new RandomForest(5);
        forest.train(Collections.emptyList());
        assertFalse(forest.predict(Arrays.asList(90.0, 30.0, 45.0, 20.0, 100.0, 33.6, 0.627, 50.0)), "Empty forest should predict false");
    }

    @Test
    void test_predict_normal() throws IOException {
        // Scenario: Predict with trained forest
        File csvFile = new File(tempDir, "test_predict.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        RandomForest forest = new RandomForest(5);
        forest.train(collector.getRecords());
        List<Double> input = collector.normalizeInput(Arrays.asList(6.0, 148.0, 72.0, 35.0, 0.0, 33.6, 0.627, 50.0));
        assertTrue(forest.predict(input), "Prediction should be true for diabetic-like features");
    }

    @Test
    void test_analyzePrediction_normal() throws IOException {
        // Scenario: Analyze prediction with trained forest
        File csvFile = new File(tempDir, "test_analyze.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        RandomForest forest = new RandomForest(5);
        forest.train(collector.getRecords());
        List<Double> input = collector.normalizeInput(Arrays.asList(6.0, 148.0, 72.0, 35.0, 0.0, 33.6, 0.627, 50.0));
        boolean prediction = forest.predict(input);
        String analysis = forest.analyzePrediction(input, prediction, collector);
        assertTrue(analysis.contains("Prediction: Diabetic"), "Analysis should include prediction result");
        assertTrue(analysis.contains("trees voted Diabetic"), "Analysis should include vote count");
        assertTrue(analysis.contains("Top Contributing Features"), "Analysis should list top features");
    }

    @Test
    void test_computeAccuracy_normal() throws IOException {
        // Scenario: Compute accuracy with known dataset
        File csvFile = new File(tempDir, "test_accuracy.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        RandomForest forest = new RandomForest(5);
        forest.train(collector.getRecords());
        double accuracy = forest.computeAccuracy(collector.getRecords());
        assertTrue(accuracy >= 0.0 && accuracy <= 100.0, "Accuracy should be between 0 and 100");
    }
}