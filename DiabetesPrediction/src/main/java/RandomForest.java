package diabetes;

import java.util.*;

public class RandomForest {
    private List<Tree> trees;
    private int numTrees;
    private List<Record> trainingData;

    public RandomForest(int numTrees) {
        this.numTrees = numTrees;
        this.trees = new ArrayList<>();
    }

    public void train(List<Record> records) {
        this.trainingData = records;
        trees.clear();
        Random rand = new Random();
        for (int i = 0; i < numTrees; i++) {
            List<Record> sample = new ArrayList<>();
            for (int j = 0; j < records.size(); j++) {
                sample.add(records.get(rand.nextInt(records.size())));
            }
            Tree t = new Tree();
            t.build(sample, rand);
            trees.add(t);
        }
    }

    public boolean predict(List<Double> features) {
        int vote = 0;
        for (Tree t : trees) {
            if (t.predict(features)) vote++;
        }
        return vote > trees.size() / 2;
    }

    public double computeAccuracy(List<Record> records) {
        if (records.isEmpty()) return 0.0;
        int correct = 0;
        for (Record record : records) {
            boolean prediction = predict(record.getFeatures());
            if (prediction == record.getLabel()) {
                correct++;
            }
        }
        return (double) correct / records.size() * 100.0;
    }

    public String analyzePrediction(List<Double> input, boolean result, RecordCollector collector) {
        StringBuilder sb = new StringBuilder();
        sb.append("Prediction: ").append(result ? "Diabetic" : "Non-Diabetic").append("\n");

        int vote = 0;
        for (Tree t : trees) {
            if (t.predict(input)) vote++;
        }
        sb.append(vote).append("/").append(trees.size()).append(" trees voted Diabetic\n\n");

        double[] diabeticMeans = new double[8];
        int count = 0;
        for (Record r : trainingData) {
            if (r.getLabel()) {
                for (int i = 0; i < 8; i++) {
                    diabeticMeans[i] += r.getFeatures().get(i);
                }
                count++;
            }
        }
        for (int i = 0; i < 8; i++) {
            diabeticMeans[i] = count > 0 ? diabeticMeans[i] / count : 0.0;
        }

        List<String> names = collector.getFeatureNames();
        List<Double> diffs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            diffs.add(Math.abs(input.get(i) - diabeticMeans[i]));
        }
        List<Integer> topIndices = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double max = -1;
            int index = -1;
            for (int j = 0; j < diffs.size(); j++) {
                if (!topIndices.contains(j) && diffs.get(j) > max) {
                    max = diffs.get(j);
                    index = j;
                }
            }
            if (index != -1) topIndices.add(index);
        }
        sb.append("Top Contributing Features:\n");
        for (int idx : topIndices) {
            sb.append("- ").append(names.get(idx)).append(": Your value = ")
                    .append(String.format("%.2f", input.get(idx)))
                    .append(", Diabetic avg = ")
                    .append(String.format("%.2f", diabeticMeans[idx])).append("\n");
        }
        return sb.toString();
    }
}