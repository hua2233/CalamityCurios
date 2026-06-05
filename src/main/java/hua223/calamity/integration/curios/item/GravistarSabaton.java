package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.net.packets.DataPackActive;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GravistarSabaton extends BaseCurio implements IKeyDataPackResponse {
    @OnlyIn(Dist.CLIENT)
    public static float impactSpeed;

    public GravistarSabaton(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (!listener.isTriggerByLiving && listener.source.is(DamageTypeTags.IS_FALL))
            listener.canceledEvent();
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
        IDataPackResponse response = CalamityItems.BOUNDING.asPackHandler();
        response.getPack().putFloat("gravistar_sabaton", 0.3f);
        response.sendToClient(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
        IDataPackResponse response = CalamityItems.BOUNDING.asPackHandler();
        response.getPack().putFloat("gravistar_sabaton", -0.3f);
        response.sendToClient(player);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        float r = tag.getFloat("radius");
        player.getCooldowns().addCooldown(this, (int) Math.min(r * 40, 260));
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        player.level().explode(player, x, y, z, r, Level.ExplosionInteraction.NONE);

        r *= 2;
        int k1 = Mth.floor(x - r - 1);
        int l1 = Mth.floor(x + r + 1);
        int i2 = Mth.floor(y - r - 1);
        int i1 = Mth.floor(y + r + 1);
        int j2 = Mth.floor(z - r - 1);
        int j1 = Mth.floor(z + r + 1);

        player.level().getEntities(player, new AABB(k1, i2, j2, l1, i1, j1)).forEach(entity -> {
                if (entity instanceof  LivingEntity living)
                    living.addEffect(new MobEffectInstance(CalamityEffects.ASTRAL_INFECTION.get(), 200, 0));
            });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_X;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public boolean accept(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (notInCooling(minecraft) && DelayRunnable.addUniqueLoopTask(() -> {
            if (player.isAlive() && player.onGround()) {
                player.Calamity$Player.jumpPower += 1f;
                DelayRunnable.addRunTask(40, () -> player.Calamity$Player.jumpPower -= 1f);
                NetMessages.sendToServer(new DataPackActive(this));

                return true;
            } else {
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, impactSpeed -= 0.05f, v.z);
                return false;
            }
        }, 1, GravistarSabaton.class)) impactSpeed = 0;

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public CompoundTag getSerializationStream() {
        CompoundTag tag = new CompoundTag();
        impactSpeed = -impactSpeed;
        tag.putFloat("radius", impactSpeed > 10 ? 10 : impactSpeed);
        return tag;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "gravistar_sabaton", 1, 2, 3, 4, 5, 6);
        return tooltips;
    }
}
