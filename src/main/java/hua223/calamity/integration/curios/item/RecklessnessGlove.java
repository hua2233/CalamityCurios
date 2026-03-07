package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(PrecisionGlove.class)
public class RecklessnessGlove extends BaseCurio {
    public RecklessnessGlove(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(uuid, "recklessness_glove", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.probability -= 0.05f;
        listener.applyAmplifier(-0.1f);
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        Vec3 move = listener.projectile.getDeltaMovement();
        double originalSpeed = move.length();

        if (originalSpeed < 1e-6) return;
        float inaccuracy = (float) Math.min(10.0, 0.5 * originalSpeed);
        listener.projectile.shoot(move.x, move.y, move.z, (float) originalSpeed, inaccuracy);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("recklessness_glove", 1));
        tooltips.add(CMLangUtil.getTranslatable("recklessness_glove", 2));
        return tooltips;
    }
}
