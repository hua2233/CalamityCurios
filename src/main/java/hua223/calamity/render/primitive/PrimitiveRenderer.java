package hua223.calamity.render.primitive;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.util.Vector2f;
import org.joml.Matrix4f;
import hua223.calamity.render.CircleBuffer;
import hua223.calamity.util.RenderUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class PrimitiveRenderer {
    private static final short MAX_POSITIONS = 400;
    private static final Vector2f[] MAIN_POSITIONS = new Vector2f[MAX_POSITIONS];
    private static final short[] MAIN_INDICES = new short[8192];
    private static short positionsIndex;
    private static short verticesIndex;
    private static short indicesIndex;

    /**
     * @param oldPositions   渲染物体所经过的轨迹，用于绘制拖尾
     * @param settings       要使用的基本元绘制设置
     * @param pointsToCreate 采样数量，细节越多，性能越差。默认情况下，是与轨迹长度相等
     */
    public static void renderTrail(CircleBuffer<Vector2f> oldPositions, PrimitiveSettings settings, int pointsToCreate, Matrix4f matrix4f) {
        // Return if not enough or too many to draw anything.
        int count = oldPositions.getCount();
        if (count <= 2 || count >= MAX_POSITIONS) return;
        settings.initVertexArgumentBuffer(oldPositions.size);

        // IF this is false, a correct position trail could not be made and rendering should not continue.
        if (!assignPointsRectangleTrail(oldPositions, settings, pointsToCreate)) return;

        // A trail with only one point or less has nothing to connect to, and therefore, can't make a trail.
        if (positionsIndex <= 2) return;

        assignVerticesRectangleTrail(settings);
        assignIndicesRectangleTrail();

        privateRender(matrix4f, settings);
    }

    private static boolean assignPointsRectangleTrail(CircleBuffer<Vector2f> positions, PrimitiveSettings settings,
                                                      int samplingPoint) {
        // Don't smoothen the points unless explicitly told do so.
        if (!settings.smoothen()) {
            positionsIndex = 0;

            float endPos = samplingPoint - 1f;
            int maxIndex = positions.getCount() - 1;

            for (int i = 0; i < samplingPoint; i++) {
                float completionRatio = i / endPos;
                int currentIndex = (int) (completionRatio * maxIndex);

                Vector2f currentPoint = positions.get(currentIndex);
                Vector2f nextPoint = positions.get(++currentIndex);

                // offset function needs to apply even in cases where smoothing is off.
                Vector2f finalPos = currentPoint.lerp(completionRatio * maxIndex % 0.99999f, nextPoint);
                settings.offset(completionRatio, finalPos);

                MAIN_POSITIONS[positionsIndex] = finalPos;
                positionsIndex++;
            }
        } else {
            // Due to the first point being manually added, points should be added starting at the second position instead of the first.
            positionsIndex = 1;

            // Create the control points for the spline.
            // Don't incorporate points that are zeroed out.
            // They are almost certainly a result of incomplete oldPos arrays.

            // Avoid stupid index errors.
            if (positions.getCount() <= 4) return false;
            float fPoint = (float) samplingPoint;

            float newMaxSize = positions.getCount() - 1;
            float newSizeLess = newMaxSize - 1f;
            for (int j = 0; j < samplingPoint; j++) {
                float splineInterpolant = j / fPoint;
                float localSplineInterpolant = splineInterpolant * newMaxSize % 1f;
                int localSplineIndex = (int) (splineInterpolant * newMaxSize);

                Vector2f farLeft;
                Vector2f left = positions.get(localSplineIndex);
                Vector2f right = positions.get(localSplineIndex + 1);
                Vector2f farRight;

                // Special case: If the spline attempts to access the previous/next index but the index is already at the very beginning/end, simply
                // cheat a little bit by creating a phantom point that's mirrored from the previous one.
                farLeft = localSplineIndex == 0 ? left.clone().mul(2).add(-right.x, -right.y) : positions.get(localSplineIndex - 1).clone();
                farRight = localSplineIndex >= newSizeLess ? right.clone().mul(2f).add(-left.x, -left.y) : positions.get(localSplineIndex + 2).clone();

                MAIN_POSITIONS[positionsIndex] = RenderUtil.catmullRomVec(farLeft, left, right, farRight, localSplineInterpolant);
                positionsIndex++;
            }

            // Manually insert the front and end points.
            MAIN_POSITIONS[0] = positions.getHead().clone();
            MAIN_POSITIONS[positionsIndex] = positions.getLast().clone();
            positionsIndex++;
        }

        return true;
    }

    private static Vector2f relative(Vector2f base, Vector2f point) {
        return new Vector2f(base.x - point.x, base.y = point.y);
    }

    //In Terraria, this is drawn using the triangular list type, and to avoid unexpected problems, triangles are also used instead of quadrilaterals
    private static void assignVerticesRectangleTrail(PrimitiveSettings settings) {
        verticesIndex = 0;
        float maxIndexLess = positionsIndex - 1;
        for (int i = 0; i < positionsIndex; i++) {
            float completionRatio = i == 0 ? i : (i - 1f) / maxIndexLess;
            float widthAtVertex = settings.vertexWidth(completionRatio);
            //This is the same color container object and should only write this color information without modifying it
            Vector4i vertexColor = settings.vertexColor(completionRatio);
            Vector2f currentPosition = MAIN_POSITIONS[i];

            Vector2f directionToAhead = i == positionsIndex - 1 ?
                MAIN_POSITIONS[i].clone().add(-MAIN_POSITIONS[i - 1].x, -MAIN_POSITIONS[i - 1].y) :
                MAIN_POSITIONS[i + 1].clone().add(-MAIN_POSITIONS[i].x, -MAIN_POSITIONS[i].y);

            directionToAhead.safeNormalize(Vector2f.ZERO);

            //Revert the vertex width that was originally in Terraria, as this acts on shader sampling
            float rawWidth = widthAtVertex * settings.widthCorrectionRatio();
            Vector2f leftCurrentTextureCoord = new Vector2f(completionRatio, 0.5f - rawWidth * 0.5f);
            Vector2f rightCurrentTextureCoord = new Vector2f(completionRatio, 0.5f + rawWidth * 0.5f);

            // Point 90 degrees away from the direction towards the next point, and use it to mark the edges of the rectangle.
            // This doesn't use RotatedBy for the sake of performance (there can potentially be a lot of trail points).
            directionToAhead.set(-directionToAhead.y, directionToAhead.x);;
            Vector2f sideDirection = directionToAhead.mul(widthAtVertex);

            Vector2f left = currentPosition.clone().add(-sideDirection.x, -sideDirection.y);
            Vector2f right = sideDirection.add(currentPosition.x, currentPosition.y);

            // What this is doing, at its core, is defining a rectangle based on two triangles.
            // These triangles are defined based on the width of the strip at that point.
            // The resulting rectangles combined are what make the trail itself.
            settings.addArgument(left, vertexColor, leftCurrentTextureCoord, rawWidth);
            verticesIndex++;
            settings.addArgument(right, vertexColor, rightCurrentTextureCoord, rawWidth);
            verticesIndex++;
        }
    }


    //connect them
    private static void assignIndicesRectangleTrail() {
        // What this is doing is basically representing each point on the vertices list as
        // indices. These indices should come together to create a tiny rectangle that acts
        // as a segment on the trail. This is achieved here by splitting the indices (or rather, points)
        // into 2 triangles, which requires 6 points.
        // The logic here basically determines which indices are connected together.
        indicesIndex = 0;
        for (short i = 0; i < positionsIndex - 2; i++) {
            short connectToIndex = (short) (i * 2);
            MAIN_INDICES[indicesIndex] = connectToIndex;
            indicesIndex++;

            MAIN_INDICES[indicesIndex] = (short) (connectToIndex + 1);
            indicesIndex++;

            MAIN_INDICES[indicesIndex] = (short) (connectToIndex + 2);
            indicesIndex++;

            MAIN_INDICES[indicesIndex] = (short) (connectToIndex + 2);
            indicesIndex++;

            MAIN_INDICES[indicesIndex] = (short) (connectToIndex + 1);
            indicesIndex++;

            MAIN_INDICES[indicesIndex] = (short) (connectToIndex + 3);
            indicesIndex++;
        }
    }

    private static void privateRender(Matrix4f matrix4f, PrimitiveSettings settings) {
        if (indicesIndex % 6 != 0 || verticesIndex <= 3) return;
        VertexConsumer consumer = settings.getConsumer();

        for (int i = 0; i < indicesIndex; i += 3) {
            settings.buildVertex(consumer, matrix4f, MAIN_INDICES[i]);
            settings.buildVertex(consumer, matrix4f, MAIN_INDICES[i + 1]);
            settings.buildVertex(consumer, matrix4f, MAIN_INDICES[i + 2]);
        }
    }
}
