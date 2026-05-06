package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.hud.AdrenalineHud;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class DraedonHeart extends BaseCurio implements
    ICuriosStorage, IKeyDataPackResponse, IDataPackResponse {
    public DraedonHeart(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
                adrenaline -> adrenaline.switchMode((ServerPlayer) player, this));
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
        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
            adrenaline -> adrenaline.setEnabled(player, true, this));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(adrenaline ->
            adrenaline.setEnabled(player, false, this));
    }

    @Override
    public void onServerResponse(ServerPlayer player) {
        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
            adrenaline -> adrenaline.adrenalineActivate(player, true, this));
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

        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(adrenaline -> {
            if (adrenaline.isActive()) {
                if (adrenaline.isNanoMachinesMode()) listener.amplifier -= adrenaline.getDamageOffset();
            } else {
                if (adrenaline.isNanoMachinesMode()) {
                    count[0] = -20 - (20 - count[0]);
                } else {
                    if (adrenaline.isMax()) listener.amplifier -= adrenaline.getDamageOffset();
                    player.level().playSound(null, player, CalamitySounds.MAJOR_LOSS.get(), SoundSource.PLAYERS, 1f, 1f);
                    adrenaline.zero(listener.player, this);
                }
            }
        });
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
            if (flag) Minecraft.getInstance().player.playSound(CalamitySounds.FULL_ADRENALINE.get());
            AdrenalineHud.playAnimation(flag);
        }
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        CalamityCapProvider.ADRENALINE.getCapabilityFrom(listener.player).ifPresent(adrenaline -> {
            if (adrenaline.isActive() && !adrenaline.isNanoMachinesMode())
                listener.amplifier += adrenaline.getAmplifier();
        });
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0]++ == 20) {
            count[0] = 0;
            CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
                adrenaline -> adrenaline.addValue((ServerPlayer) player, this));
        }
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
