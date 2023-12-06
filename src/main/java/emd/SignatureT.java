package emd;

public class SignatureT implements Cloneable {
    public int n;
    public FeatureT[] Features;
    public double[] Weights;
    public SignatureT() {}
    public SignatureT(int n, FeatureT[] features, double[] weights) {
        this.n = n;
        Features = features;
        Weights = weights;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        int i;
        SignatureT o = (SignatureT) super.clone();
        o.Features = (FeatureT[]) o.Features.clone();

        for (i = 0; i < n; i++)
            o.Features[i] = (FeatureT) o.Features[i].clone();
        o.Weights = (double[]) o.Weights.clone();

        return o;
    }
}
