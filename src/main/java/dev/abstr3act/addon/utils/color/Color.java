package dev.abstr3act.addon.utils.color;

import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class Color implements ICopyable<Color>, ISerializable<Color> {
    public static final Color WHITE = new Color(java.awt.Color.WHITE);
    public static final Color LIGHT_GRAY = new Color(java.awt.Color.LIGHT_GRAY);
    public static final Color GRAY = new Color(java.awt.Color.GRAY);
    public static final Color DARK_GRAY = new Color(java.awt.Color.DARK_GRAY);
    public static final Color BLACK = new Color(java.awt.Color.BLACK);
    public static final Color RED = new Color(java.awt.Color.RED);
    public static final Color PINK = new Color(java.awt.Color.PINK);
    public static final Color ORANGE = new Color(java.awt.Color.ORANGE);
    public static final Color YELLOW = new Color(java.awt.Color.YELLOW);
    public static final Color GREEN = new Color(java.awt.Color.GREEN);
    public static final Color MAGENTA = new Color(java.awt.Color.MAGENTA);
    public static final Color CYAN = new Color(java.awt.Color.CYAN);
    public static final Color BLUE = new Color(java.awt.Color.BLUE);
    public int r;
    public int g;
    public int b;
    public int a;

    public Color() {
        this(255, 255, 255, 255);
    }

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
        this.validate();
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.validate();
    }

    public Color(float r, float g, float b, float a) {
        this.r = (int) (r * 255.0F);
        this.g = (int) (g * 255.0F);
        this.b = (int) (b * 255.0F);
        this.a = (int) (a * 255.0F);
        this.validate();
    }

    public Color(int packed) {
        this.r = toRGBAR(packed);
        this.g = toRGBAG(packed);
        this.b = toRGBAB(packed);
        this.a = toRGBAA(packed);
    }

    public Color(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public Color(java.awt.Color color) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public Color(Formatting formatting) {
        if (formatting.isColor()) {
            this.r = toRGBAR(formatting.getColorValue());
            this.g = toRGBAG(formatting.getColorValue());
            this.b = toRGBAB(formatting.getColorValue());
            this.a = toRGBAA(formatting.getColorValue());
        } else {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
        }
    }

    public Color(TextColor textColor) {
        this.r = toRGBAR(textColor.getRgb());
        this.g = toRGBAG(textColor.getRgb());
        this.b = toRGBAB(textColor.getRgb());
        this.a = toRGBAA(textColor.getRgb());
    }

    public Color(Style style) {
        TextColor textColor = style.getColor();
        if (textColor == null) {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
        } else {
            this.r = toRGBAR(textColor.getRgb());
            this.g = toRGBAG(textColor.getRgb());
            this.b = toRGBAB(textColor.getRgb());
            this.a = toRGBAA(textColor.getRgb());
        }
    }

    public static int fromRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBAR(int color) {
        return color >> 16 & 0xFF;
    }

    public static int toRGBAG(int color) {
        return color >> 8 & 0xFF;
    }

    public static int toRGBAB(int color) {
        return color & 0xFF;
    }

    public static int toRGBAA(int color) {
        return color >> 24 & 0xFF;
    }

    public static Color fromHsv(double h, double s, double v) {
        if (s <= 0.0) {
            return new Color((int) (v * 255.0), (int) (v * 255.0), (int) (v * 255.0), 255);
        } else {
            double hh = h;
            if (h >= 360.0) {
                hh = 0.0;
            }

            hh /= 60.0;
            int i = (int) hh;
            double ff = hh - i;
            double p = v * (1.0 - s);
            double q = v * (1.0 - s * ff);
            double t = v * (1.0 - s * (1.0 - ff));
            double r;
            double g;
            double b;
            switch (i) {
                case 0:
                    r = v;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = v;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = v;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = v;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = v;
                    break;
                case 5:
                default:
                    r = v;
                    g = p;
                    b = q;
            }

            return new Color((int) (r * 255.0), (int) (g * 255.0), (int) (b * 255.0), 255);
        }
    }

    public Color set(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.validate();
        return this;
    }

    public Color r(int r) {
        this.r = r;
        this.validate();
        return this;
    }

    public Color g(int g) {
        this.g = g;
        this.validate();
        return this;
    }

    public Color b(int b) {
        this.b = b;
        this.validate();
        return this;
    }

    public Color a(int a) {
        this.a = a;
        this.validate();
        return this;
    }

    public Color set(Color value) {
        this.r = value.r;
        this.g = value.g;
        this.b = value.b;
        this.a = value.a;
        this.validate();
        return this;
    }

    public boolean parse(String text) {
        String[] split = text.split(",");
        if (split.length != 3 && split.length != 4) {
            return false;
        } else {
            try {
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                int a = split.length == 4 ? Integer.parseInt(split[3]) : this.a;
                this.r = r;
                this.g = g;
                this.b = b;
                this.a = a;
                return true;
            } catch (NumberFormatException var7) {
                return false;
            }
        }
    }

    public Color copy() {
        return new Color(this.r, this.g, this.b, this.a);
    }

    public SettingColor toSetting() {
        return new SettingColor(this.r, this.g, this.b, this.a);
    }

    public TextColor toTextColor() {
        return TextColor.fromRgb(this.getPacked());
    }

    public Style toStyle() {
        return Style.EMPTY.withColor(this.toTextColor());
    }

    public Style styleWith(Style style) {
        return style.withColor(this.toTextColor());
    }

    public void validate() {
        if (this.r < 0) {
            this.r = 0;
        } else if (this.r > 255) {
            this.r = 255;
        }

        if (this.g < 0) {
            this.g = 0;
        } else if (this.g > 255) {
            this.g = 255;
        }

        if (this.b < 0) {
            this.b = 0;
        } else if (this.b > 255) {
            this.b = 255;
        }

        if (this.a < 0) {
            this.a = 0;
        } else if (this.a > 255) {
            this.a = 255;
        }
    }

    public Vec3d getVec3d() {
        return new Vec3d(this.r / 255.0, this.g / 255.0, this.b / 255.0);
    }

    public Vector3f getVec3f() {
        return new Vector3f(this.r / 255.0F, this.g / 255.0F, this.b / 255.0F);
    }

    public int getPacked() {
        return fromRGBA(this.r, this.g, this.b, this.a);
    }

    public java.awt.Color awt() {
        return new java.awt.Color(this.r, this.g, this.b, this.a);
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("r", this.r);
        tag.putInt("g", this.g);
        tag.putInt("b", this.b);
        tag.putInt("a", this.a);
        return tag;
    }

    public Color fromTag(NbtCompound tag) {
        this.r = tag.getInt("r");
        this.g = tag.getInt("g");
        this.b = tag.getInt("b");
        this.a = tag.getInt("a");
        this.validate();
        return this;
    }

    @Override
    public String toString() {
        return this.r + " " + this.g + " " + this.b + " " + this.a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Color color = (Color) o;
            return this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.r;
        result = 31 * result + this.g;
        result = 31 * result + this.b;
        return 31 * result + this.a;
    }
}
