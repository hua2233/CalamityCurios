package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IServantsReinforcement;
import hua223.calamity.util.PlayerServantsManager;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "击杀高位唤魔者掉落")
public class Temperance extends Card implements IServantsReinforcement {
    public Temperance(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        PlayerServantsManager.addServantsReinforcement(player, this);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        PlayerServantsManager.removeServantsReinforcement(player, this);
    }

    @Override
    public void modifyServant(boolean apply, ServerPlayer player, LivingEntity servant) {
        boolean deck = player.Calamity$Player.cardDeck;
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.MAX_HEALTH, deck ? 1 : 0.6, AttributeModifier.Operation.MULTIPLY_BASE);
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ARMOR, deck ? 8 : 4, AttributeModifier.Operation.ADDITION);
    }

    @ApplyGlobalLoot
    public void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityRegistry.ARCHEVOKER.get())
            && context.chance(0.7f)) context.addLoot(this);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("temperance", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("temperance", 2).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
