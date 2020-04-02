package math;

public class Normalization {
    float dataHigh;
    float dataLow;
    float normalizedHigh;
    float normalizedLow;

    public Normalization(float dataHigh, float dataLow, float normalizedHigh, float normalizedLow) {
        this.dataHigh = dataHigh;
        this.dataLow = dataLow;
        this.normalizedHigh = normalizedHigh;
        this.normalizedLow = normalizedLow;
    }

    public float normalize(float x) {
        return ((x - dataLow)
                / (dataHigh - dataLow))
                * (normalizedHigh - normalizedLow) + normalizedLow;
    }

    public double normalize(double x) {
        return ((x - dataLow)
                / (dataHigh - dataLow))
                * (normalizedHigh - normalizedLow) + normalizedLow;
    }
}
