package diabetes;

import java.util.List;

public class Record {
    private int id;
    private List<Double> features;
    private boolean label;

    public Record(int id, List<Double> features, boolean label) {
        if (features.size() != 8) throw new IllegalArgumentException("Record must have exactly 8 features");
        this.id = id;
        this.features = features;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public List<Double> getFeatures() {
        return features;
    }

    public boolean getLabel() {
        return label;
    }
}