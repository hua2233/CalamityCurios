package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class GalvanicCorrosion extends CalamityEffect {
    public GalvanicCorrosion(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc414",
            -0.3f, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(AttributeRegistry.LIGHTNING_MAGIC_RESIST.get(), "50cd184c-6dc0-486d-b52b-bb73cb5cc414",
            -0.3f, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 50 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide) {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level);
            bolt.setVisualOnly(true);
            bolt.setDamage(7f);
            bolt.setPos(entity.position());
            entity.level.addFreshEntity(bolt);
            entity.thunderHit((ServerLevel) entity.level, bolt);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("galvanic_corrosion").withStyle(ChatFormatting.AQUA));
    }
}
