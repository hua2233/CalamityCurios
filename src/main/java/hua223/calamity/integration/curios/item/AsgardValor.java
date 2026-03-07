package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value =  SprintCurio.class)
public class AsgardValor extends OrnateShield {
    public AsgardValor(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        super.equipHandle(player, stack);
        CalamityHelp.setCalamityFlag(player, 7, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        CalamityHelp.setCalamityFlag(player, 7, false);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancelHarmfulOnes(0.7f);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "asgard_valor", 3, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "asgard_valor", 8, AttributeModifier.Operation.ADDITION));
        
    }

    @Override
    public double getSpeed() {
        return 1.3;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        immuneSprint(player, target, 6, 6);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ornate_shield", 1));
        tooltips.add(CMLangUtil.getTranslatable("ornate_shield", 2));
        tooltips.add(CMLangUtil.getTranslatable("asgard_valor", 1));
        tooltips.add(CMLangUtil.getTranslatable("asgard_valor", 2));
        return tooltips;
    }
}
