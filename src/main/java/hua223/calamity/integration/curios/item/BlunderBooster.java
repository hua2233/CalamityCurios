package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.ProjectileSpawnListener;
import hua223.calamity.register.entity.TeslaAura;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.caelus.api.CaelusApi;

import java.util.List;
import java.util.UUID;

@ConflictChain(PlagueFuelPack.class)
public class BlunderBooster extends BaseCurio implements ICuriosStorage {
    @OnlyIn(Dist.CLIENT)
    public static int energy = 60;
    public BlunderBooster(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getUUID(player)[0] = TeslaAura.create(player, true).getUUID();
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        killAffiliated(player);
    }

    @Override
    public void onLogOut(Player player) {
        if (!player.isLocalPlayer())
            killAffiliated((ServerPlayer) player);
    }

    private void killAffiliated(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        Entity entity = level.getEntity(getFirstUUID(player));
        if (entity != null && entity.isAlive()) entity.discard();
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CaelusApi.getInstance().getFlightAttribute(),
            new AttributeModifier(uuid, "blunder_booster", 1, AttributeModifier.Operation.ADDITION));
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.probability += 0.1f;
        listener.applyAmplifier(0.12f);
    }

    @ApplyEvent
    public final void onProjectileShoot(ProjectileSpawnListener listener) {
        listener.speedVectorAmplifier += 0.15f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void onClientTick(Player player) {
        if (player.isFallFlying()) {
            if (energy > 2 && player.getFallFlyingTicks() % 10 == 0
                && Minecraft.getInstance().options.keyJump.isDown()) {
                energy -= 2;
                Vec3 vec3 = player.getLookAngle().normalize();
                player.push(vec3.x * 0.75f, vec3.y * 0.55f, vec3.z * 0.75f);
            }
        } else if(energy < 60) energy += 2;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    public boolean storageCount() {
        return false;
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected boolean startClientTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("blunder_booster", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("blunder_booster", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("blunder_booster", 3).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("blunder_booster", 4).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("blunder_booster", 5).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("blunder_booster", 6).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
