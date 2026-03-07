package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.FriendlyEffectCloudBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class CorrosiveSpine extends BaseCurio {
    public CorrosiveSpine(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "corrosive_spine", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "corrosive_spine", 4, AttributeModifier.Operation.ADDITION));
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.entity.hasEffect(MobEffects.POISON) && listener.player.getRandom().nextBoolean())
            listener.entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving && !listener.player.getCooldowns().isOnCooldown(this)) {
            new FriendlyEffectCloudBuilder(listener.player, listener.player.position(),
                100, 2f).setPotion(Potions.POISON).build();
            listener.player.getCooldowns().addCooldown(this, 600);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("corrosive_spine", 1));
        tooltips.add(CMLangUtil.getTranslatable("corrosive_spine", 2));
        return tooltips;
    }
}
