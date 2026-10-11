package hua223.calamity.register.items;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.item.entropy.AzureAbyssTalisman;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.effects.SurvivableEffectInstance;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "在海底废墟的战利品箱中可以被找到")
@CurioRepel(AzureAbyssTalisman.class)
public class OceanFlower extends BaseCurio {
    protected OceanFlower() {
        super(RegisterList.RARE_ONE);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        AzureAbyssTalisman talisman = (AzureAbyssTalisman) CalamityItems.AZURE_ABYSS_TALISMAN.get();
        talisman.getPack().putInt("flag", 3);
        talisman.sendToClient(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        AzureAbyssTalisman talisman = (AzureAbyssTalisman) CalamityItems.AZURE_ABYSS_TALISMAN.get();
        talisman.getPack().putInt("flag", 1);
        talisman.sendToClient(player);
        if (player.getEffect(MobEffects.NIGHT_VISION) instanceof SurvivableEffectInstance)
            player.removeEffect(MobEffects.NIGHT_VISION);
    }


    @ApplyGlobalLoot
    public void onChestLoot(ChestLootContext context) {
        if (context.fromFuzzyType("underwater_ruin") && context.chance(0.18f))
            context.addLoot(this);
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.isInWater() && !player.hasEffect(MobEffects.NIGHT_VISION))
            player.addEffect(new SurvivableEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, player::isInWater));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ocean_flower").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
