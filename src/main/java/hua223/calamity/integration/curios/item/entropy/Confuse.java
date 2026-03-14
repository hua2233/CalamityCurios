package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Confuse extends Card {
    public Confuse(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            boolean flag = CalamityHelp.getCalamityFlag(listener.player, 10);
            listener.entity.addEffect(new MobEffectInstance(
                CalamityEffects.DECEIVE.get(), flag ? 90 : 60, flag ? 1 : 0));
            listener.player.getCooldowns().addCooldown(this, flag ? 140 : 200);
        }
    }

    @ApplyGlobalLoot
    public final void onEntityGlobalLoot(EntitiesLootContext context) {
        EnderMan man = context.verification(EntityType.ENDERMAN);
        if (man != null && man.getTarget() == null && context.player.hasEffect(MobEffects.INVISIBILITY)
            && context.entity.level.getBiome(context.entity.getOnPos()).is(Biomes.WARPED_FOREST))
            context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {

        tooltips.add(CMLangUtil.getTranslatable("confuse", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("confuse", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
