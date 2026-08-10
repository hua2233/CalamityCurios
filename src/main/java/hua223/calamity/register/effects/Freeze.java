package hua223.calamity.register.effects;

import hua223.calamity.net.IEffectDataResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
        addAttributeModifier(Attributes.ARMOR,
            "123e4567-e89b-12d3-a456-426614174005", 30, AttributeModifier.Operation.ADDITION);
        addAttributeModifier(CalamityAttributes.INJURY_OFFSET.get(),
            "123e4567-e89b-12d3-a456-426614174005", 0.3, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
            "123e4567-e89b-12d3-a456-426614174005", -0.9, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(AttributeRegistry.ICE_MAGIC_RESIST.get(),
            "123e4567-e89b-12d3-a456-426614174005", 0.9, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) {
            entity.calamity$Player.Calamity$Player.freeze = true;
            getPack().putBoolean("flag", true);
            sendToClient((ServerPlayer) entity);
        } else if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            entity.setTicksFrozen(effect.getDuration());
        }
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity.calamity$IsPlayer) {
            entity.calamity$Player.Calamity$Player.freeze = false;
            getPack().putBoolean("flag", false);
            sendToClient((ServerPlayer) entity);
        } else if (entity instanceof Mob mob) {
            mob.setNoAi(false);
            entity.setTicksFrozen(0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().freeze = tag.getBoolean("flag");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("freeze").withStyle(ChatFormatting.AQUA));//
    }
}
