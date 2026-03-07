package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class UrsaSergeant extends BaseCurio implements ICuriosStorage {
    public UrsaSergeant(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.ASTRAL_INFECTION.get());
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "ursa_sergeant", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "ursa_sergeant", 10, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(uuid, "ursa_sergeant", 5, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 10) {
            zeroCount(player, 0);

            float max = player.getMaxHealth();
            float health = player.getHealth();

            if (health < max * .15f) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));

            } else if (health < max * .25f) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

            } else if (health < max * .5f) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @ApplyEvent
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ursa_sergeant", 1));
        tooltips.add(CMLangUtil.getTranslatable("ursa_sergeant", 2));
        return tooltips;
    }
}
