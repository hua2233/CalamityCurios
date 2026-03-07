package hua223.calamity.util.primitive;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CircleBuffer;
import hua223.calamity.util.RenderUtil;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PrimitiveRenderer {
    private static final short MAX_POSITIONS = 400;
    private static final Vec3[] MAIN_POSITIONS = new Vec3[MAX_POSITIONS];
    private static final short[] MAIN_INDICES = new short[8192];
    private static short positionsIndex;
    private static short verticesIndex;
    private static short indicesIndex;

    /**
     * @param oldPositions   渲染物体所经过的轨迹，用于绘制拖尾
     * @param settings       要使用的基本元绘制设置
     * @param pointsToCreate 采样数量，细节越多，性能越差。默认情况下，是与轨迹长度相等
     */
    public static void renderTrail(CircleBuffer<Vec3> oldPositions, PrimitiveSettings settings, int pointsToCreate,
                                   PoseStack pos) {

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

        privateRender(pos, settings);
    }

    private static boolean assignPointsRectangleTrail(CircleBuffer<Vec3> positions, PrimitiveSettings settings, int samplingPoint) {
        // Don't smoothen the points unless explicitly told do so.
        if (!settings.smoothen) {
            positionsIndex = 0;

            // delete uninitialized points
            List<Vec3> controlPoints = positions.toList();
            int size = controlPoints.size();
            if (size <= 2) return false;

            float endPos = samplingPoint - 1f;
            int size1 = size - 1;
            for (int i = 0; i < samplingPoint; i++) {
                float completionRatio = i / endPos;
                int currentIndex = (int) (completionRatio * size);

                Vec3 currentPoint = controlPoints.get(currentIndex);
                Vec3 nextPoint = controlPoints.get(++currentIndex % size);

                // offset function needs to apply even in cases where smoothing is off.
                Vec3 finalPos = currentPoint.lerp(nextPoint, completionRatio * size1 % 0.99999f);

                MAIN_POSITIONS[positionsIndex] = finalPos;
                positionsIndex++;
            }

            return true;
        }

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

            Vec3 farLeft;
            Vec3 left = positions.get(localSplineIndex);
            Vec3 right = positions.get(localSplineIndex + 1);
            Vec3 farRight;

            // Special case: If the spline attempts to access the previous/next index but the index is already at the very beginning/end, simply
            // cheat a little bit by creating a phantom point that's mirrored from the previous one.
            if (localSplineIndex == 0) farLeft = RenderUtil.subtractVec2(right, left.scale(2f));
            else farLeft = positions.get(localSplineIndex - 1);

            if (localSplineIndex >= newSizeLess) farRight = RenderUtil.subtractVec2(left, right.scale(2f));
            else farRight = positions.get(localSplineIndex + 2);

            MAIN_POSITIONS[positionsIndex] = RenderUtil.catmullRomVec(farLeft, left, right, farRight, localSplineInterpolant, false);
            positionsIndex++;
        }

        // Manually insert the front and end points.
        MAIN_POSITIONS[0] = positions.getHead();
        MAIN_POSITIONS[positionsIndex] = positions.getLast();
        positionsIndex++;
        return true;
    }

    //In Terraria, this is drawn using the triangular list type, and to avoid unexpected problems, triangles are also used instead of quadrilaterals
    private static void assignVerticesRectangleTrail(PrimitiveSettings settings) {
        verticesIndex = 0;
        float maxIndexLess = positionsIndex - 1;
        for (int i = 0; i < positionsIndex; i++) {
            float completionRatio = i == 0 ? i : (i - 1f) / maxIndexLess;
            float widthAtVertex = settings.vertexWidthFunction.apply(completionRatio);
            //This is the same color container object and should only write this color information without modifying it
            Vector4f vertexColor = settings.vertexColorFunction.apply(completionRatio);
            Vec3 currentPosition = MAIN_POSITIONS[i];

            Vec3 directionToAhead = i == positionsIndex - 1 ? vec3Normalized(RenderUtil.subtractVec2(MAIN_POSITIONS[i - 1], MAIN_POSITIONS[i])) :
                vec3Normalized(RenderUtil.subtractVec2(MAIN_POSITIONS[i], MAIN_POSITIONS[i + 1]));

            //Revert the vertex width that was originally in Terraria, as this acts on shader sampling
            float rawWidth = widthAtVertex * settings.widthCorrectionRatio;
            Vec2 leftCurrentTextureCoord = new Vec2(completionRatio, 0.5f - rawWidth * 0.5f);
            Vec2 rightCurrentTextureCoord = new Vec2(completionRatio, 0.5f + rawWidth * 0.5f);

            // Point 90 degrees away from the direction towards the next point, and use it to mark the edges of the rectangle.
            // This doesn't use RotatedBy for the sake of performance (there can potentially be a lot of trail points).
            Vec3 sideDirection = new Vec3(-directionToAhead.y, directionToAhead.x, directionToAhead.z);

            Vec3 v = sideDirection.scale(widthAtVertex);
            Vec3 left = RenderUtil.subtractVec2(v, currentPosition);
            Vec3 right = currentPosition.add(v.x, v.y, 0);

            // What this is doing, at its core, is defining a rectangle based on two triangles.
            // These triangles are defined based on the width of the strip at that point.
            // The resulting rectangles combined are what make the trail itself.
            settings.addArgument(new Vec3(left.x, left.y, left.z), vertexColor, leftCurrentTextureCoord, rawWidth);
            verticesIndex++;
            settings.addArgument(new Vec3(right.x, right.y, right.z), vertexColor, rightCurrentTextureCoord, rawWidth);
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

    private static void privateRender(PoseStack pos, PrimitiveSettings settings) {
        if (indicesIndex % 6 != 0 || verticesIndex <= 3) return;
        VertexConsumer consumer = settings.getConsumer();

        Matrix4f matrix4f = pos.last().pose();

        for (int i = 0; i < indicesIndex; i += 3) {
            addVertex(consumer, matrix4f, settings.wrappersBuffer[MAIN_INDICES[i]]);
            addVertex(consumer, matrix4f, settings.wrappersBuffer[MAIN_INDICES[i + 1]]);
            addVertex(consumer, matrix4f, settings.wrappersBuffer[MAIN_INDICES[i + 2]]);
        }
    }

    //this handles the z axis
    public static void renderVec3Trail(CircleBuffer<Vec3> oldPositions, PrimitiveSettings settings, TrailOrientation orientation, int pointsToCreate, PoseStack pos) {
        int count = oldPositions.getCount();
        if (count <= 2 || count >= MAX_POSITIONS) return;
        settings.initVertexArgumentBuffer(oldPositions.size);

        if (!assignVec3PointsRectangleTrail(oldPositions, pointsToCreate)) return;

        if (positionsIndex <= 2) return;

        assignVec3VerticesRectangleTrail(settings, orientation);
        assignIndicesRectangleTrail();

        privateRender(pos, settings);
    }

    private static boolean assignVec3PointsRectangleTrail(CircleBuffer<Vec3> positions, int samplingPoint) {
        positionsIndex = 1;

        if (positions.getCount() <= 4) return false;
        float fPoint = (float) samplingPoint;

        float newMaxSize = positions.getCount() - 1;
        float newSizeLess = newMaxSize - 1f;
        for (int j = 0; j < samplingPoint; j++) {
            float splineInterpolant = j / fPoint;
            float localSplineInterpolant = splineInterpolant * newMaxSize % 1f;
            int localSplineIndex = (int) (splineInterpolant * newMaxSize);

            Vec3 farLeft;
            Vec3 left = positions.get(localSplineIndex);
            Vec3 right = positions.get(localSplineIndex + 1);
            Vec3 farRight;

            if (localSplineIndex == 0) {
                Vec3 direction = left.subtract(right);
                farLeft = direction.scale(2f).subtract(left);
            } else {
                farLeft = positions.get(localSplineIndex - 1);
            }

            if (localSplineIndex >= newSizeLess) {
                Vec3 direction = right.subtract(left);
                farRight = direction.scale(2f).subtract(right);
            } else {
                farRight = positions.get(localSplineIndex + 2);
            }

            MAIN_POSITIONS[positionsIndex] = RenderUtil.catmullRomVec(farLeft, left, right, farRight, localSplineInterpolant, true);
            positionsIndex++;
        }

        MAIN_POSITIONS[0] = positions.get(0);
        MAIN_POSITIONS[positionsIndex] = positions.get((int) newMaxSize);
        positionsIndex++;
        return true;
    }

    //Default vertical draw tailing on Y-axis trail (tsk, damn math)
    private static void assignVec3VerticesRectangleTrail(PrimitiveSettings settings, TrailOrientation orientation) {
        verticesIndex = 0;
        float maxIndexLess = positionsIndex - 1;

        for (int i = 0; i < positionsIndex; i++) {
            float completionRatio = i == 0 ? i : (i - 1f) / maxIndexLess;
            float widthAtVertex = settings.vertexWidthFunction.apply(completionRatio);
            Vector4f vertexColor = settings.vertexColorFunction.apply(completionRatio);
            Vec3 currentPosition = MAIN_POSITIONS[i];

            float rawWidth = widthAtVertex * settings.widthCorrectionRatio;
            Vec2 leftCurrentTextureCoord = new Vec2(completionRatio, 0.5f - rawWidth * 0.5f);
            Vec2 rightCurrentTextureCoord = new Vec2(completionRatio, 0.5f + rawWidth * 0.5f);

            Vec3 sideDir = orientation.getDir(i == maxIndexLess ? MAIN_POSITIONS[i].subtract(MAIN_POSITIONS[i - 1]).normalize() :
                MAIN_POSITIONS[i + 1].subtract(MAIN_POSITIONS[i]).normalize());

            Vec3 vertexPos = sideDir.scale(widthAtVertex);
            Vec3 left = currentPosition.subtract(vertexPos);
            Vec3 right = currentPosition.add(vertexPos);

            settings.addArgument(new Vec3(left.x, left.y, left.z), vertexColor, leftCurrentTextureCoord, rawWidth);
            verticesIndex++;
            settings.addArgument(new Vec3(right.x, right.y, right.z), vertexColor, rightCurrentTextureCoord, rawWidth);
            verticesIndex++;
        }
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix4f, VertexArgumentWrapper vertex) {
        consumer.vertex(matrix4f, (float) vertex.position.x, (float) vertex.position.y, (float) vertex.position.z)
            .color(vertex.r, vertex.g, vertex.b, vertex.a)
            .uv(vertex.uv.x, vertex.uv.y)
            .uv2(vertex.w)
            .endVertex();
    }

    private static Vec3 vec3Normalized(Vec3 vec3) {
        Vec2 vec2 = new Vec2((float) vec3.x, (float) vec3.y).normalized();
        return new Vec3(vec2.x, vec2.y, vec3.z);
    }

    public enum TrailOrientation {
        /**
         * 拖尾平面始终包含世界 Y 轴（竖直方向）。
         * - 宽度方向 = 水平侧向（由 forward × worldUp 得到）
         * - 视觉效果：拖尾位于“竖直平面”内
         * - 正对时呈现为水平短线（因看到平面边缘）
         */
        VERTICAL_PLANE {
            @Override
            protected Vec3 getDir(Vec3 directionToAhead) {
                Vec3 cross = directionToAhead.cross(CalamityHelp.UNIT_Y);
                return cross.lengthSqr() > 1e-6f ? cross.normalize() : CalamityHelp.UNIT_X;
            }
        },

        /**
         * 拖尾宽度方向始终沿局部“上方向”（尽量对齐世界 Y）。
         * - 宽度方向 = 局部上向（由 up × horizontalRight 得到）
         * - 视觉效果：正对时呈现为竖直线
         * - 优势：玩家正视时有明确高度感
         */
        SCREEN_ALIGNED_VERTICAL {
            @Override
            protected Vec3 getDir(Vec3 directionToAhead) {
                Vec3 horizontalForward = new Vec3(directionToAhead.x, 0, directionToAhead.z);
                Vec3 right;
                if (horizontalForward.lengthSqr() < 1e-6f) right = CalamityHelp.UNIT_X;
                else right = new Vec3(-horizontalForward.z, 0, horizontalForward.x).normalize();
                return directionToAhead.cross(right).normalize();
            }
        };

        protected abstract Vec3 getDir(Vec3 directionToAhead);
    }
}
