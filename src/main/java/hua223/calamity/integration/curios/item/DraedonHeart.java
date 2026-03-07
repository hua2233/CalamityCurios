package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.clientInfos.Adrenaline;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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

import java.util.List;
import java.util.UUID;

public class DraedonHeart extends BaseCurio implements ICuriosStorage {
    public DraedonHeart(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
                adrenaline -> adrenaline.switchMode((ServerPlayer) player));
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
        CalamityHelp.setKeyEvent(player, "ADRENALINE_ACTIVE", true);
        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
            adrenaline -> adrenaline.setEnabled(player, true));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "ADRENALINE_ACTIVE", false);
        CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(adrenaline ->
            adrenaline.setEnabled(player, false));
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
                    player.level.playSound(null, player, CalamitySounds.MAJOR_LOSS.get(), SoundSource.PLAYERS, 1f, 1f);
                    adrenaline.zero(listener.player);
                }
            }
        });
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
        if (addCount(player, 0) == 20) {
            float[] count = getCount(player);
            count[0] = 0;

            CalamityCapProvider.ADRENALINE.getCapabilityFrom(player).ifPresent(
                adrenaline -> adrenaline.addValue((ServerPlayer) player));
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 2));
            tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 3));
            tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 4));
            tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 5));
            if (Adrenaline.isNanoMachinesMode)
                tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 6).withStyle(ChatFormatting.GOLD));
            else tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 7).withStyle(ChatFormatting.GOLD));
        } else tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 1));
        return tooltips;
    }
}
