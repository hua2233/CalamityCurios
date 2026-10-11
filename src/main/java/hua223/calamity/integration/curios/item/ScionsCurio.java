package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.ProjectileHitListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.FishingLootContext;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.AcidicRain;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "在沼泽地区钓鱼可能获得，受幸运影响")
public class ScionsCurio extends BaseCurio {
    public ScionsCurio(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.IRRADIATED.get());
    }

    @ApplyEvent
    public final void onHit(ProjectileHitListener listener) {
        AcidicRain.of(listener.player.level(), listener.target, listener.player);
    }

    @ApplyGlobalLoot
    public void onFishing(FishingLootContext context) {
        if (context.matchBiome(Biomes.SWAMP) && context.luckChance(.06f))
            context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("scions_curio", 1));
        tooltips.add(CMLangUtil.getTranslatable("scions_curio", 2));
        return tooltips;
    }
}
