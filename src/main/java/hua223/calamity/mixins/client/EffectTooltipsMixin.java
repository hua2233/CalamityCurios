package hua223.calamity.mixins.client;

import hua223.calamity.register.effects.CalamityEffect;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectTooltipsMixin {
    @Unique
    private static MobEffectInstance calamity$Capture;
    @Redirect(method = "renderEffects", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/effect/MobEffectUtil;formatDuration(Lnet/minecraft/world/effect/MobEffectInstance;F)Ljava/lang/String;"))
    private String captureTemp(MobEffectInstance effect, float durationFactor) {
        calamity$Capture = effect;
        return MobEffectUtil.formatDuration(effect, durationFactor);
    }

    @Redirect(method = "renderEffects", at = @At(value = "INVOKE",
        target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"))
    private List<Object> effectTooltips(Object e1, Object e2) {
        List<Object> list = new ArrayList<>();
        list.add(e1);
        list.add(e2);
        if (calamity$Capture.getEffect() instanceof CalamityEffect effect)
            effect.appendTooltips(list);
        calamity$Capture = null;
        return list;
    }
}
