package diabetes;

import java.io.*;
import java.util.*;

public class RecordCollector {
    private List<Record> records;
    private final List<String> featureNames;
    private double[] means;
    private double[] stdDevs;

    // Initialize with immutable feature names
    public RecordCollector() {
        this.records = new ArrayList<>();
        this.featureNames = Collections.unmodifiableList(Arrays.asList(
                "Pregnancies", "Glucose", "BloodPressure", "SkinThickness",
                "Insulin", "BMI", "DiabetesPedigreeFunction", "Age"
        ));
        this.means = new double[featureNames.size()];
        this.stdDevs = new double[featureNames.size()];
    }

    public List<Record> getRecords() {
        return new ArrayList<>(records); // Defensive copy to prevent external modification
    }

    public List<String> getFeatureNames() {
        return featureNames; // Already immutable
    }

    public boolean loadFromCSV(String path) {
        records.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int id = 1;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length != 9) {
                    System.err.println("Skipping invalid line (wrong column count): " + line);
                    continue;
                }
                List<Double> features = new ArrayList<>();
                try {
                    for (int i = 0; i < 8; i++) {
                        String token = tokens[i].trim();
                        features.add(token.isEmpty() || "null".equalsIgnoreCase(token) ? Double.NaN : Double.parseDouble(token));
                    }
                    boolean label = Double.parseDouble(tokens[8].trim()) > 0.5;
                    records.add(new Record(id++, features, label));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line with invalid numeric data: " + line);
                }
            }
            if (records.isEmpty()) {
                System.err.println("No valid records found in CSV: " + path);
                return false;
            }
            handleMissingData(records);
            normalizeFeatures(records);
            return true;
        } catch (IOException e) {
            System.err.println("Error loading file " + path + ": " + e.getMessage());
            return false;
        }
    }

    private void handleMissingData(List<Record> records) {
        int featureCount = featureNames.size();
        means = new double[featureCount];
        int[] nonMissingCounts = new int[featureCount];

        // Calculate means
        for (Record r : records) {
            List<Double> features = r.getFeatures();
            for (int i = 0; i < featureCount; i++) {
                if (!Double.isNaN(features.get(i))) {
                    means[i] += features.get(i);
                    nonMissingCounts[i]++;
                }
            }
        }
        for (int i = 0; i < featureCount; i++) {
            means[i] = nonMissingCounts[i] > 0 ? means[i] / nonMissingCounts[i] : 0.0;
        }

        // Replace NaN values with means
        for (int i = 0; i < records.size(); i++) {
            List<Double> features = new ArrayList<>(records.get(i).getFeatures());
            for (int j = 0; j < featureCount; j++) {
                if (Double.isNaN(features.get(j))) {
                    features.set(j, means[j]);
                }
            }
            records.set(i, new Record(records.get(i).getId(), features, records.get(i).getLabel()));
        }
    }

    private void normalizeFeatures(List<Record> records) {
        int featureCount = featureNames.size();
        means = new double[featureCount]; // Recalculate means
        double[] variances = new double[featureCount];

        // Calculate means
        for (Record r : records) {
            List<Double> features = r.getFeatures();
            for (int i = 0; i < featureCount; i++) {
                means[i] += features.get(i);
            }
        }
        for (int i = 0; i < featureCount; i++) {
            means[i] /= records.size();
        }

        // Calculate variances
        for (Record r : records) {
            List<Double> features = r.getFeatures();
            for (int i = 0; i < featureCount; i++) {
                double diff = features.get(i) - means[i];
                variances[i] += diff * diff;
            }
        }
        for (int i = 0; i < featureCount; i++) {
            stdDevs[i] = Math.sqrt(variances[i] / records.size());
            if (stdDevs[i] == 0) stdDevs[i] = 1.0; // Avoid division by zero
        }

        // Apply normalization
        for (int i = 0; i < records.size(); i++) {
            List<Double> features = new ArrayList<>(records.get(i).getFeatures());
            for (int j = 0; j < featureCount; j++) {
                features.set(j, (features.get(j) - means[j]) / stdDevs[j]);
            }
            records.set(i, new Record(records.get(i).getId(), features, records.get(i).getLabel()));
        }
    }

    public List<Double> normalizeInput(List<Double> input) {
        if (input.size() != featureNames.size()) {
            throw new IllegalArgumentException("Input must have " + featureNames.size() + " features");
        }
        if (means == null || stdDevs == null) {
            throw new IllegalStateException("Normalization parameters not initialized. Load a dataset first.");
        }
        List<Double> normalized = new ArrayList<>();
        for (int i = 0; i < featureNames.size(); i++) {
            normalized.add((input.get(i) - means[i]) / stdDevs[i]);
        }
        return normalized;
    }
}