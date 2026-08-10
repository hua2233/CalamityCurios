package hua223.calamity.render;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

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

    public static float piecewiseAnimation(float progress, CurveSegment... segments) {
        if (segments.length == 0) return 0f;

        //If for whatever reason you try to not play by the rules, get fucked
        if (segments[0].startingX != 0) segments[0].startingX = 0;

        //Clamp the progress
        progress = Mth.clamp(progress, 0f, 1f);
        float ratio = 0f;

        for (int i = 0; i <= segments.length - 1; i++) {
            CurveSegment segment = segments[i];
            float startPoint = segment.startingX;
            float endPoint = 1f;

            //Too early. This should never get reached,
            //Since by the time you'd have gotten there you'd have found the appropriate segment and broken out of the for loop
            if (progress < segment.startingX) continue;

            if (i < segments.length - 1) {
                //Too late
                if (segments[i + 1].startingX <= progress) continue;
                endPoint = segments[i + 1].startingX;
            }

            float segmentLength = endPoint - startPoint;
            float segmentProgress = (progress - segment.startingX) / segmentLength; //How far along the specific segment
            ratio = segment.startingHeight;

            //Failsafe because somehow it can fail? what
            ratio += Objects.requireNonNullElse(segment.easing, CurveSegment.EasingType.LINEAR)
                .easingFunction(segmentProgress, segment.degree) * segment.elevationShift;

            break;
        }
        return ratio;
    }

    // The height of the segment after the elevation shift is taken into account.
    public float getEndingHeight() {
        return startingHeight + elevationShift;
    }

    @OnlyIn(Dist.CLIENT)
    public enum EasingType {
        POLY_OUT {
            @Override
            protected float easingFunction(float amount, int degree) {
                return 1f - (float) Math.pow(1f - amount, degree);
            }
        },
        LINEAR {
            @Override
            protected float easingFunction(float amount, int degree) {
                return amount;
            }
        };

        protected abstract float easingFunction(float amount, int degree);
    }
}
