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
        // Scenario: Valid CSV with two records
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
        assertEquals(Arrays.asList(6.0, 148.0, 72.0, 35.0, 0.0, 33.6, 0.627, 50.0), records.get(0).getFeatures(), "First record features should match");
        assertTrue(records.get(0).getLabel(), "First record label should be true");
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
        // Scenario: One record with missing value, one complete
        File csvFile = new File(tempDir, "test_missing.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,null,72,35,0,33.6,0.627,50,1\n");
            writer.write("1,85,66,29,0,26.6,0.351,31,0\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        List<Record> records = collector.getRecords();
        assertEquals(2, records.size(), "Should load two records");
        assertEquals(85.0, records.get(0).getFeatures().get(1), "Missing Glucose should be replaced with mean (85.0)");
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
        assertEquals(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), records.get(0).getFeatures(), "All missing features should be set to 0.0");
    }

    @Test
    void test_normalizeInput_normal() throws IOException {
        // Scenario: Normalize input after loading a dataset
        File csvFile = new File(tempDir, "test_normalize.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome\n");
            writer.write("6,148,72,35,0,33.6,0.627,50,1\n");
        }

        RecordCollector collector = new RecordCollector();
        collector.loadFromCSV(csvFile.getAbsolutePath());
        List<Double> input = Arrays.asList(6.0, 148.0, 72.0, 35.0, 0.0, 33.6, 0.627, 50.0);
        List<Double> normalized = collector.normalizeInput(input);
        assertEquals(8, normalized.size(), "Normalized input should have 8 features");
        assertTrue(normalized.get(0) < 1.0 && normalized.get(0) > -1.0, "Normalized values should be standardized");
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