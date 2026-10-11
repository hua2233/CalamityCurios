package hua223.calamity.register.effects;

import hua223.calamity.events.listeners.TrackedListener;
import hua223.calamity.net.IEffectDataResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Freeze extends CalamityEffect implements IEffectsCallBack, IEffectDataResponse {
    protected Freeze(MobEffectCategory category, int color) {
        super(category, color);
        String id = getSelfUUID();
        addAttributeModifier(Attributes.ARMOR, id, 30, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(CalamityAttributes.INJURY_OFFSET.get(), id, 0.3, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, id, -0.9, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(AttributeRegistry.ICE_MAGIC_RESIST.get(), id, 0.9, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) writeFlagAndSendToAllTracking(entity.calamity$Player.Calamity$Player.getPlayer(), true);
        else if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            entity.setTicksFrozen(effect.getDuration());
        }
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity.calamity$IsPlayer) writeFlagAndSendToAllTracking(entity.calamity$Player.Calamity$Player.getPlayer(), false);
        else if (entity instanceof Mob mob) {
            mob.setNoAi(false);
            entity.setTicksFrozen(0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        AbstractClientPlayer player = TrackedListener.getInitiator(tag);
        if (player != null) player.Calamity$Player.freeze = tag.getBoolean("flag");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("freeze").withStyle(ChatFormatting.AQUA));
    }
}
