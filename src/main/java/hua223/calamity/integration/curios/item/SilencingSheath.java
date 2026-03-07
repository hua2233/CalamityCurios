package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(DarkMatterSheath.class)
public class SilencingSheath extends BaseCurio {
    public SilencingSheath(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.player.isCrouching()) {
            boolean canSneakAttack = true;

            if (listener.entity instanceof Mob mob)
                canSneakAttack = mob.getTarget() == null;

            if (canSneakAttack) {
                Vec3 toPlayer = listener.player.getEyePosition()
                    .subtract(listener.entity.getEyePosition());
                Vec3 forward = listener.entity.getLookAngle();

                if (forward.dot(toPlayer.normalize()) < -0.7071)
                    listener.amplifier += 1f;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("silencing_sheath"));
        return tooltips;
    }
}
