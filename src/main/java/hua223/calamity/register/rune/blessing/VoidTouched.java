package hua223.calamity.register.rune.blessing;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.register.effects.CalamityEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidTouched extends Blessing {
    @Override
    protected String prefix() {
        return "void_touched";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        addVoidTouched(player, to ? .2f : -.2f);
    }

    @ApplyEvent(300)
    @SuppressWarnings("ConstantConditions")
    public final void onAttack(DamageListener listener) {
       float[] data = listener.player.Calamity$Player.getRuneData(this);
       if (data[0] > 0f) {
           listener.entity.hurt(listener.player.damageSources().outOfBorder(), listener.getCorrectionValue() * data[0]);

           if (listener.entity.isAlive()) {
               int level = (int) data[0] * 10;
               MobEffect effect = CalamityEffects.VOID_TOUCH.get();
               MobEffectInstance instance = listener.entity.getEffect(effect);

               if (instance == null) listener.entity.calamity$ForciblyAddEffect(new MobEffectInstance(effect, level, 200), listener.player);
               else if (instance.getAmplifier() < level) instance.calamity$SetProperties(200, level, listener.entity);
           }
       }
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayVoidTouchedValue(tooltips, .2f);
    }
}
