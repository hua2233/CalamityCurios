package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.screen.ConvergingEnergyRenderer;
import hua223.calamity.render.screen.ErosionScreenRenderer;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

//TODO I should refactor it, maybe the previous one was too perfunctory
public class GruesomeEminence extends BaseCurio implements ICuriosStorage, IKeyDataPackResponse {
    public GruesomeEminence(Properties properties) {
        super(properties);
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
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(this)) return;
        float[] count = getCount(player);
        count[0] = 1;
        cooldowns.addCooldown(this, 500);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        float[] count = getCount(listener.player);
        if (count[0] > 0) listener.amplifier += count[1];
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0] != 0) {
            if (++count[0] > 200) count[0] = 0;
            else count[1] = Mth.lerp(count[0] / 180, .35f, 1.5f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_G;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public boolean accept(Minecraft minecraft) {
        if (notInCooling(minecraft)) {
            new ErosionScreenRenderer(220);
            CalamitySounds.GE_ACTIVATE.playLocalSound();
            return true;
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "gruesome_eminence", 3);
        tooltips.add(CMLangUtil.blankLine());
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.DARK_RED, "gruesome_eminence", 1, 2);
        return tooltips;
    }
}
