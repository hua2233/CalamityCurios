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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = PrecisionGlove.class, isRoot = true)
public class PrecisionGlove extends BaseCurio {
    public PrecisionGlove(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(uuid, "precision_glove", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.applyAmplifier(0.1f);
        listener.probability += 0.15f;
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += 0.15f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("precision_glove"));
        return tooltips;
    }
}
