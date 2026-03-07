package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Inspiration extends Card {
    public Inspiration(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "inspiration", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyGlobalLoot
    public final void onGlobalChestLoot(ChestLootContext context) {
        if (context.fromSpecificName("desert_pyramid") && context.chance(0.4f))
            context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("inspiration").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
