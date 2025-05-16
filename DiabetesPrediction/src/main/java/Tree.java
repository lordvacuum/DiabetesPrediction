package diabetes;

import java.util.*;

class Node {
    int attribute;
    double threshold;
    boolean isLeaf;
    boolean label;
    Node left, right;

    Node(int attribute, double threshold) {
        this.attribute = attribute;
        this.threshold = threshold;
        this.isLeaf = false;
    }

    Node(boolean label) {
        this.label = label;
        this.isLeaf = true;
    }

    boolean predict(List<Double> features) {
        if (isLeaf) return label;
        return features.get(attribute) <= threshold ? left.predict(features) : right.predict(features);
    }
}

public class Tree {
    private Node root;

    public void build(List<Record> records, Random rand) {
        int numFeatures = 8;
        int attribute = rand.nextInt(numFeatures);
        List<Double> values = new ArrayList<>();
        for (Record r : records) values.add(r.getFeatures().get(attribute));
        Collections.sort(values);
        double threshold = values.get(values.size() / 2);
        root = new Node(attribute, threshold);
        split(root, records, rand);
    }

    private void split(Node node, List<Record> records, Random rand) {
        List<Record> left = new ArrayList<>(), right = new ArrayList<>();
        for (Record r : records) {
            if (r.getFeatures().get(node.attribute) <= node.threshold) left.add(r);
            else right.add(r);
        }
        if (left.isEmpty() || right.isEmpty()) {
            int count = 0;
            for (Record r : records) if (r.getLabel()) count++;
            node.label = count > records.size() / 2;
            node.isLeaf = true;
            return;
        }
        int numFeatures = 8;
        int attribute = rand.nextInt(numFeatures);
        List<Double> values = new ArrayList<>();
        for (Record r : left) values.add(r.getFeatures().get(attribute));
        Collections.sort(values);
        double leftThreshold = values.isEmpty() ? 0 : values.get(values.size() / 2);
        node.left = new Node(attribute, leftThreshold);
        split(node.left, left, rand);

        values.clear();
        for (Record r : right) values.add(r.getFeatures().get(attribute));
        Collections.sort(values);
        double rightThreshold = values.isEmpty() ? 0 : values.get(values.size() / 2);
        node.right = new Node(attribute, rightThreshold);
        split(node.right, right, rand);
    }

    public boolean predict(List<Double> features) {
        return root.predict(features);
    }
}