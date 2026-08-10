package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCap;
import hua223.calamity.capability.CalamityCap.CurseType;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.damage.*;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;
import java.util.UUID;

import static hua223.calamity.generators.DamageMapping.*;

public class Calamity extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    @DamageRequester(key = SPUTTERING, msg = "abyss",
        style = ChatFormatting.BLUE, zh_cn = "%s殁亡于深渊之中")
    public static DamageSupplier abyss;

    @DamageRequester(key = MAGIC_FIRE, msg = "sulfurFire",
        style = ChatFormatting.DARK_RED, zh_cn = "%s被硫磺火焚烧殆尽")
    public static DamageSupplier sulfurFire;

    public Calamity(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.source.is(DamageTypeTags.IS_FIRE))
            sulfurFireCurse(listener, listener.player);

        if (listener.isTriggerByLiving &&
            !listener.player.Calamity$Player.calamityCap.isInverted(CurseType.SILVA))
            listener.amplifier += 0.3f;
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.player.Calamity$Player.calamityCap.isInverted(CurseType.SILVA))
            listener.amplifier -= 0.3f;

        abyssAttackCurse(listener.player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(CalamitySounds.SUPREME_CALAMITAS.get(), 1f, 1f);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @LogoutRelease
    public static void onLogOut(ServerPlayer player) {
        player.Calamity$Player.calamityCap.setCursePlayer(false);
    }

    //Global logic, triggered only when others are injured
    @SuppressWarnings("ConstantConditions")
    public static void sunkCurse(HurtListener listener) {
        if (CalamityCap.notHasCalamity()) return;
        ServerPlayer player = listener.player;
        CalamityCap cap = player.Calamity$Player.calamityCap;

        if (cap.isCursePlayer()) {
            if (cap.isInverted(CurseType.SUNK)) {
                listener.amplifier -= 0.3f;
                return;
            } else if (CalamityCap.getCalamityPlayerCount() == 1) {
                listener.amplifier *= 2f;
                return;
            }
        }

        final float hurt = listener.baseAmount * 0.45f;
        List<ServerPlayer> players = cap.getRestCalamity();
        if (players.isEmpty()) return;

        DamageSource source = abyss.get();
        for (ServerPlayer curseTarget : players)
            curseTarget.hurt(source, hurt);
    }

    @SuppressWarnings("ConstantConditions")
    private static void sulfurFireCurse(HurtListener listener, ServerPlayer player) {
        boolean isOnFire = player.isOnFire();
        if (listener.player.Calamity$Player.calamityCap.isInverted(CurseType.SULFUR_FIRE)) {
            if (isOnFire) {
                float healAmount = player.getRemainingFireTicks() / 20.0F;
                player.heal(healAmount);
                player.clearFire();
            } else {
                player.heal(listener.baseAmount);
            }
            listener.canceledEvent();
        } else {
            listener.setSource(sulfurFire.get());
            if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                float amplifier = (player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() / 20f);
                if (isOnFire) {
                    amplifier += (player.getRemainingFireTicks() / 20f);
                    player.clearFire();
                }
                player.removeEffect(MobEffects.FIRE_RESISTANCE);

                amplifier /= 100;
                listener.floating += player.getMaxHealth() * Math.min(amplifier + 0.2f, 0.4f);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void abyssAttackCurse(ServerPlayer player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        boolean isBeneficial = player.Calamity$Player.calamityCap.isInverted(CurseType.ABYSS);
        int count = DelayRunnable.getIterableCount(attackSpeed);

        if (count != -1) {
            int maxCount = isBeneficial ? 10 : 8;
            if (count >= maxCount) return;

            count++;
            double amplifier = isBeneficial ? count * 0.05 : -count * 0.1;
            VariableAttributeModifier modifier = (VariableAttributeModifier) attackSpeed.getModifier(getFirstUUID(player));
            modifier.setBatchValue(amplifier, attackSpeed, speed);

            DelayRunnable.iterativeTask(attackSpeed);
        } else {
            double init = isBeneficial ? 0.05 : -0.1;
            VariableAttributeModifier modifier = (VariableAttributeModifier) attackSpeed.getModifier(getFirstUUID(player));
            modifier.setBatchValue(init, attackSpeed, speed);

            DelayRunnable.addIterativeTask(120, attackSpeed,
                () -> modifier.setBatchValue(0, attackSpeed, speed));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void abyssCurse(Player player, AttributeInstance instance, UUID uuid, boolean isDark, boolean isInverted) {
        VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);
        if (isDark) {
            double amount = isInverted ? 0.5 : -5;
            if (modifier.getAmount() == amount) return;
            modifier.setBatchValue(amount, instance, player.getAttribute(Attributes.ARMOR_TOUGHNESS));
        } else {
            if (modifier.getAmount() == 0) return;
            modifier.setBatchValue(0, instance, player.getAttribute(Attributes.ARMOR_TOUGHNESS));
        }
    }

    public static boolean isPlayerInDark(Player player) {
        Level level = player.level();
        int environmentLight = level.getRawBrightness(player.blockPosition(), level.getSkyDarken());
        return environmentLight < 8;
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level().isClientSide) return;
        getUUID(equipped)[0] = uuid;

        VariableAttributeModifier abyssArmor = new VariableAttributeModifier(uuid, "armor_bonus", 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        modifier.put(Attributes.ARMOR, abyssArmor);
        modifier.put(Attributes.ARMOR_TOUGHNESS, abyssArmor);
        VariableAttributeModifier abyssSpeed = new VariableAttributeModifier(uuid, "armor_bonus", 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        modifier.put(Attributes.ATTACK_SPEED, abyssSpeed);
        modifier.put(Attributes.MOVEMENT_SPEED, abyssSpeed);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.calamityCap.setCursePlayer(true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.calamityCap.setCursePlayer(false);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player)
            return player.isCreative();
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        for (String type : tag.getAllKeys()) {
            try {
                CurseType.valueOf(type).reversed = true;
            } catch (IllegalArgumentException e) {
                CalamityCurios.LOGGER.error("Unable to find the corresponding curse type: {}", type, e);
            }
        }
    }

    @Override
    public @NotNull ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        if (++memory.count[0]> 10) {
            memory.count[0] = 0;
            abyssCurse(player, player.getAttribute(Attributes.ARMOR), memory.uuids[0], isPlayerInDark(player),
                player.Calamity$Player.calamityCap.isInverted(CurseType.ABYSS));
        }
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
            tooltips.add(CMLangUtil.getTranslatable(CurseType.SUNK.reversed
                ? "sunk_inverted" : "calamity_sunk").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CurseType.SULFUR_FIRE.reversed
                ? "fire_inverted" : "calamity_fire").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CurseType.SILVA.reversed
                ? "silva_inverted" : "calamity_silva").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CurseType.ABYSS.reversed
                ? "abyss_inverted" : "calamity_abyss").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CurseType.DESERT.reversed
                ? "desert_inverted" : "calamity_desert").setStyle(style));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("calamity").withStyle(ChatFormatting.DARK_RED));
            tooltips.add(CMLangUtil.blankLine());
            tooltips.add(CMLangUtil.getView().withStyle(ChatFormatting.GOLD));
        }
        return tooltips;
    }

    @Override
    public int getCountSize() {
        return 1;
    }
}