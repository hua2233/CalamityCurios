package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = Abaddon.class, isRoot = true)
public class Abaddon extends BaseCurio {
    protected final double value;

    public Abaddon(Properties properties, double value) {
        super(properties);
        this.value = value;
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "abaddon", value, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.BRIMSTONE_FLAMES.get());
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.isCalamityCriticalHits) {
            ItemCooldowns cooldowns = listener.player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            listener.player.calamity$SetInvulnerableTime(2);
            listener.entity.level().explode(null, listener.entity.getX(),
                listener.entity.getY(), listener.entity.getZ(), 2f, Level.ExplosionInteraction.NONE);
            listener.entity.addEffect(new MobEffectInstance(CalamityEffects.BRIMSTONE_FLAMES.get(), 60));
            cooldowns.addCooldown(this, 160);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("abaddon", 1));
        tooltips.add(CMLangUtil.getTranslatable("abaddon", 2));
        return tooltips;
    }
}
