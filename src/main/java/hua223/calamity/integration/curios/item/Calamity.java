package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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

public class Calamity extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    public Calamity(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.source.is(DamageTypeTags.IS_FIRE))
            sulfurFireCurse(listener, listener.player);

        if (listener.isTriggerByLiving &&
            !CalamityCap.isInverted(CalamityCap.CurseType.SILVA, listener.player))
            listener.amplifier += 0.3f;
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!CalamityCap.isInverted(CalamityCap.CurseType.SILVA, listener.player))
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

    @Override
    public void onLogOut(Player player) {
        if (!player.isLocalPlayer())
            CalamityCap.setCalamity(player, false);
    }

    //Global logic, triggered only when others are injured
    @SuppressWarnings("ConstantConditions")
    public static void sunkCurse(HurtListener listener) {
        if (CalamityCap.notHasCalamity()) return;
        ServerPlayer player = listener.player;

        if (CalamityCap.isCalamity(player)) {
            if (CalamityCap.isInverted(CalamityCap.CurseType.SUNK, player)) {
                listener.amplifier -= 0.3f;
                return;
            } else if (CalamityCap.getCalamityList().size() == 1) {
                listener.amplifier *= 2f;
                return;
            }
        }

        final float hurt = listener.baseAmount * 0.45f;
        DamageSource source = null;
        UUID safe = listener.player.getUUID();
        PlayerList list = listener.player.getServer().getPlayerList();
        for (UUID id : CalamityCap.getCalamityList()) {
            if (id != safe) {
                ServerPlayer curseTarget = list.getPlayer(id);
                if (!CalamityCap.isInverted(CalamityCap.CurseType.SUNK, curseTarget))
                    curseTarget.hurt(source == null ? source = CalamityDamageSource.source(CalamityDamageTypes.ABYSS, player.level()) : source, hurt);
            }

        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void sulfurFireCurse(HurtListener listener, ServerPlayer player) {
        boolean isOnFire = player.isOnFire();
        if (CalamityCap.isInverted(CalamityCap.CurseType.SULFUR_FIRE, player)) {
            if (isOnFire) {
                float healAmount = player.getRemainingFireTicks() / 20.0F;
                player.heal(healAmount);
                player.clearFire();
            } else {
                player.heal(listener.baseAmount);
            }
            listener.canceledEvent();
        } else {
            listener.setSource(CalamityDamageSource.source(CalamityDamageTypes.SULFUR_FIRE, listener.player.level()));
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

    private void abyssAttackCurse(ServerPlayer player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        boolean isBeneficial = CalamityCap.isInverted(CalamityCap.CurseType.ABYSS, player);
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
        CalamityCap.setCalamity(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityCap.setCalamity(player, false);
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
                CalamityCap.CurseType.valueOf(type).reversed = true;
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
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 10) {
            var memory = getMemory(player);
            memory.count[0] = 0;
            abyssCurse(player, player.getAttribute(Attributes.ARMOR), memory.uuids[0], isPlayerInDark(player),
                CalamityCap.isInverted(CalamityCap.CurseType.ABYSS, player));
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
            tooltips.add(CMLangUtil.getTranslatable(CalamityCap.CurseType.SUNK.reversed
                ? "sunk_inverted" : "calamity_sunk").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CalamityCap.CurseType.SULFUR_FIRE.reversed
                ? "fire_inverted" : "calamity_fire").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CalamityCap.CurseType.SILVA.reversed
                ? "silva_inverted" : "calamity_silva").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CalamityCap.CurseType.ABYSS.reversed
                ? "abyss_inverted" : "calamity_abyss").setStyle(style));

            tooltips.add(CMLangUtil.getTranslatable(CalamityCap.CurseType.DESERT.reversed
                ? "desert_inverted" : "calamity_desert").setStyle(style));
        } else CMLangUtil.batchColorTexts(tooltips, ChatFormatting.DARK_RED, "calamity", 1, 2);
        return tooltips;
    }

    @Override
    public int getCountSize() {
        return 1;
    }
}