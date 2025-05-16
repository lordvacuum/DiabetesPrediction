package diabetes;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RecordTest {

    @Test
    void test_getFeatures_normal() {
        // Scenario: Valid record with typical feature values
        List<Double> features = Arrays.asList(120.0, 30.0, 45.0, 20.0, 100.0, 33.6, 0.627, 50.0);
        Record record = new Record(1, features, true);
        assertEquals(features, record.getFeatures(), "getFeatures should return the input features");
    }

    @Test
    void test_getFeatures_edge_empty() {
        // Scenario: Invalid record with empty features (boundary case)
        assertThrows(IllegalArgumentException.class, () -> {
            new Record(2, Collections.emptyList(), false);
        }, "Empty features should throw IllegalArgumentException");
    }

    @Test
    void test_getLabel_normal() {
        // Scenario: Valid record with true label
        List<Double> features = Arrays.asList(110.0, 25.0, 40.0, 15.0, 80.0, 26.6, 0.351, 31.0);
        Record record = new Record(3, features, true);
        assertTrue(record.getLabel(), "getLabel should return true");
    }

    @Test
    void test_getLabel_edge_false() {
        // Scenario: Valid record with false label (boundary case)
        List<Double> features = Arrays.asList(90.0, 20.0, 35.0, 10.0, 60.0, 22.5, 0.2, 25.0);
        Record record = new Record(4, features, false);
        assertFalse(record.getLabel(), "getLabel should return false");
    }
}