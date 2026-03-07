package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SpectralVeil extends BaseCurio implements ICuriosStorage {
    public SpectralVeil(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "SPECTRAL_TELEPORT", true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "SPECTRAL_TELEPORT", false);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        float[] count = getCount(listener.player);
        if (count[0] > 0) {
            count[0] = 0;
            count[1] = 1;
            listener.canceledEvent();
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        float[] count = getCount(listener.player);
        if (count[1] == 1) {
            count[1] = 0;
            listener.probability += 1f;
        } else listener.probability += 0.15f;
    }

    @SuppressWarnings("ConstantConditions")
    public static void teleport(ServerPlayer player) {
        if (!player.hasEffect(CalamityEffects.CHAOS_STATE.get())) {
            float[] count = GlobalCuriosStorage.getCountStorages(player, SpectralVeil.class);
            Vec3 startPos = player.getEyePosition();
            Vec3 endPos = player.getLookAngle().normalize().scale(16).add(startPos);
            BlockHitResult result = player.level.clip(new ClipContext(player.getEyePosition(), endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (result.getType() == HitResult.Type.BLOCK)
                endPos = result.getLocation();

            count[0] = 40;
            player.resetFallDistance();
            player.addEffect(new MobEffectInstance(CalamityEffects.CHAOS_STATE.get(), 300));
            if (player.isPassenger()) player.dismountTo(endPos.x, endPos.y, endPos.z);
            else player.teleportTo(endPos.x, endPos.y, endPos.z);
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0] > 0) count[0]--;
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
        tooltips.add(CMLangUtil.getTranslatable("spectral_veil", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("spectral_veil", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("spectral_veil", 3).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("spectral_veil", 4).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("spectral_veil", 5).withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
