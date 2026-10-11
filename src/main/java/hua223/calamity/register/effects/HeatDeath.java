package hua223.calamity.register.effects;

import hua223.calamity.generators.DamageMapping;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class HeatDeath extends CalamityEffect {
    @DamageRequester(key = DamageMapping.VOID, msg = "heat_death",
        id = "minecraft", style = ChatFormatting.DARK_RED, zh_cn = "%s来到了熵寂的尽头")
    public static DamageSupplier supplier;

    public HeatDeath() {
        super(MobEffectCategory.HARMFUL, 11141120);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 4 == 0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(getDescriptionId()).withStyle(ChatFormatting.DARK_RED);
    }

    @Override
    public void applyServerTick(@NotNull LivingEntity target, int amplifier) {
        AttributeInstance attributeinstance = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeinstance != null) {
            UUID uuid = UUID.fromString(getSelfUUID());
            VariableAttributeModifier modifier = (VariableAttributeModifier) attributeinstance.getModifier(uuid);
            if (modifier != null) modifier.addValue(target.getType().is(Tags.EntityTypes.BOSSES) ? -0.015f : -0.02f, attributeinstance);
        }

        if (target.tickCount % 20 == 0) target.hurt(supplier.get(), 6f + target.getMaxHealth() * 0.025f);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity livingEntity, @NotNull AttributeMap attributeMap, int amplifier) {
        AttributeInstance attributeinstance = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (attributeinstance != null) attributeinstance.addPermanentModifier(new VariableAttributeModifier(
            UUID.fromString(getSelfUUID()), getDescriptionId(), 0, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity livingEntity, @NotNull AttributeMap attributeMap, int amplifier) {
        AttributeInstance attributeinstance = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (attributeinstance != null) attributeinstance.removeModifier(UUID.fromString(getSelfUUID()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("heat_death").withStyle(ChatFormatting.DARK_RED));
    }
}
