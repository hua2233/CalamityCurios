package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.capability.Adrenaline;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.Item.ICustomBackgroundRender;
import hua223.calamity.render.hud.AdrenalineHud;
import hua223.calamity.util.*;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class DraedonHeart extends BaseCurio implements ICuriosStorage,
    IKeyDataPackResponse, IDataPackResponse, ICustomBackgroundRender {
    public DraedonHeart(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            player.Calamity$Player.adrenaline.switchMode(this);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "draedon_heart", 48, AttributeModifier.Operation.ADDITION));
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "draedon_heart", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
        player.Calamity$Player.adrenaline.setEnabled(true, this);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
        player.Calamity$Player.adrenaline.setEnabled(false, this);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        player.Calamity$Player.adrenaline.adrenalineActivate(true, this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_K;
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (!listener.isTriggerByLiving) return;
        ServerPlayer player = listener.player;
        float[] count = getCount(player);
        Adrenaline adrenaline = player.Calamity$Player.adrenaline;

        if (adrenaline.isActive()) {
            if (adrenaline.isNanoMachinesMode()) listener.amplifier -= adrenaline.getDamageOffset();
        } else {
            if (adrenaline.isNanoMachinesMode()) {
                count[0] = -20 - (20 - count[0]);
            } else {
                if (adrenaline.isMax()) listener.amplifier -= adrenaline.getDamageOffset();
                CalamitySounds.MAJOR_LOSS.playSound(player);
                adrenaline.zero(this);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        if (tag.contains("value")) AdrenalineHud.setAdrenalineProgress(tag.getInt("value"));
        if (tag.contains("state")) AdrenalineHud.setAdrenalineEnabled(tag.getBoolean("state"));
        if (tag.contains("count")) AdrenalineHud.setAdrenalineCount(tag.getByte("count"));
        if (tag.contains("isNano")) AdrenalineHud.setForMachinesMode(tag.getBoolean("isNano"));
        if (tag.contains("play")) {
            boolean flag = tag.getBoolean("play");
            if (flag) CalamitySounds.FULL_ADRENALINE.playLocalSound();
            AdrenalineHud.playAnimation(flag);
        }
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        Adrenaline adrenaline = listener.player.Calamity$Player.adrenaline;
        if (adrenaline.isActive() && !adrenaline.isNanoMachinesMode())
            listener.amplifier += adrenaline.getAmplifier();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20))
            player.Calamity$Player.adrenaline.addValue(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderType getRenderType() {
        return RenderUtil.Shaders.getDisintegration();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void internalRender(VertexConsumer consumer, int x1, int y1, int x2, int y2, int z, float[] gradientColor, Matrix4f matrix4f) {
        consumer.vertex(matrix4f, x1, y1, z)
            .color(gradientColor[1], gradientColor[2], gradientColor[3], gradientColor[0])
            .uv(1f, 1f)
            .endVertex();

        consumer.vertex(matrix4f, x1, y2, z)
            .color(gradientColor[5], gradientColor[6], gradientColor[7], gradientColor[4])
            .uv(1f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y2, z)
            .color(gradientColor[5], gradientColor[6], gradientColor[7], gradientColor[4])
            .uv(0f, 0f)
            .endVertex();

        consumer.vertex(matrix4f, x2, y1, z)
            .color(gradientColor[1], gradientColor[2], gradientColor[3], gradientColor[0])
            .uv(0f, 1f)
            .endVertex();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "draedon_heart", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("draedon_heart", AdrenalineHud.isNanoMachinesMode ? 6 : 7).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
