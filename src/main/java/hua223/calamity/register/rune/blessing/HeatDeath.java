package hua223.calamity.register.rune.blessing;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PreAttackListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class HeatDeath extends Blessing {
    @Override
    protected String prefix() {
        return "heat_death";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        super.onSlotChange(to, stack, player);
        removeOrApplyEventsBasedOnData(0, .2f, to, player);
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onAttack(PreAttackListener listener) {
        ServerPlayer player = listener.player;

        player.hurt(player.damageSources().magic(), player.getMaxHealth() * (0.05f + listener.player.getRandom().nextFloat() * 0.05f));
        if (!player.isDeadOrDying()) {
            float[] data =  listener.player.Calamity$Player.getRuneData(this);
            listener.amplifier += data[0];

            MobEffect effect = CalamityEffects.HEAT_DEATH.get();
            if (!listener.entity.hasEffect(effect))
                listener.entity.calamity$ForciblyAddEffect(new MobEffectInstance(effect, 200), listener.player);
        }
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(.25, AttributeModifier.Operation.MULTIPLY_TOTAL),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, prefix(), 1, 2);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable(prefix(), 3).withStyle(ChatFormatting.DARK_RED));
    }
}
