package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.entity.projectiles.FireMeteor;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

import static hua223.calamity.register.effects.CalamityEffects.DRAGON_BURN;

public class YharimGift extends BaseCurio {
    public YharimGift(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            ServerPlayer player = listener.player;
            player.invulnerableTime *= 2;

            List<MobEffect> effects = player.getActiveEffectsMap().keySet().stream()
                .filter(effect -> !effect.isBeneficial()).toList();
            int count = effects.size();
            if (count > 2)
                for (int i = 0; i < count - 1; i++)
                    player.removeEffect(effects.get(i));

            LivingEntity attacker = listener.entity;
            player.level.addFreshEntity(FireMeteor.of(attacker.level, attacker, player));

            MobEffect effect = DRAGON_BURN.get();
            MobEffectInstance instance = attacker.getEffect(effect);
            int odds = attacker.getRandom().nextInt(0, 10);
            if (odds < 4) return;
            if (instance != null) {
                int level = instance.getAmplifier();
                if (level > 7) {
                    attacker.hurt(new DamageSource("dragonBurn_explosion").setMagic()
                        .bypassMagic().setIsFire(), 50f);
                    attacker.removeEffect(effect);
                } else {
                    attacker.addEffect(new MobEffectInstance(effect, 200, level + 1));
                }
                return;
            }
            attacker.addEffect(new MobEffectInstance(effect, 200, 0));
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "speed_bonus", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        double amplifier = equipped.getArmorValue() * 0.1 + 15;
        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "armor_bonus", amplifier, AttributeModifier.Operation.ADDITION));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style gold = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getTranslatable("yharim_gift", 1).setStyle(gold));
        tooltips.add(CMLangUtil.getTranslatable("yharim_gift", 2).setStyle(gold));
        tooltips.add(CMLangUtil.getTranslatable("yharim_gift", 3).setStyle(gold));
        return tooltips;
    }
}
