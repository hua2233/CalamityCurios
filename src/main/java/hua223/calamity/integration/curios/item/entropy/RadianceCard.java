package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.PlayerHealListener;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class RadianceCard extends Card {
    public RadianceCard(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        listener.amplification += 0.2f;
    }

    @ApplyGlobalLoot
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityRegistry.PRIEST.get())
            && context.chance(0.4f)) context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("radiance_card", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("radiance_card", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
