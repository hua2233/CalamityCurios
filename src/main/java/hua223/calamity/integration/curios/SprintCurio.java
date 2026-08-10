package hua223.calamity.integration.curios;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.ApplyKeyEvent;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class SprintCurio extends BaseCurio implements IDataPackResponse {
    public SprintCurio(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (notAttachment()) NetMessages.sendToClient(new ApplyKeyEvent(GLFW.GLFW_KEY_R, true), player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (notAttachment()) NetMessages.sendToClient(new ApplyKeyEvent(GLFW.GLFW_KEY_R, false), player);
    }

    public int getTime() {
        return 0;
    }

    public double getSpeed() {
        return 0d;
    }

    public void preparingForSprint(ServerPlayer player) {}

    public void onSprinting(ServerPlayer player) {}

    public void onCollision(ServerPlayer player, LivingEntity target) {}

    public int getCooldownTime() {
        return 0;
    }

    public double getCooldownReduced() {
        return 0;
    }

    public double getSpeedAmplifier() {
        return 0;
    }

    public boolean notAttachment() {
        return true;
    }

    public boolean isEffectiveAttachment(ServerPlayer player) {
        return true;
    }

    public static void onServerResponse(ServerPlayer player) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player).resolve();
        if (optional.isPresent()) {
            IItemHandlerModifiable handler = optional.get().getEquippedCurios();
            ArrayList<SprintCurio> curios = new ArrayList<>(handler.getSlots());
            SprintCurio mainCurio = null;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof SprintCurio sprint) {
                    if (sprint.notAttachment()) mainCurio = sprint;
                    else if (sprint.isEffectiveAttachment(player)) curios.add(sprint);
                }
            }

            if (mainCurio != null) mainCurio.applySprinting(player, mainCurio, curios);
        }
    }

    private void applySprinting(ServerPlayer player, SprintCurio curio, List<SprintCurio> attachment) {
        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(curio)) return;
        cooldowns.addCooldown(curio, getReducedCoolingTime(curio, attachment));

        int time = curio.getTime();
        CompoundTag pack = getPack();
        pack.putFloat("time", time);
        pack.putDouble("speed", getFinalSpeed(curio, attachment));
        sendToClient(player);

        int[] sprintTime = {time + 1};
        attachment.add(curio);
        for (SprintCurio sprint : attachment) sprint.preparingForSprint(player);
        DelayRunnable.addUniqueLoopTask(() -> {
            if (player.isAlive() && sprintTime[0]-- > 0) {
                for (SprintCurio sprint : attachment)
                    sprint.onSprinting(player);

                List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(1.3f));
                if (!entities.isEmpty()) {
                    for (LivingEntity entity : entities)
                        if (entity.isPickable() && entity != player && !entity.isAlliedTo(player))
                            for (SprintCurio sprint : attachment)
                                sprint.onCollision(player, entity);
                }
                return false;
            }

            return true;
        }, 1, player.getUUID());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (tag.contains("stop")) {
            Vec3 vector = player.getDeltaMovement();
            player.setDeltaMovement(vector.scale(-0.9));
            DelayRunnable.removeTask(SprintCurio.class);
        } else {
            int[] sprintTime = {tag.getInt("time")};
            final double sprintSpeed = tag.getDouble("speed");
            DelayRunnable.addUniqueLoopTask(() -> {
                if (player.isAlive() && sprintTime[0]-- > 0) {
                    float f = player.getYRot() * Mth.DEG_TO_RAD;
                    double factor = player.onGround() ? sprintSpeed : sprintSpeed * 0.6;
                    player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(f) * factor, 0.0D, Mth.cos(f) * factor));
                    return false;
                }

                return true;
                //We should try to apply different locks as much as possible, because the integrated server is shared
            }, 1, SprintCurio.class);
        }
    }

    private static int getReducedCoolingTime(SprintCurio curio, List<SprintCurio> attachment) {
        int baseCooldown = curio.getCooldownTime();
        if (attachment.isEmpty()) return baseCooldown;
        double reductionRatio = 1 - attachment.stream().mapToDouble(SprintCurio::getCooldownReduced).sum();

        return (int) Math.max(0, baseCooldown * reductionRatio);
    }

    private static double getFinalSpeed(SprintCurio curio, List<SprintCurio> attachment) {
        double baseSpeed = curio.getSpeed();
        if (attachment.isEmpty()) return baseSpeed;
        double speedAmplifier = 1 + attachment.stream().mapToDouble(SprintCurio::getSpeedAmplifier).sum();
        return Math.max(0, baseSpeed * speedAmplifier);
    }

    protected void applyCounterforce(ServerPlayer player) {
        getPack().putByte("stop", (byte) 0);
        sendToClient(player);
        DelayRunnable.removeTask(player.getUUID());
    }
}
