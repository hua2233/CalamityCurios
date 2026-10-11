package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.Bane;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CurioRepel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@CurioRepel(isRoot = true)
@JeiInfo(zh_cn = "小概率掉落于烈焰人，受抢夺影响")
public class Abaddon extends BaseCurio {
    protected byte maximumPropagationNumber = 8;
    protected float criticalStrikeAmplificationFactor = 3f;

    public Abaddon(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        if (listener.isCalamityCriticalHits && !listener.player.getCooldowns().isOnCooldown(this)) {
            listener.player.getCooldowns().addCooldown(this, 100);
            MobEffectInstance instance = listener.entity.getEffect(CalamityEffects.BANE.get());
            if (instance != null) {
               Collection<MobEffectInstance> instances = listener.entity.getActiveEffects();
                if (instances.size() == 1) return;
                instances = new ArrayList<>(instances);

               instances.removeIf(effect -> effect.getAmplifier() > instance.getAmplifier() ||
                   (effect.getAmplifier() == instance.getAmplifier() && effect.getDuration() >= instance.getDuration()));
               if (instances.isEmpty()) return;

               List<Mob> mobs = CalamityHelp.getAttackableEntity(Mob.class, listener.player, listener.entity.getBoundingBox().inflate(5));
               if (mobs.isEmpty()) return;

               for (MobEffectInstance effect : instances)
                   for (int i = 0; i < (Math.min(maximumPropagationNumber, mobs.size())) - 1; i++) {
                       Mob mob = mobs.get(i);
                       mob.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier()), listener.player);
                   }
            } else {
                MobEffectInstance bane = new MobEffectInstance(CalamityEffects.BANE.get(), 300, 1);
                CountFactorEffects.CountFactor factor = bane.calamity$GetUniversalFactor((Bane) CalamityEffects.BANE.get());
                factor.setOwner(listener.player);
                factor.getFactor()[0] = criticalStrikeAmplificationFactor;
                listener.entity.addEffect(bane);
            }
        }
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.BLAZE) && context.lootingChance(.05f))
            context.addLoot(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void displayDpsInformation(List<Component> tooltips, Style style) {
        LocalPlayer player = Minecraft.getInstance().player;
        float hurt = 3f;
        double amplifier = player.getAttributeValue(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get()) - 1;
        if (amplifier > 0) hurt += hurt * (float)  (amplifier * criticalStrikeAmplificationFactor);
        tooltips.add(CMLangUtil.getDynamic("abaddon", 3, hurt).setStyle(style));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getTranslatable("abaddon", 1).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("abaddon", 2).setStyle(style));
        displayDpsInformation(tooltips, style);
        return tooltips;
    }
}
