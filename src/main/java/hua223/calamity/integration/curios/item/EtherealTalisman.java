package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class EtherealTalisman extends BaseCurio {
    public EtherealTalisman(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand usedHand) {
        if (usedHand == InteractionHand.MAIN_HAND) {
            ItemStack stack = pPlayer.getItemInHand(usedHand);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("disable_apply", !tag.getBoolean("disable_apply"));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(pPlayer.getItemInHand(usedHand));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putBoolean("disable_apply", true);
        return stack;
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityCapProvider.safetyRunCalamityMagic(player, expand ->
            expand.calamity$SetAutomaticUsePotion(stack.getOrCreateTag().contains("disable_apply")));
    }


    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityCapProvider.safetyRunCalamityMagic(player,
            expand -> expand.calamity$SetAutomaticUsePotion(false));
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "talisman", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(uuid, "talisman", 150, AttributeModifier.Operation.ADDITION));
        modifier.put(AttributeRegistry.SPELL_POWER.get(),
            new AttributeModifier(uuid, "talisman", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.MAGIC_REDUCTION.get(),
            new AttributeModifier(uuid, "talisman", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("talisman", 2));
            tooltips.add(CMLangUtil.getTranslatable("talisman", 3));
            tooltips.add(CMLangUtil.getTranslatable("talisman", 4));
            if (stack.getOrCreateTag().getBoolean("disable_apply")) {
                tooltips.add(CMLangUtil.getTranslatable("talisman", 5).withStyle(ChatFormatting.GOLD));
            } else tooltips.add(CMLangUtil.getTranslatable("talisman", 6).withStyle(ChatFormatting.GOLD));
        } else tooltips.add(CMLangUtil.getTranslatable("talisman", 1));

        return tooltips;
    }
}
