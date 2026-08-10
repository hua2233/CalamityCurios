package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.render.screen.FlashScreenRenderer;
import hua223.calamity.util.*;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.EnderSlashParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE;

public class AntiVoid extends MagicSwordItem {
    @SuppressWarnings("removal")
    public AntiVoid(Properties properties) {
        super(RegisterList.GOD_EATER, 18, -2.1f,
            new SpellDataRegistryHolder[]{new SpellDataRegistryHolder(SpellRegistry.SCULK_TENTACLES_SPELL, 5)},
            Map.of(CalamityAttributes.DAMAGE_UP.get(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "AntiVoid", -0.9f, MULTIPLY_BASE),
                AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "AntiVoid", -.5f, MULTIPLY_BASE),
                AttributeRegistry.ELDRITCH_SPELL_POWER.get(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "AntiVoid", -0.75f, MULTIPLY_BASE)),
            properties);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker.calamity$IsPlayer) {
            if (target.getRandom().nextBoolean())
                CalamityHelp.addIfDoesNotExist(target, 40, 1, MobEffectRegistry.SLOWED.get());
            ItemCooldowns cooldowns = ((Player)attacker).getCooldowns();
            if (cooldowns.isOnCooldown(this)) cooldowns.calamity$ReduceCooldown(this, 20, (ServerPlayer) attacker);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!player.getCooldowns().isOnCooldown(this)) {
            LivingEntity entity = CalamityHelp.getSightDetectionEntityResult(player, player.level(), 16);
            if (entity != null) {
                Vec3 forward = entity.getForward().normalize().reverse();
                Vec3 endPos = entity.position().add(forward.scale(3));
                BlockPos state = new BlockPos((int) endPos.x, (int) endPos.y, (int) endPos.z);
                if (level.getBlockState(state).isAir() && level.getBlockState(state.above()).isAir()) {
                    if (level.isClientSide) {
                        new FlashScreenRenderer(12, 0.6f, 16733695);
                        player.swing(InteractionHand.MAIN_HAND);
                    } else {
                        ServerPlayer serverPlayer = (ServerPlayer) player;

                        Vec3 up = CalamityHelp.UNIT_Y;
                        if (forward.dot(up) > 0.999) up = CalamityHelp.UNIT_X;
                        Vec3 right = up.cross(forward);
                        Vec3 particlePos = entity.position().add(forward);
                        MagicManager.spawnParticles(level, new EnderSlashParticleOptions((float) forward.x, (float)forward.y, (float)forward.z,
                                (float)right.x, (float)right.y, (float)right.z, 1.0F),
                            particlePos.x, particlePos.y + 1.3, particlePos.z, 1, 0.0F, 0.0F, 0.0F, 0.0F, true);
                        serverPlayer.resetFallDistance();
                        serverPlayer.calamity$SetInvulnerableTime(60);
                        if (serverPlayer.isPassenger()) serverPlayer.dismountTo(endPos.x, endPos.y, endPos.z);
                        else serverPlayer.teleportTo(endPos.x, endPos.y, endPos.z);
                        serverPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, entity, EntityAnchorArgument.Anchor.EYES);
                        entity.hurt(level.damageSources().outOfBorder(), (float) (serverPlayer.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2));
                        player.getCooldowns().addCooldown(this, 600);
                    }

                    return InteractionResultHolder.success(player.getMainHandItem());
                }
            }
        }

        return InteractionResultHolder.fail(player.getMainHandItem());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        super.appendHoverText(stack, pLevel, tooltips, advanced);
        tooltips.add(CMLangUtil.blankLine());
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "anti_void", 1, 2);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("anti_void", 3).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
