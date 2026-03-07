package hua223.calamity.register.Items;

import com.google.common.collect.HashMultimap;
import hua223.calamity.register.entity.EternityHex;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

//The only thing in the world that is eternal and unchanging is change itself.....
public class Eternity extends UniqueSpellBook {
    public Eternity() {
        super(SpellRarity.LEGENDARY, new SpellDataRegistryHolder[] {
            new SpellDataRegistryHolder(SpellRegistry.STARFALL_SPELL, 11),
            new SpellDataRegistryHolder(SpellRegistry.BLACK_HOLE_SPELL, 7),
            new SpellDataRegistryHolder(SpellRegistry.ECHOING_STRIKES_SPELL, 6),
            new SpellDataRegistryHolder(SpellRegistry.PLANAR_SIGHT_SPELL, 5),
        },11, () -> {
            HashMultimap<Attribute, AttributeModifier> map = HashMultimap.create(4, 1);
            map.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier("eternity", 250, AttributeModifier.Operation.ADDITION));
            map.put(AttributeRegistry.CAST_TIME_REDUCTION.get(),
                new AttributeModifier("eternity", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));
            map.put(AttributeRegistry.SPELL_RESIST.get(),
                new AttributeModifier("eternity", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
            map.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                new AttributeModifier("eternity", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
            return map;
        });
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && !player.isUsingItem()) {
            LivingEntity entity = CalamityHelp.getSightDetectionEntityResult(player, level, 16);
            if (entity != null) {
                EternityHex.create(player, level, entity);
                player.startUsingItem(hand);
            }
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10000;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag advanced) {
        super.appendHoverText(stack, level, components, advanced);
        if (Screen.hasShiftDown()) {
            components.add(CMLangUtil.getTranslatable("eternity", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else components.add(CMLangUtil.getTranslatable("eternity", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
