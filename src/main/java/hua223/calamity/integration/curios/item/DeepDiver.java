package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = SprintCurio.class)
public class DeepDiver extends SprintCurio {
    public DeepDiver(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(ForgeMod.SWIM_SPEED.get(),
            new AttributeModifier(uuid, "deep_diver", 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    public int getTime() {
        return 3;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        target.hurt(DamageSource.playerAttack(player), 4);
        player.invulnerableTime += 6;
    }

    @Override
    public int getCooldownTime() {
        return 20;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("deep_diver", 1));
        tooltips.add(CMLangUtil.getTranslatable("deep_diver", 2));
        tooltips.add(CMLangUtil.getTranslatable("deep_diver", 3));
        return tooltips;
    }
}
