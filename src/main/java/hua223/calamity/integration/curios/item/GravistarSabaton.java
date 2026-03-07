package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.net.C2SPacket.SlamExplosion;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.PlayerJumpPower;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class GravistarSabaton extends BaseCurio {
    @OnlyIn(Dist.CLIENT)
    public static float jumpPower = 1f;
    @OnlyIn(Dist.CLIENT)
    public static boolean impact;
    @OnlyIn(Dist.CLIENT)
    public static float impactSpeed;

    public GravistarSabaton(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (!listener.isTriggerByLiving && listener.source == DamageSource.FALL)
            listener.canceledEvent();
    }

    public static void handleExplosionAffectedEntities(ServerPlayer player, float r, double x, double y, double z) {
        r *= 2;
        int k1 = Mth.floor(x - r - 1);
        int l1 = Mth.floor(x + r + 1);
        int i2 = Mth.floor(y - r - 1);
        int i1 = Mth.floor(y + r + 1);
        int j2 = Mth.floor(z - r - 1);
        int j1 = Mth.floor(z + r + 1);

        LivingEntity[] entities = player.level.getEntities(player, new AABB(k1, i2, j2, l1, i1, j1))
            .stream().filter(entity -> entity instanceof LivingEntity).toArray(LivingEntity[]::new);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(CalamityEffects.ASTRAL_INFECTION.get(), 200, 0));
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "SLAM", true);
        NetMessages.sendToClient(new PlayerJumpPower(0.3f), player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "SLAM", false);
        NetMessages.sendToClient(new PlayerJumpPower(0.3f), player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void onClientTick(Player player) {
        if (impact) {
            if (player.isOnGround()) {
                jumpPower += 1f;
                DelayRunnable.addRunTask(40, () -> jumpPower -= 1f);
                impact = false;

                float r = -impactSpeed;
                NetMessages.sendToServer(new SlamExplosion(r > 10 ? 10 : r));
                impactSpeed = 0;
            } else {
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, impactSpeed -= 0.05f, v.z);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onLogOut(Player player) {
        if (player.isLocalPlayer()) {
            impact = false;
            impactSpeed = 0f;
            jumpPower = 1f;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected boolean startClientTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("gravistar_sabaton", 1));
        tooltips.add(CMLangUtil.getTranslatable("gravistar_sabaton", 2));
        tooltips.add(CMLangUtil.getTranslatable("gravistar_sabaton", 3));
        tooltips.add(CMLangUtil.getTranslatable("gravistar_sabaton", 4));
        tooltips.add(CMLangUtil.getTranslatable("gravistar_sabaton", 5));
        tooltips.add(CMLangUtil.getTranslatable("gravistar_sabaton", 6));
        return tooltips;
    }
}
