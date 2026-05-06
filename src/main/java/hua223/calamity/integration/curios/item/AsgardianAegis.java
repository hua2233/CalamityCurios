package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = SprintCurio.class, isRoot = true)
public class AsgardianAegis extends AsgardValor {
    public AsgardianAegis(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onGetEffects(EffectListener listener) {
        listener.tryCancelHarmfulOnes(0.9f);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "asgardian_aegis", 4, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "asgardian_aegis", 10, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCooldownTime() {
        return 140;
    }

    @Override
    public double getSpeed() {
        return 1.4;
    }

    @Override
    public int getTime() {
        return 5;
    }

    @Override
    public void onSprinting(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RandomSource source = level.random;
        Vec3 position = player.position();
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, position.x, position.y, position.z,
            source.nextInt(2, 4), 0, 0, 0, (2.0 * source.nextDouble() - 1.0) * 0.3);
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        MobEffect effect = CalamityEffects.GOD_SLAYER_INFERNO.get();
        if (!target.hasEffect(effect)) target.addEffect(new MobEffectInstance(effect, 300, 0));
        immuneSprint(player, target, 10, 10);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.LIGHT_PURPLE, "asgardian_aegis", 1,2, 3, 4);
        return tooltips;
    }
}
