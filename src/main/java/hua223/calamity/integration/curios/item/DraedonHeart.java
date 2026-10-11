package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.Adrenaline;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.hud.AdrenalineHud;
import hua223.calamity.render.item.IDraedonCreationBG;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
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
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class DraedonHeart extends BaseCurio implements ICuriosStorage,
    IKeyDataPackResponse, IDataPackResponse, IDraedonCreationBG {
    public DraedonHeart(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            player.Calamity$Player.adrenaline.switchMode();
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
        player.Calamity$Player.adrenaline.setEnabled(true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
        player.Calamity$Player.adrenaline.setEnabled(false);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        player.Calamity$Player.adrenaline.adrenalineActivate(true);
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_K;
    }

    @ApplyEvent
    public final void onDamage(DamageListener listener) {
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
                adrenaline.zero();
            }
        }
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        Adrenaline adrenaline = listener.player.Calamity$Player.adrenaline;
        if (adrenaline.isActive() && !adrenaline.isNanoMachinesMode())
            listener.amplifier += adrenaline.getAmplifier();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        byte stateInfo = tag.getByte("bit");
        for (int i = 0; i < 6; i++)
            if (((stateInfo >> i) & 1) == 1) byBit(i, tag);
    }

    @OnlyIn(Dist.CLIENT)
    private static void byBit(int bit, CompoundTag tag) {
        switch (bit) {
            case 0 -> AdrenalineHud.INSTANCE.setEnabled(tag.getBoolean("state"));

            case 1 -> AdrenalineHud.INSTANCE.setProgress(tag.getInt("value"));

            case 2 -> AdrenalineHud.INSTANCE.setDisplayCount(tag.getByte("count"));

            case 3 -> AdrenalineHud.INSTANCE.playAnimation();

            case 4 -> AdrenalineHud.INSTANCE.active();

            case 5 -> AdrenalineHud.INSTANCE.setModel(tag.getBoolean("nano"));
        }
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20))
            player.Calamity$Player.adrenaline.addValue();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "draedon_heart", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("draedon_heart", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("draedon_heart", AdrenalineHud.INSTANCE.isNanoMode() ? 6 : 7).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
