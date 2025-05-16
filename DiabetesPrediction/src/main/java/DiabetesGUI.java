package diabetes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

public class DiabetesGUI {
    private RecordCollector collector = new RecordCollector();
    private RandomForest forest = new RandomForest(100);
    private boolean trained = false;
    private JTextArea resultArea;
    private int helpClicks = 0;
    private List<JTextField> inputFields = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DiabetesGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Diabetes Prediction System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700, 600));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        frame.add(panel);

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        String[] labels = {"Pregnancies", "Glucose", "BloodPressure", "SkinThickness",
                "Insulin", "BMI", "DiabetesPedigreeFunction", "Age"};

        for (String label : labels) {
            inputPanel.add(new JLabel(label));
            JTextField field = new JTextField();
            field.setName(label);
            inputFields.add(field);
            inputPanel.add(field);
        }

        resultArea = new JTextArea(10, 50);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton predictBtn = new JButton("Predict");
        JButton uploadBtn = new JButton("Upload CSV");
        JButton helpBtn = new JButton("Help");
        JButton clearBtn = new JButton("Clear");
        buttonPanel.add(predictBtn);
        buttonPanel.add(uploadBtn);
        buttonPanel.add(helpBtn);
        buttonPanel.add(clearBtn);

        predictBtn.addActionListener((ActionEvent e) -> {
            if (!trained) {
                resultArea.setText("Error: Model not trained. Please upload a CSV file first.");
                return;
            }
            try {
                List<Double> features = new ArrayList<>();
                double[] minValues = {0, 0, 0, 0, 0, 10, 0, 0};
                double[] maxValues = {20, 200, 200, 100, 1000, 50, 2.5, 120};
                for (int i = 0; i < inputFields.size(); i++) {
                    JTextField field = inputFields.get(i);
                    double value = Double.parseDouble(field.getText().trim());
                    if (value < minValues[i] || value > maxValues[i]) {
                        resultArea.setText("Error: " + labels[i] + " out of range (" + minValues[i] + " to " + maxValues[i] + ").");
                        return;
                    }
                    features.add(value);
                }
                List<Double> norm = collector.normalizeInput(features);
                long start = System.currentTimeMillis();
                boolean prediction = forest.predict(norm);
                long end = System.currentTimeMillis();
                String result = "Prediction Time: " + (end - start) + " ms\n" +
                        forest.analyzePrediction(norm, prediction, collector);
                resultArea.setText(result);
            } catch (NumberFormatException ex) {
                resultArea.setText("Error: Invalid input. Please enter numeric values.");
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        });

        uploadBtn.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                resultArea.setText("Loading and training model, please wait...");
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            if (collector.loadFromCSV(file.getAbsolutePath())) {
                                forest.train(collector.getRecords());
                                trained = true;
                                double accuracy = forest.computeAccuracy(collector.getRecords());
                                resultArea.setText("Dataset loaded and model trained.\nAccuracy: " + String.format("%.2f", accuracy) + "%");
                            } else {
                                resultArea.setText("Failed to load dataset.");
                            }
                        } catch (Exception ex) {
                            resultArea.setText("Error loading or training model: " + ex.getMessage());
                        }
                        return null;
                    }
                };
                worker.execute();
            }
        });

        helpBtn.addActionListener((ActionEvent e) -> {
            helpClicks++;
            JOptionPane.showMessageDialog(frame,
                    "- Enter 8 health features within valid ranges.\n- Click 'Predict' to get result.\n- Use 'Upload CSV' to train new data.\n\nHelp clicked: " + helpClicks + " times.",
                    "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        clearBtn.addActionListener((ActionEvent e) -> {
            for (JTextField field : inputFields) {
                field.setText("");
            }
            resultArea.setText("");
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
}