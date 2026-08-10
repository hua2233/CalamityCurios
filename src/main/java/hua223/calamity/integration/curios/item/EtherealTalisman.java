package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class EtherealTalisman extends BaseCurio {
    public EtherealTalisman(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (usedHand == InteractionHand.MAIN_HAND) {
            ItemStack stack = player.getItemInHand(usedHand);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("disable_apply", !tag.getBoolean("disable_apply"));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.automaticUsePotion = stack.getOrCreateTag().getBoolean("disable_apply");
    }


    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.automaticUsePotion = false;
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
        tooltips.add(CMLangUtil.getTranslatable("talisman", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("talisman",
            stack.getOrCreateTag().getBoolean("disable_apply") ? 3 : 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("talisman", 4).withStyle(ChatFormatting.AQUA));

        return tooltips;
    }
}
