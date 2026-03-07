package hua223.calamity.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CurveSegment {
    // This is the type of easing used in the segment
    public EasingType easing;
    // This indicates when the segment starts on the animation
    public float startingX;
    // This indicates what the starting height of the segment is
    public float startingHeight;
    // This represents the elevation shift that will happen during the segment. Set this to 0 to turn the segment into a flat line.
    // Usually this elevation shift is fully applied at the end of a segment, but the sinebump easing type makes it be reached at the apex of its curve.
    public float elevationShift;
    // This is the degree of the polynomial, if the easing mode chosen is a polynomial one
    public int degree;

    public CurveSegment(EasingType mode, float startX, float startHeight, float elevationShift, int degree) {
        easing = mode;
        startingX = startX;
        startingHeight = startHeight;
        this.elevationShift = elevationShift;
        this.degree = degree;
    }

    // The height of the segment after the elevation shift is taken into account.
    public float getEndingHeight() {
        return startingHeight + elevationShift;
    }

    @OnlyIn(Dist.CLIENT)
    public enum EasingType {
        POLY_OUT {
            @Override
            float easingFunction(float amount, int degree) {
                return 1f - (float) Math.pow(1f - amount, degree);
            }
        },
        LINEAR {
            @Override
            float easingFunction(float amount, int degree) {
                return amount;
            }
        };

        abstract float easingFunction(float amount, int degree);
    }
}
