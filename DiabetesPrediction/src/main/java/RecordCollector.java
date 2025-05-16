package diabetes;

import java.io.*;
import java.util.*;

public class RecordCollector {
    private List<Record> records;
    private List<String> featureNames;
    private double[] means;
    private double[] stddevs;

    public RecordCollector() {
        records = new ArrayList<>();
        featureNames = Arrays.asList("Pregnancies", "Glucose", "BloodPressure", "SkinThickness",
                "Insulin", "BMI", "DiabetesPedigreeFunction", "Age");
    }

    public List<Record> getRecords() {
        return records;
    }

    public List<String> getFeatureNames() {
        return featureNames;
    }

    public boolean loadFromCSV(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            records.clear();
            String line;
            int id = 1;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length != 9) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }
                List<Double> features = new ArrayList<>();
                try {
                    for (int i = 0; i < 8; i++) {
                        String token = tokens[i].trim();
                        features.add(token.equals("null") || token.isEmpty() ? Double.NaN : Double.parseDouble(token));
                    }
                    boolean label = Double.parseDouble(tokens[8].trim()) > 0.5;
                    records.add(new Record(id++, features, label));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line with invalid numeric data: " + line);
                }
            }
            if (records.isEmpty()) {
                System.err.println("No valid records found in CSV.");
                return false;
            }
            handleMissingData();
            normalizeFeatures();
            return true;
        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
            return false;
        }
    }

    private void handleMissingData() {
        int size = 8;
        means = new double[size];
        int[] counts = new int[size];
        for (Record r : records) {
            List<Double> features = r.getFeatures();
            for (int i = 0; i < size; i++) {
                if (!Double.isNaN(features.get(i))) {
                    means[i] += features.get(i);
                    counts[i]++;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            means[i] = counts[i] > 0 ? means[i] / counts[i] : 0;
        }
        for (int i = 0; i < records.size(); i++) {
            List<Double> features = new ArrayList<>(records.get(i).getFeatures());
            for (int j = 0; j < size; j++) {
                if (Double.isNaN(features.get(j))) features.set(j, means[j]);
            }
            records.set(i, new Record(records.get(i).getId(), features, records.get(i).getLabel()));
        }
    }

    private void normalizeFeatures() {
        int size = 8;
        stddevs = new double[size];
        double[] sums = new double[size];
        for (Record r : records) {
            List<Double> features = r.getFeatures();
            for (int i = 0; i < size; i++) {
                sums[i] += features.get(i);
            }
        }
        for (int i = 0; i < size; i++) {
            means[i] = sums[i] / records.size();
        }
        for (Record r : records) {
            List<Double> features = r.getFeatures();
            for (int i = 0; i < size; i++) {
                stddevs[i] += Math.pow(features.get(i) - means[i], 2);
            }
        }
        for (int i = 0; i < size; i++) {
            stddevs[i] = Math.sqrt(stddevs[i] / records.size());
            if (stddevs[i] == 0) stddevs[i] = 1;
        }

        for (int i = 0; i < records.size(); i++) {
            List<Double> features = new ArrayList<>(records.get(i).getFeatures());
            for (int j = 0; j < size; j++) {
                features.set(j, (features.get(j) - means[j]) / stddevs[j]);
            }
            records.set(i, new Record(records.get(i).getId(), features, records.get(i).getLabel()));
        }
    }

    public List<Double> normalizeInput(List<Double> input) {
        if (input.size() != 8) throw new IllegalArgumentException("Input must be 8 features");
        if (means == null || stddevs == null) {
            throw new IllegalStateException("Normalization parameters not initialized. Load a dataset first.");
        }
        List<Double> norm = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            norm.add((input.get(i) - means[i]) / stddevs[i]);
        }
        return norm;
    }
}