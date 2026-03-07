package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.ProjectileHitListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.AcidicRain;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class RustyMedallion extends BaseCurio {
    public RustyMedallion(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.IRRADIATED.get());
    }

    @ApplyEvent
    public final void onHit(ProjectileHitListener listener) {
        Level level = listener.player.level;
        level.addFreshEntity(AcidicRain.of(level, listener.target, listener.player));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("rusty", 1));
        tooltips.add(CMLangUtil.getTranslatable("rusty", 2));
        return tooltips;
    }
}
