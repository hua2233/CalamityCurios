package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static hua223.calamity.generators.DamageMapping.*;

@CurioRepel(isRoot = true)
public class EclipseMirror extends BaseCurio implements ICuriosStorage {
    @DamageRequester(key = SPUTTERING, msg = "dark_light", zh_cn = "%s被黑暗抹杀了")
    public static DamageSupplier supplier;

    public EclipseMirror() {
        super(RegisterList.CURIOS_EPIC);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "eclipse_mirror", 10, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityPlayer calamity = player.Calamity$Player;
        calamity.changeInvisible(-.8f);
        RuinMedallion.sprintingHit(calamity, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityPlayer calamity = player.Calamity$Player;
        calamity.changeInvisible(.8f);
        RuinMedallion.sprintingHit(calamity, false);
    }

    @ApplyEvent(110)
    public final void onDeath(DeathListener listener) {
        if (listener.canceledPlayerDeathIfNotCooldowns(this, 1f, 3600, 16733695, 11141290, 16755200, 11185196)) {
            listener.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 3));
            listener.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 9));
            listener.player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 4));
            listener.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 2));
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this) &&
            CalamityHelp.isCanDodge(listener.player, listener.getCorrectionValue(), listener.player.getMaxHealth() * 0.05f, -1)) {
            listener.canceledEvent();
            listener.player.calamity$SetInvulnerableTime(60);
            List<Mob> entities = CalamityHelp.getAttackableEntity(Mob.class, listener.player, 7);
            if (!entities.isEmpty()) {
                float damage = listener.baseAmount * 3f;
                CalamitySounds.DARK_LIGHT.playSound(listener.player);
                DamageSource source = supplier.get(listener.player);
                float[] count = getCount(listener.player);
                count[3] = 0;
                if (count[2] < 2) count[2] += (2 - count[2]);

                Iterator<Mob> mobIterator = entities.iterator();
                while (mobIterator.hasNext()) {
                    Mob mob = mobIterator.next();
                    mob.hurt(source, damage);
                    if (mob.isDeadOrDying()) mobIterator.remove();
                }

                if (!entities.isEmpty()) AbyssalMirror.stun(entities, 80);
            }
        }
    }

    @ApplyEvent
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        float[] count = getCount(listener.player);
        listener.applyAmplifier(.2f + count[0]);
        count[0] = 0;
        if (count[2] > 0) {
            count[2]--;
            listener.probability += 1.5f;
        } else listener.probability += listener.player.walkDist == listener.player.walkDistO ? .45f : .2f;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);


        if (player.walkDist == player.walkDistO) {
            count[0] = 0;
            if (++count[1] == 40) {
                player.heal(5);
                count[1] = 0;
            }
        } else {
            if (count[0] < 3f) count[0] += .01f;
            count[1] = 0;
        }

        if (count[2] > 0 && ++count[3] == 300) count[2] = 0;
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "eclipse_mirror", 1, 2, 3, 4, 5, 6);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("eclipse_mirror", 7).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
