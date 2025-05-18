package diabetes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tree {
    private Node root;
    private static final int MAX_DEPTH = 10;

    public void build(List<Record> records, Random random) {
        root = buildTree(records, 0, random);
    }

    private Node buildTree(List<Record> records, int depth, Random random) {
        if (records.isEmpty()) {
            return new Node(false);
        }
        if (depth >= MAX_DEPTH || allSameLabel(records)) {
            return new Node(majorityLabel(records));
        }

        int attribute = random.nextInt(8);
        List<Double> values = new ArrayList<>();
        for (Record record : records) {
            values.add(record.getFeatures().get(attribute));
        }
        values.sort(Double::compareTo);
        double threshold = values.get(values.size() / 2);

        List<Record> leftRecords = new ArrayList<>();
        List<Record> rightRecords = new ArrayList<>();
        for (Record record : records) {
            if (record.getFeatures().get(attribute) <= threshold) {
                leftRecords.add(record);
            } else {
                rightRecords.add(record);
            }
        }

        Node node = new Node(attribute, threshold);
        node.left = buildTree(leftRecords, depth + 1, random);
        node.right = buildTree(rightRecords, depth + 1, random);
        return node;
    }

    private boolean allSameLabel(List<Record> records) {
        if (records.isEmpty()) return true;
        boolean label = records.get(0).getLabel();
        return records.stream().allMatch(record -> record.getLabel() == label);
    }

    private boolean majorityLabel(List<Record> records) {
        long positiveCount = records.stream().filter(Record::getLabel).count();
        return positiveCount > records.size() / 2;
    }

    public boolean predict(List<Double> features) {
        if (root == null) {
            return false;
        }
        return root.predict(features);
    }
}
