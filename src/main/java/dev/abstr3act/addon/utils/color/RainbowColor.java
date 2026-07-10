package dev.abstr3act.addon.utils.color;

public class RainbowColor extends Color {
    private static final float[] hsb = new float[3];
    private double speed;

    public double getSpeed() {
        return this.speed;
    }

    public RainbowColor setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public RainbowColor getNext() {
        return this.getNext(1.0);
    }

    public RainbowColor getNext(double delta) {
        if (this.speed > 0.0) {
            java.awt.Color.RGBtoHSB(this.r, this.g, this.b, hsb);
            int c = java.awt.Color.HSBtoRGB(hsb[0] + (float) (this.speed * delta), 1.0F, 1.0F);
            this.r = toRGBAR(c);
            this.g = toRGBAG(c);
            this.b = toRGBAB(c);
        }

        return this;
    }

    public RainbowColor set(RainbowColor color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.speed = color.speed;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || this.getClass() != o.getClass()) {
            return false;
        } else {
            return !super.equals(o) ? false : Double.compare(((RainbowColor) o).speed, this.speed) == 0;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(this.speed);
        return 31 * result + (int) (temp ^ temp >>> 32);
    }
}
