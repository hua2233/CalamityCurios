package hua223.calamity.mixins;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffect.class)
public abstract class EffectMixin {
    @Shadow
    @Final
    private MobEffectCategory category;

    @Inject(at = @At("HEAD"), method = "addAttributeModifiers", cancellable = true)
    private void injectAddModifier(LivingEntity entity, AttributeMap attributeMap, int amplifier, CallbackInfo info) {
        if (category != MobEffectCategory.BENEFICIAL && entity.calamity$IsPlayer)
            if (entity.calamity$Player.Calamity$Player.hasRadianceEffect) info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "applyInstantenousEffect", cancellable = true)
    private void injectInstantenous(Entity source, Entity indirectSource, LivingEntity entity, int amplifier, double health, CallbackInfo info) {
        if (category != MobEffectCategory.BENEFICIAL && entity.calamity$IsPlayer)
            if (entity.calamity$Player.Calamity$Player.hasRadianceEffect) info.cancel();
    }
}
