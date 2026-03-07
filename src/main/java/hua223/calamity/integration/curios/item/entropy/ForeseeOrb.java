package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class ForeseeOrb extends BaseCurio implements ICuriosStorage {
    public ForeseeOrb(Properties properties) {
        super(properties);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (getCount(player)[1] > 0) player.calamity$HurtNoInvulnerable(
            DamageSource.OUT_OF_WORLD, player.getMaxHealth() * 0.9f);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new VariableAttributeModifier(uuid, "foresee_orb", 0.12, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.baseAmount > 8f) {
            listener.setFinalAmount(8);
            listener.player.getCooldowns().addCooldown(this, 600);
            Tuple<float[], UUID[]> pair = getPair(listener.player);
            pair.getA()[0] = 1;
            pair.getA()[1] = 600;
            VariableAttributeModifier.updateModifierInInstance(listener.player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), pair.getB()[0], 0);
        }
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0] == 1 && --count[1] == 0) {
            count[0] = 0;
            VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), getFirstUUID(player), 0.12);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("foresee_orb", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("foresee_orb", 2).withStyle(ChatFormatting.RED));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("foresee_orb", 3).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
