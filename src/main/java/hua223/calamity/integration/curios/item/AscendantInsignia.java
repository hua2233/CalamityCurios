package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

@ConflictChain(Wings.class)
public class AscendantInsignia extends Wings implements IKeyDataPackResponse {
    public AscendantInsignia(Properties properties) {
        super(properties, 300, 1.75f, 1);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "ascendant_insignia", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onServerResponse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(player, player.blockPosition(), CalamitySounds.ASCENDANT_ACTIVATE.get(), SoundSource.AMBIENT);
        float[] count = getMemory(player).count;

        count[4] = 1;
        player.getCooldowns().addCooldown(this, 800);
        DelayRunnable.addRunTask(160, () -> {
            count[4] = 0;
            level.playSound(player, player.blockPosition(), CalamitySounds.ASCENDANT_OFF.get(), SoundSource.AMBIENT);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_Y;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean accept(Minecraft minecraft) {
        return notInCooling(minecraft);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        float[] count = getCount(player);

        if (abilities.flying) {
            if (count[4] != 1 && count[0]-- <= 0) {
                count[1] = 0;
                abilities.setFlyingSpeed(0.05f);
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        } else if (count[1] == 0 && player.onGround()) {
            count[1] = 1;
            count[0] = getFlyTime(player);
            abilities.setFlyingSpeed(0.05f * flySpeedAmplifier);
            abilities.mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public int getCountSize() {
        return 5;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.AQUA, "ascendant_insignia", 1, 2, 3);
        super.getSlotsTooltip(tooltips, stack);
        return tooltips;
    }
}
