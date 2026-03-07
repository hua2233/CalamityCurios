package hua223.calamity.integration.curios;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.ClientSprint;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

public abstract class SprintCurio extends BaseCurio {
    public SprintCurio(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (!isAttachment()) CalamityHelp.setKeyEvent(player, "SPRINTING", true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (!isAttachment()) CalamityHelp.setKeyEvent(player, "SPRINTING", false);
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

    public boolean isAttachment() {
        return false;
    }

    public boolean isEffectiveAttachment(ServerPlayer player) {
        return true;
    }

    public static void onServerResponse(ServerPlayer player) {
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
            ArrayList<SprintCurio> curios = new ArrayList<>(handler.getSlots());
            SprintCurio mainCurio = null;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof SprintCurio sprint) {
                    if (!sprint.isAttachment()) {
                        curios.add(sprint);
                        mainCurio = sprint;
                    }
                    else if (sprint.isEffectiveAttachment(player)) curios.add(sprint);
                }
            }

            if (mainCurio != null)
                applySprinting(player, mainCurio, curios);
        });
    }

    public static void applySprinting(ServerPlayer player, SprintCurio curio, List<SprintCurio> attachment) {
        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(curio)) return;
        cooldowns.addCooldown(curio, getReducedCoolingTime(curio, attachment));

        int time = curio.getTime();
        NetMessages.sendToClient(new ClientSprint(time, getFinalSpeed(curio, attachment)), player);
        int[] sprintTime = {time + 1};
        for (SprintCurio sprint : attachment) sprint.preparingForSprint(player);
        DelayRunnable.addUniqueLoopTask(() -> {
            if (player.isAlive() && sprintTime[0]-- > 0) {
                for (SprintCurio sprint : attachment)
                    sprint.onSprinting(player);

                List<LivingEntity> entities = player.level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(1.3f));
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

    @OnlyIn(Dist.CLIENT)
    public static void applyClientSprinting(int time, double sprintSpeed) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int[] sprintTime = {time};
        DelayRunnable.addUniqueLoopTask(() -> {
            if (player.isAlive() && sprintTime[0]-- > 0) {
                float f = player.getYRot() * Mth.DEG_TO_RAD;
                double factor = player.isOnGround() ? sprintSpeed : sprintSpeed * 0.6;
                player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(f) * factor, 0.0D, Mth.cos(f) * factor));
                return false;
            }

            return true;
            //We should try to apply different locks as much as possible, because the integrated server is shared
        }, 1, SprintCurio.class);
    }

    private static int getReducedCoolingTime(SprintCurio curio, List<SprintCurio> attachment) {
        int baseCooldown = curio.getCooldownTime();
        if (attachment.isEmpty()) return baseCooldown;
        double reductionRatio = 1 - attachment.stream().filter(SprintCurio::isAttachment)
            .mapToDouble(SprintCurio::getCooldownReduced).sum();

        return (int) Math.max(0, baseCooldown * reductionRatio);
    }

    private static double getFinalSpeed(SprintCurio curio, List<SprintCurio> attachment) {
        double baseSpeed = curio.getSpeed();
        if (attachment.isEmpty()) return baseSpeed;
        double speedAmplifier = 1 + attachment.stream().filter(SprintCurio::isAttachment)
            .mapToDouble(SprintCurio::getSpeedAmplifier).sum();

        return Math.max(0, baseSpeed * speedAmplifier);
    }

    protected static void applyCounterforce(ServerPlayer player) {
        NetMessages.sendToClient(ClientSprint.stopSprinting(), player);
        DelayRunnable.removeTask(player.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopClientSprint() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Vec3 vector = player.getDeltaMovement();
            player.setDeltaMovement(vector.scale(-0.9));
            DelayRunnable.removeTask(SprintCurio.class);
        }
    }
}
