package diabetes;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class TreeTest {

    @Test
    void test_build_normal() {
        // Scenario: Build tree with three records
        List<Record> records = Arrays.asList(
                new Record(1, Arrays.asList(120.0, 30.0, 45.0, 20.0, 100.0, 33.6, 0.627, 50.0), true),
                new Record(2, Arrays.asList(90.0, 20.0, 35.0, 10.0, 60.0, 22.5, 0.2, 25.0), false),
                new Record(3, Arrays.asList(110.0, 28.0, 40.0, 15.0, 80.0, 26.6, 0.351, 31.0), true)
        );
        Tree tree = new Tree();
        tree.build(records, new Random(42));
        assertNotNull(tree, "Tree should be built with non-null root");
    }

    @Test
    void test_build_edge_emptyRecords() {
        // Scenario: Build tree with empty records (boundary case)
        Tree tree = new Tree();
        tree.build(Collections.emptyList(), new Random(42));
        assertFalse(tree.predict(Arrays.asList(90.0, 30.0, 45.0, 20.0, 100.0, 33.6, 0.627, 50.0)), "Empty tree should predict false by default");
    }

    @Test
    void test_predict_normal() {
        // Scenario: Predict with a simple tree
        List<Record> records = Arrays.asList(
                new Record(1, Arrays.asList(120.0, 30.0, 45.0, 20.0, 100.0, 33.6, 0.627, 50.0), true),
                new Record(2, Arrays.asList(90.0, 20.0, 35.0, 10.0, 60.0, 22.5, 0.2, 25.0), false)
        );
        Tree tree = new Tree();
        tree.build(records, new Random(42));
        boolean prediction = tree.predict(Arrays.asList(120.0, 30.0, 45.0, 20.0, 100.0, 33.6, 0.627, 50.0));
        assertTrue(prediction, "Prediction should be true for diabetic-like features");
    }
}