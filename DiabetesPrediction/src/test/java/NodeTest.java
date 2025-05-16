package diabetes;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @Test
    void test_predict_normal_leafNode() {
        // Scenario: Leaf node prediction
        Node node = new Node(true);
        assertTrue(node.predict(Arrays.asList(90.0, 30.0, 45.0)), "Leaf node should return its label (true)");
    }

    @Test
    void test_predict_normal_nonLeaf() {
        // Scenario: Non-leaf node with children
        Node node = new Node(1, 25.0);
        node.left = new Node(false);
        node.right = new Node(true);
        List<Double> features = Arrays.asList(90.0, 30.0, 45.0);
        assertTrue(node.predict(features), "Should return true (30.0 > 25.0, right child)");
    }

    @Test
    void test_predict_edge_threshold() {
        // Scenario: Feature value equals threshold (boundary case)
        Node node = new Node(1, 25.0);
        node.left = new Node(false);
        node.right = new Node(true);
        List<Double> features = Arrays.asList(90.0, 25.0, 45.0);
        assertFalse(node.predict(features), "Should return false (25.0 <= 25.0, left child)");
    }
}