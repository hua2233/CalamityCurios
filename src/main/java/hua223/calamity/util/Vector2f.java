package hua223.calamity.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Contract;

import java.util.Random;

public class Vector2f {
    public static final Vector2f ZERO = new Vector2f(0f, 0f);
    public static final Vector2f NUNIT_Y = new Vector2f(0f, -1f);
    public static final Vector2f UNIT_X = new Vector2f(1f, 0f);

    public float x;
    public float y;

    public Vector2f() {}

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(double x, double y) {
        this((float) x, (float) y);
    }

    public static Vector2f toRotationVector2(float f) {
        return new Vector2f((float) Math.cos(f), (float) Math.sin(f));
    }

    public static void toRotationVector2(float f, Vector2f vector2f) {
        vector2f.set((float) Math.cos(f), (float) Math.sin(f));
    }

    public static Vector2f nextVector2Circular(float circleHalfWidth, float circleHalfHeight, RandomSource r) {
        Vector2f v = Vector2f.toRotationVector2(Mth.TWO_PI * r.nextFloat());
        v.set(v.x * circleHalfWidth, v.y * circleHalfHeight);
        return v.mul(r.nextFloat());
    }

    public static Vector2f nextVector2Circular(float circleHalfWidth, float circleHalfHeight, RandomSource r, float min, float max) {
        Vector2f v = Vector2f.toRotationVector2(Mth.TWO_PI * r.nextFloat());
        v.set(v.x * circleHalfWidth, v.y * circleHalfHeight);
        return v.mul(min + r.nextFloat() * (max - min));
    }

    public static Vector2f nextVector2CircularEdge(float circleHalfWidth, float circleHalfHeight, RandomSource source) {
       Vector2f vector2f = new Vector2f();
       vector2f.nextVector2Unit(source);
       vector2f.x *= circleHalfWidth;
       vector2f.y *= circleHalfHeight;
       return vector2f;
    }

    public void nextVector2Unit(RandomSource source){
        float randomValue = source.nextFloat();
        float theta = randomValue * Mth.TWO_PI;
        x = (float)Math.cos(theta);
        y = (float)Math.sin(theta);
    }

    public Vector2f vector2Circular(float circleHalfWidth, float circleHalfHeight, RandomSource r) {
        float f = Mth.TWO_PI * r.nextFloat();
        x = (float) Math.cos(f) * circleHalfWidth;
        y = (float) Math.sin(f) * circleHalfHeight;
        return mul(r.nextFloat());
    }

    @Contract(pure = true)
    public static Vector2f vector2dMultiply(float w1, float i1, float w2, float i2) {
        return new Vector2f(w1 * w2 - i1 * i2, w1 * i2 + w2 * i1);
    }

    public Vector2f rotatedBy(float radians, Vector2f center, boolean self) {
        float num = (float) Math.cos(radians);
        float num2 = (float) Math.sin(radians);

        float vx = x - center.x;
        float vy = y - center.y;

        float x = center.x + (vx * num - vy * num2);
        float y = center.y + (vx * num2 + vy * num);
        if (self) {
            this.x = x;
            this.y = y;
            return this;
        }

        return new Vector2f(x, y);
    }

    public Vector2f rotatedByRandom(RandomSource random, Vector2f center, float maxRadians, boolean self) {
        return rotatedBy((float) (random.nextDouble() * maxRadians - random.nextDouble() * maxRadians), center, self);
    }

    public float toRotation() {
        return (float) Math.atan2(y, x);
    }

    public Vector2f normalize(boolean self) {
        float v = length();
        if (v < 1.0E-4F) return ZERO;
        else if (!self) return new Vector2f(x / v, y / v);

        x /= v;
        y /= v;
        return this;
    }

    @SuppressWarnings("all")
    public Vector2f clone() {
        return new Vector2f(x, y);
    }

    public void safeNormalize(Vector2f vector) {
        if (x == 0 && y == 0) {
            x = vector.x;
            y = vector.y;
            return;
        }

        normalize(true);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2f mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vector2f mul(double scalar) {
        return mul((float) scalar);
    }

    public Vector2f mul(Vector2f scalar) {
        this.x *= scalar.x;
        this.y *= scalar.y;
        return this;
    }

    public Vector2f add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2f add(Vector2f vector2f) {
        return add(vector2f.x, vector2f.y);
    }

    public Vector2f add(double x, double y) {
        return add((float) x, (float) y);
    }

    public Vector2f relative(Vector2f base, boolean clone) {
        if (base != null) return new Vector2f(base.x - x, base.y - y);
        else if (clone) return clone();
        return this;
    };

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public void set(Vector2f vector2) {
        this.x = vector2.x;
        this.y = vector2.y;
    }

//    public static void lerp(Vector2f to, float delta) {
//        x = Mth.lerp(delta, x, to.x);
//        y = Mth.lerp(delta, y, to.y);
//    }

    public void lerp(Vector2f to, float delta) {
        x = Mth.lerp(delta, x, to.x);
        y = Mth.lerp(delta, y, to.y);
    }

    public float angleTo(Vector2f center) {
        float dx = center.x - x;
        float dy = center.y - y;
        return (float) Math.atan2(dy, dx);
    }

    public float distance(Vector2f center) {
        float dx = x - center.x;
        float dy = y - center.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2f lerp(float delta, Vector2f to) {
        return new Vector2f(
            Mth.lerp(delta, x, to.x),
            Mth.lerp(delta, y, to.y));
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
