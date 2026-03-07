package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.SurvivableEffectInstance;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class HolyMoonlight extends BaseCurio implements ICuriosStorage {
    public HolyMoonlight(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.SPELL_POWER.get(),
            new AttributeModifier(uuid, "holy_moonlight", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifier.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(uuid, "holy_moonlight", 150, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.removeEffect(CalamityEffects.MOONLIGHT_SHIELD.get());
    }

    @ApplyEvent(400)
    public final void onHurt(HurtListener listener) {
        float damage = listener.getCorrectionValue();
        float v = ICuriosStorage.getReducedValue(getCount(listener.player), 1, damage);
        if (v != 0) {
            if (v >= listener.baseAmount) {
                listener.entity.hurt(DamageSource.MAGIC, listener.baseAmount * 0.2f);
                listener.canceledEvent();
            } else {
                for (MobEffect effect : listener.player.getActiveEffectsMap().keySet())
                    if (!effect.isBeneficial()) listener.player.removeEffect(effect);
                listener.floating -= v;
            }
        }
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (getCount(listener.player)[1] > 0) {
            if (listener.player.getHealth() < listener.player.getMaxHealth())
                CalamityCapProvider.safetyRunCalamityMagic(listener.player, expand ->
                    listener.player.heal((float) Mth.lerp(expand.calamity$GetMana() /
                    listener.player.getAttributeValue(AttributeRegistry.MAX_MANA.get()), 0, Math.sqrt(listener.baseAmount) * 2f)));

        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] == 600) {
            count[0] = 0;
            count[1] = (float) (player.getAttributeValue(AttributeRegistry.MAX_MANA.get()) / 2d);
            MobEffect effect = CalamityEffects.MOONLIGHT_SHIELD.get();
            if (!player.hasEffect(effect))
                player.addEffect(new SurvivableEffectInstance(effect, 72000, 0, () -> count[1] > 0));
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style style = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
        tooltips.add(CMLangUtil.getTranslatable("holy_moonlight", 1).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("holy_moonlight", 2).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("holy_moonlight", 3).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("holy_moonlight", 4).setStyle(style));
        return tooltips;
    }
}
