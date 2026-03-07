package hua223.calamity.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Contract;

import java.util.Random;

public class Vector2d {
    public static final Vector2d ZERO = new Vector2d(0, 0);
    public static final Vector2d NUNIT_Y = new Vector2d(0, -1);
    public static final Vector2d UNIT_X = new Vector2d(1, 0);

    public double x;
    public double y;

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2d toRotationVector2(float f) {
        return new Vector2d((float) Math.cos(f), (float) Math.sin(f));
    }

    public static Vector2d nextVector2Circular(float circleHalfWidth, float circleHalfHeight, RandomSource r) {
        Vector2d v = Vector2d.toRotationVector2(Mth.TWO_PI * r.nextFloat());
        v.set(v.x * circleHalfWidth, v.y * circleHalfHeight);
        return v.mul(r.nextFloat());
    }

    public static Vector2d nextVector2Circular(float circleHalfWidth, float circleHalfHeight, Random r, float min, float max) {
        Vector2d v = Vector2d.toRotationVector2(Mth.TWO_PI * r.nextFloat());
        v.set(v.x * circleHalfWidth, v.y * circleHalfHeight);
        return v.mul(r.nextFloat(min, max));
    }

    public Vector2d nextVector2Circular(float circleHalfWidth, float circleHalfHeight, Random r) {
        float f = Mth.TWO_PI * r.nextFloat();
        x = (float) Math.cos(f) * circleHalfWidth;
        y = (float) Math.sin(f) * circleHalfHeight;
        return mul(r.nextFloat());
    }

    @Contract(pure = true)
    public static Vector2d vector2dMultiply(double w1, double i1, double w2, double i2) {
        return new Vector2d(w1 * w2 - i1 * i2, w1 * i2 + w2 * i1);
    }

    public Vector2d rotatedBy(double radians, Vector2d center, boolean self) {
        float num = (float) Math.cos(radians);
        float num2 = (float) Math.sin(radians);

        double vx = x - center.x;
        double vy = y - center.y;

        double x = center.x + (vx * num - vy * num2);
        double y = center.y + (vx * num2 + vy * num);
        if (self) {
            this.x = x;
            this.y = y;
            return this;
        }

        return new Vector2d(x, y);
    }

    public Vector2d rotatedByRandom(Random random, Vector2d center, double maxRadians, boolean self) {
        return rotatedBy(random.nextDouble() * maxRadians - random.nextDouble() * maxRadians, center, self);
    }

    public Vector2d rotatedByRandom(RandomSource random, Vector2d center, double maxRadians, boolean self) {
        return rotatedBy(random.nextDouble() * maxRadians - random.nextDouble() * maxRadians, center, self);
    }

    public float toRotation() {
        return (float) Math.atan2(y, x);
    }

    public Vector2d normalize(boolean self) {
        float v = length();
        if (v < 1.0E-4F) return ZERO;
        else if (!self) return new Vector2d(x / v, y / v);

        x /= v;
        y /= v;
        return this;
    }

    public Vector2d safeNormalize(Vector2d vector) {
        if (x == 0 && y == 0) {
            x = vector.x;
            y = vector.y;
            return this;
        }

        return normalize(true);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2d mul(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vector2d add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2d vector2d) {
        this.x = vector2d.x;
        this.y = vector2d.y;
    }

    public void lerp(Vector2d to, float delta) {
        x = Mth.lerp(delta, x, to.x);
        y = Mth.lerp(delta, y, to.y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
