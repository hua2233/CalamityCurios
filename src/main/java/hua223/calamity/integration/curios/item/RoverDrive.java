package hua223.calamity.integration.curios.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.TrackedListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.hud.BarrierHud;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.render.shaders.DraedonBarrierRenderer;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

@CurioRepel(isRoot = true)
public class RoverDrive extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    protected float absorptionRatio = .25f;
    public RoverDrive(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        float[] counts = getCount(player);
        counts[2]++;

        float max = getMaxBarrierValue(player);
        counts[4] = max;
        counts[7] = max / 12f;

        CompoundTag pack = getPack();
        pack.putFloat("max", max);
        pack.putFloat("value", 0);
        TrackedListener.writeTracker(pack, player);
        sendToTrackingAndSelf(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CompoundTag pack = getPack();
        pack.putFloat("max", 0);
        TrackedListener.writeTracker(pack, player);
        sendToTrackingAndSelf(player);
    }

    private float getMaxBarrierValue(Player player) {
        return player.getMaxHealth() * absorptionRatio;
    }

    @ApplyEvent(20)
    public final void onDamage(DamageListener listener) {
        var memory = getMemory(listener.player);
        float[] count = memory.count;

        count[2] = 1;
        count[1] = 180;
        if (count[3] == 0) {
            float hurt = listener.getCorrectionValue();
            float absorbed = ICuriosStorage.getReducedValue(count, 0, hurt);

            if (absorbed == hurt) listener.canceledEvent();
            else if (absorbed > 0) listener.floating -= absorbed;

            if (count[0] <= 0) {
                count[0] = 0;
                count[3] = 1;
                onChangeBarrierAttribute(listener.player, memory, false);
            }

            CompoundTag pack = getPack();
            pack.putFloat("value", memory.count[0]);
            TrackedListener.writeTracker(pack, listener.player);
            pack.putInt("sound", absorbed == hurt ? 0 : 1);
            sendToTrackingAndSelf(listener.player);
        }
    }

    @ApplyEvent
    public final void onTracked(TrackedListener listener) {
        float[] count = getCount(listener.trackedPlayer);

        CompoundTag pack = getPack();
        pack.putFloat("max", count[4]);
        pack.putFloat("value", count[0]);
        listener.sendTrackedToTheSelf(pack, this);
    }

    protected void onChangeBarrierAttribute(Player player, GlobalCuriosStorage.CuriosMemory memory, boolean chargingCompleted) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        AbstractClientPlayer player = TrackedListener.getInitiator(tag);
        if (player != null) {
            BarrierHud hud = BarrierHud.getInstance();
            CalamityPlayer localModPlayer = player.Calamity$Player;
            if (tag.contains("max", 5)) {
                localModPlayer.shieldDurabilityMax = tag.getFloat("max");
                localModPlayer.shieldDurability = Math.min(localModPlayer.shieldDurability, localModPlayer.shieldDurabilityMax);

                if (player.isLocalPlayer()) {
                    if (localModPlayer.shieldDurabilityMax == 0) hud.close();
                    else if (hud.notRender) hud.start();
                    else hud.setProgress(localModPlayer.shieldDurability);
                }
            }

            if (tag.contains("value", 5)) {
                localModPlayer.shieldDurability = tag.getFloat("value");
                if (player.isLocalPlayer()) {
                    hud.setProgress(localModPlayer.shieldDurability);
                    if (!tag.contains("sound", 3)) return;

                    ClientLevel level = player.clientLevel;
                    Vec3 pos = player.position().add(0, .6f, 0);

                    switch (tag.getInt("sound")) {
                        case 0 -> level.playLocalSound(pos.x, pos.y, pos.z, CalamitySounds.DRAEDON_BARRIER_HIT.get(), SoundSource.PLAYERS, .6f, .6f, false);
                        case 1 -> level.playLocalSound(pos.x, pos.y, pos.z, CalamitySounds.DRAEDON_BARRIER_BREAK.get(), SoundSource.PLAYERS, .75f, 1, false);
                        case 2 -> level.playLocalSound(pos.x, pos.y, pos.z, CalamitySounds.DRAEDON_BARRIER_ACTIVE.get(), SoundSource.PLAYERS, .85f, 1, false);
                    }
                }
            }
        }
    }
    @Override
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        if (memory.count[5]++ == 10) {
            memory.count[5] = 0;
            float max = getMaxBarrierValue(player);
            if (Mth.abs(max - memory.count[4]) > 0.5f) {
                memory.count[7] = max / 12f;

                if (memory.count[4] > max) memory.count[0] = Math.min(max, memory.count[0]);
                else  {
                    memory.count[2] = 1;
                    memory.count[1] = Math.max(30, memory.count[1]);
                }

                memory.count[4] = max;
                CompoundTag pack = getPack();
                ServerPlayer serverPlayer = (ServerPlayer) player;

                pack.putFloat("max", max);
                TrackedListener.writeTracker(pack, serverPlayer);
                sendToTrackingAndSelf(serverPlayer);
            }
        }

        if (memory.count[2] == 1 && --memory.count[1] <= 0) {
            memory.count[0] += memory.count[7];
            CompoundTag pack = getPack();

            if (memory.count[0] >= memory.count[4]) {
                memory.count[0] = memory.count[4];
                memory.count[2] = 0;
                if (memory.count[3] == 1) {
                    memory.count[3] = 0;
                    pack.putInt("sound", 2);
                    onChangeBarrierAttribute(player, memory, true);
                }
            } else memory.count[1] = 10;

            ServerPlayer serverPlayer = (ServerPlayer) player;
            pack.putFloat("value", memory.count[0]);

            TrackedListener.writeTracker(pack, serverPlayer);
            sendToTrackingAndSelf(serverPlayer);
        }
    }

    @Override
    public int getCountSize() {
        return 8;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style style = Style.EMPTY.withColor(ChatFormatting.YELLOW);
        tooltips.add(CMLangUtil.getDynamic("rover_drive", 1,  (int) (absorptionRatio * 100)).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("rover_drive", 2).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("rover_drive", 3).setStyle(style));
        return tooltips;
    }

    @OnlyIn(Dist.CLIENT)
    public static class BarrierRenderer implements ICurioRenderer {
        private final int[] palette = new int[] {
            0xFF3366FF,0xFF47CAFF, 0xFFC2FF43
        };

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent,
                                                                              MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount,
                                                                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            LivingEntity entity = slotContext.entity();
            if (entity.calamity$IsPlayer) {
                partialTicks += RenderUtil.getLocalTick();
                // Noise scale also grows and shrinks, although out of sync with the shield
                float noiseScale = Mth.lerp((float)Math.sin(partialTicks * 0.5) * 0.5f + 0.5f, 0.4f, 0.8f);

                int edgeColor = CalamityHelp.multicolorLerp(partialTicks * 0.01f, palette);
                DraedonBarrierRenderer.setUniform(noiseScale, edgeColor);

                float scale = 0.18f + 0.05f * (0.5f + 0.5f * (float)Math.sin(partialTicks * .12));
                float size = .85f + scale;

                Matrix4f matrix4f = poseStack.last().pose();
                float offset = entity.getBbHeight() * .275f;
                matrix4f.translate(0, offset, 0);

                CalamityPlayer localModPlayer = entity.calamity$Player.Calamity$Player;
                float shieldDurabilityRatio = localModPlayer.shieldDurability / localModPlayer.shieldDurabilityMax;
                float shieldStrength = (float) Math.pow(shieldDurabilityRatio, 0.5);

                float baseShieldOpacity = 0.9f + 0.1f * (float)Math.sin(partialTicks  * .6);
                baseShieldOpacity *= (0.5f + 0.5f * shieldStrength);
                VertexConsumer consumer = multiBufferSource.getBuffer(CalamityShaders.getDraedonBarrierRenderType());

                Quaternionf quaternionf = new Quaternionf();
                for (int i = 0; i < 4; i++) builderVertex(consumer, RenderUtil.TRANSIENT_MATRIX.set(matrix4f)
                    .rotate(quaternionf.identity().rotateY(90 * i * Mth.DEG_TO_RAD)).translate(0, 0, size), size, baseShieldOpacity);

                builderVertex(consumer, RenderUtil.TRANSIENT_MATRIX.set(matrix4f).rotate(quaternionf.identity()
                    .rotateX(90 * Mth.DEG_TO_RAD)).translate(0, 0, size), size, baseShieldOpacity);

                builderVertex(consumer, RenderUtil.TRANSIENT_MATRIX.set(matrix4f).rotate(quaternionf.identity()
                    .rotateX(-90 * Mth.DEG_TO_RAD)).translate(0, 0, size), size, baseShieldOpacity);

                matrix4f.translate(0, -offset, 0);
            }
        }

        private static void builderVertex(VertexConsumer builder, Matrix4f matrix4f, float size, float baseShieldOpacity) {
            builder.vertex(matrix4f, -size, -size, 0f).color(1, 1, 1, baseShieldOpacity).uv(1, 1).endVertex();
            builder.vertex(matrix4f, -size, size, 0f).color(1, 1, 1, baseShieldOpacity).uv(1, 0).endVertex();
            builder.vertex(matrix4f, size, size, 0f).color(1, 1, 1, baseShieldOpacity).uv(0, 0).endVertex();
            builder.vertex(matrix4f, size, -size, 0f).color(1, 1, 1, baseShieldOpacity).uv(0, 1).endVertex();
        }
    }
}
