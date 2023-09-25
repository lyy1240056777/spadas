package emd;

public class feature_t implements Cloneable {
    public double X;
    public double Y;
    public double Z;
    public feature_t(double x, double y) {
        this.X = x;
        this.Y = y;
    }
    public feature_t(double x, double y, double z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        feature_t o = (feature_t) super.clone();

        return o;
    }
}
