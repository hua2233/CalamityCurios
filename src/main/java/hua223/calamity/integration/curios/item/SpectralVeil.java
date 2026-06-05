package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SpectralVeil extends BaseCurio implements ICuriosStorage, IKeyDataPackResponse {
    public SpectralVeil(Properties properties) {
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
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        float[] count = getCount(listener.player);
        if (count[1] == 1) {
            count[1] = 0;
            listener.probability += 1f;
        } else listener.probability += 0.15f;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return super.initCapabilities(stack, nbt);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        float[] count = getMemory(player).count;
        Vec3 startPos = player.getEyePosition();
        ServerLevel level = player.serverLevel();
        Vec3 endPos = player.getLookAngle().normalize().scale(16).add(startPos);
        BlockHitResult result = level.clip(new ClipContext(player.getEyePosition(), endPos,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (result.getType() == HitResult.Type.BLOCK)
            endPos = result.getLocation();

        count[0] = 40;
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(CalamityEffects.CHAOS_STATE.get(), 300));

        if (player.isPassenger()) player.dismountTo(endPos.x, endPos.y, endPos.z);
        else player.teleportTo(endPos.x, endPos.y, endPos.z);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public boolean accept(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (!player.hasEffect(CalamityEffects.CHAOS_STATE.get())) {
            RandomSource random = player.getRandom();
            ParticleEngine engine = minecraft.particleEngine;

            float r = FastColor.ARGB32.red(11141290) / 255f;
            float g = FastColor.ARGB32.green(11141290) / 255f;
            float b = FastColor.ARGB32.blue(11141290) / 255f;
            for(int i = 0; i < 32; ++i) {
                Particle particle = engine.makeParticle(ParticleTypes.PORTAL, player.getX(), player.getY() + random.nextDouble() *
                    2.0, player.getZ(), random.nextGaussian(), 0, random.nextGaussian());
                particle.setColor(r, g, b);
                engine.add(particle);
            }

            return true;
        }

        return false;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0] > 0) count[0]--;
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "spectral_veil", 1, 2, 3, 4);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("spectral_veil", 5).withStyle(ChatFormatting.BLUE));
        return tooltips;
    }
}
