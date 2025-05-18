package diabetes;

import java.util.List;

public class Node {
    boolean isLeaf;
    boolean label;
    int attribute;
    double threshold;
    Node left;
    Node right;

    public Node(boolean label) {
        this.isLeaf = true;
        this.label = label;
    }

    public Node(int attribute, double threshold) {
        this.isLeaf = false;
        this.attribute = attribute;
        this.threshold = threshold;
    }

    public boolean predict(List<Double> features) {
        if (isLeaf) {
            return label;
        }
        if (features.get(attribute) <= threshold) {
            return left != null ? left.predict(features) : false;
        }
        return right != null ? right.predict(features) : false;
    }
}