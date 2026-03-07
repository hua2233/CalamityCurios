package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.FriendlyEffectCloudBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = PlagueFuelPack.class, isRoot = true)
public class PlagueFuelPack extends BaseCurio {
    @OnlyIn(Dist.CLIENT)
    public static int energy = 40;

    public PlagueFuelPack(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.isFallFlying() && player.getFallFlyingTicks() % 40 == 0) {
            new FriendlyEffectCloudBuilder(player, player.getEyePosition(), 40, 2f)
                .setEffects(new MobEffectInstance(CalamityEffects.PLAGUE.get(), 200, 7))
                .build();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void onClientTick(Player player) {
        if (player.isFallFlying()) {
            if (energy > 2 && player.getFallFlyingTicks() % 10 == 0
                && Minecraft.getInstance().options.keyJump.isDown()) {//((LocalPlayer) player).input.jumping
                energy -= 2;
                Vec3 vec3 = player.getLookAngle().normalize();
                player.push(vec3.x * 0.6f, vec3.y * 0.4f, vec3.z * 0.6f);
            }
        } else if(energy < 40) energy += 2;
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.applyAmplifier(0.08f);
        listener.probability += 0.1f;
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += 0.15f;
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected boolean startClientTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("plagued_fuel_pack", 1));
        tooltips.add(CMLangUtil.getTranslatable("plagued_fuel_pack", 2));
        tooltips.add(CMLangUtil.getTranslatable("plagued_fuel_pack", 3));
        return tooltips;
    }
}
