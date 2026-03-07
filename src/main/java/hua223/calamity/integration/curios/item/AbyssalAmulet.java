package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.ChangedDimensionListener;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class AbyssalAmulet extends BaseCurio implements ICuriosStorage {
    public AbyssalAmulet(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        LivingEntity attacker = listener.entity;
        if (attacker.hasEffect(CalamityEffects.RIPTIDE.get())) return;
        attacker.addEffect(new MobEffectInstance(CalamityEffects.RIPTIDE.get(), 200));
    }

    @ApplyEvent
    public final void onDimensionChange(ChangedDimensionListener listener) {
        AttributeInstance instance = listener.player.getAttribute(Attributes.MAX_HEALTH);
        if (listener.from == Level.NETHER) {
            VariableAttributeModifier.updateModifierInInstance(instance, getFirstUUID(listener.player), 0);
        } else if (listener.to == Level.NETHER) {
            VariableAttributeModifier.updateModifierInInstance(instance, getFirstUUID(listener.player), 0.1);
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level.isClientSide) return;

        double value = 0;
        if (equipped.level.dimension() == Level.NETHER) value = 0.1;
        modifier.put(Attributes.MAX_HEALTH, new VariableAttributeModifier(
            getUUID(equipped)[0] = uuid, "abyssal", value, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(MobEffects.WITHER, CalamityEffects.RIPTIDE.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("abyssal", 1));
        tooltips.add(CMLangUtil.getTranslatable("abyssal", 2));
        return tooltips;
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    public boolean storageCount() {
        return false;
    }
}
