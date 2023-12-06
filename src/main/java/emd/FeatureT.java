package emd;

public class FeatureT implements Cloneable {
    public double X;
    public double Y;
    public double Z;
    public FeatureT(double x, double y) {
        this.X = x;
        this.Y = y;
    }
    public FeatureT(double x, double y, double z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        FeatureT o = (FeatureT) super.clone();

        return o;
    }
}
