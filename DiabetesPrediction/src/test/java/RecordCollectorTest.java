package diabetes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RecordCollectorTest {

    @TempDir
    File tempDir;

    @Test
    void test_loadFromCSV_normal() throws IOException {
        // Scenario: Valid CSV with two records, expecting normalized features after loading
        File csvFile = new File(tempDir, "test.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
        }

        RecordCollector collector = new RecordCollector();
        boolean loaded = collector.loadFromCSV(csvFile.getAbsolutePath());
        assertTrue(loaded, "loadFromCSV should return true for valid CSV");
        List<Record> records = collector.getRecords();
        assertEquals(2, records.size(), "Should load two records");

        // Since loadFromCSV applies normalization, expect z-scores (values close to 0 or -1 to 1)
        List<Double> firstFeatures = records.get(0).getFeatures();
        assertEquals(8, firstFeatures.size(), "First record should have 8 normalized features");
        assertTrue(firstFeatures.stream().allMatch(v -> v >= -3.0 && v <= 3.0), "Normalized features should be within typical z-score range");

        // Verify labels (not affected by normalization)
        assertTrue(records.get(0).getLabel(), "First record label should be true");
        assertFalse(records.get(1).getLabel(), "Second record label should be false");
    }

    @Test
    void test_loadFromCSV_error_incoherent() {
        // Scenario: Non-existent CSV file (boundary case)
        RecordCollector collector = new RecordCollector();
        assertFalse(collector.loadFromCSV("invalid.csv"), "loadFromCSV should return false for non-existent file");
        assertTrue(collector.getRecords().isEmpty(), "Records should remain empty");
    }

    @Test
    void test_handleMissingData_normal() throws IOException {
        // Scenario: One record with missing Glucose, one complete, expecting mean replacement
        File csvFile = new File(tempDir, "test_missing.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,null,72,35,0,33.6,0.627,50,1\n"); // Missing Glucose
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n"); // Complete record
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        List<Record> records = collector.getRecords();
        assertEquals(2, records.size(), "Should load two records");

        // After normalization, the mean of Glucose (85.0 from the second record) is used for the missing value
        // Then normalized, so we check if the z-score is consistent
        double meanGlucose = 85.0; // From the single valid value before normalization
        double stdDevGlucose = Math.sqrt(0); // Std dev is 0 with one value, set to 1 in normalizeFeatures
        double expectedNormalized = (meanGlucose - meanGlucose) / 1.0; // Should be 0.0 after normalization
        assertEquals(expectedNormalized, records.get(0).getFeatures().get(1), 0.01, "Missing Glucose should be normalized to 0.0 with one valid value");
    }

    @Test
    void test_handleMissingData_edge_allMissing() throws IOException {
        // Scenario: Record with all features missing (boundary case)
        File csvFile = new File(tempDir, "test_all_missing.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("null,null,null,null,null,null,null,null,1\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        List<Record> records = collector.getRecords();
        assertEquals(1, records.size(), "Should load one record");
        assertEquals(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), records.get(0).getFeatures(), "All missing features should be set to 0.0 before normalization");
        // After normalization with all 0.0, z-scores will be 0.0 since mean=0 and stdDev=1
        assertTrue(records.get(0).getFeatures().stream().allMatch(v -> Math.abs(v) < 0.01), "Normalized values should be near 0.0");
    }

    @Test
    void test_normalizeInput_normal() throws IOException {
        // Scenario: Normalize input after loading a dataset
        File csvFile = new File(tempDir, "test_normalize.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        List<Double> input = Arrays.asList(6.0, 148.0, 72.0, 35.0, 0.0, 33.6, 0.627, 50.0);
        List<Double> normalized = collector.normalizeInput(input);
        assertEquals(8, normalized.size(), "Normalized input should have 8 features");
        assertTrue(normalized.stream().allMatch(v -> v >= -3.0 && v <= 3.0), "Normalized values should be within typical z-score range");
    }

    @Test
    void test_normalizeInput_error_untrained() {
        // Scenario: Normalize before loading dataset (boundary case)
        RecordCollector collector = new RecordCollector();
        List<Double> input = Arrays.asList(6.0, 148.0, 72.0, 35.0, 0.0, 33.6, 0.627, 50.0);
        assertThrows(IllegalStateException.class, () -> {
            collector.normalizeInput(input);
        }, "normalizeInput should throw IllegalStateException if untrained");
    }
}