package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.Rage;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.render.hud.RageHud;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

@ConflictChain(Community.class)
public class ShatteredCommunity extends BaseCurio implements
    ICuriosStorage, IKeyDataPackResponse, IDataPackResponse {
    public ShatteredCommunity(Properties properties) {
        super(properties);
    }

    //Calculate the trigger line for later events
    @ApplyEvent(1200)
    public final void onAttack(PlayerAttackListener listener) {
        Rage rage = listener.player.Calamity$Player.rage;
        rage.addValue(Math.min(3, listener.baseAmount / 7));
        if (rage.isActive()) {
            listener.amplifier += rage.getLevelBonus();
            rage.addLevelUpProgress((int) listener.getCorrectionValue());
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
        player.Calamity$Player.rage.setEnabled(true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
        player.Calamity$Player.rage.setEnabled(false);
        syncHealth(player);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        player.Calamity$Player.rage.activeRage();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 200)) {
            float maxHealth = player.getMaxHealth();
            if (player.getHealth() < maxHealth)
                player.heal(maxHealth * 0.15f);
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "shattered", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "shattered", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));

        modifier.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR, new AttributeModifier(
            uuid, "shattered", (10 + equipped.getAttributeValue(Attributes.ARMOR) * 0.2), AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_V;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        if (tag.contains("count")) RageHud.setRageCount(tag.getByte("count"));
        if (tag.contains("state")) RageHud.rageEnabled = tag.getBoolean("state");
        if (tag.contains("level")) RageHud.setShatteredLevel(tag.getByte("level"), tag.getInt("upDamage"));
        if (tag.contains("damage")) RageHud.setCurrentDamage(tag.getInt("damage"));
        if (tag.contains("value")) {
            float v = tag.getFloat("value");
            if (v < 0) {
                RageHud.playAnimation();
                v = -v;
            }
            RageHud.setRageProgress(v);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.YELLOW, "shattered", 2, 3, 4, 5);
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getDynamic("shattered", 6,
            RageHud.shatteredLevel).setStyle(style));
        tooltips.add(CMLangUtil.getDynamic("shattered", 7,
            RageHud.levelUpProgress).setStyle(style));
        tooltips.add(CMLangUtil.getDynamic("shattered", 8,
            RageHud.getLevelBonus()).setStyle(style));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("shattered", 1)
            .withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}