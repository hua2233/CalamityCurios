package hua223.calamity.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.render.IPlayerPostRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
//以后可能加入Dark Spark 或其他棱镜
public interface IPrismRender extends IPlayerPostRenderer {
    IClientItemExtensions PRISM_EXTENSIONS = new IClientItemExtensions() {
        private final HumanoidModel.ArmPose PRISM = HumanoidModel.ArmPose.create("PRISM_ACTIVATE", true,
            (model, entity, arm) -> {
                float entityRot = (entity.getXRot() + 8) * Mth.DEG_TO_RAD;
                float armXRot = entityRot - 1.5708f;
                //armXRot += (float) Math.toRadians(-45);
                model.rightArm.xRot = armXRot;
                model.leftArm.xRot = armXRot;

                float armYRot = 5 * Mth.DEG_TO_RAD;//(float) Math.toRadians(5);
                model.rightArm.yRot = -armYRot ;
                model.leftArm.yRot = armYRot;

                float armZRot = 10 * Mth.DEG_TO_RAD;//(float) Math.toRadians(10);
                model.rightArm.zRot = -armZRot ;
                model.leftArm.zRot = armZRot;
            });

        @Override
        public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack
        itemStack) {
            return entityLiving.calamity$IsPlayer && entityLiving.calamity$Player.isUsingItem() ? PRISM : null;
        }
    };

    default Vec3 yRotDir(LivingEntity player) {
        float f = player.getXRot() * Mth.DEG_TO_RAD;
        float f1 = (-player.yBodyRot + 4) * Mth.DEG_TO_RAD;
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

    void updateModelTransform(PoseStack pose, BakedModel model, ItemDisplayContext type);
}
