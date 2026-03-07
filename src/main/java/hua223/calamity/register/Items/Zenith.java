package hua223.calamity.register.Items;

import hua223.calamity.register.entity.projectiles.ZenithProjectile;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Zenith extends SwordItem {
    public Zenith(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this)) {
            player.getCooldowns().addCooldown(this, 4);
            if (!level.isClientSide) {
                LivingEntity entity = CalamityHelp.getLookedEntity(player, level, 50);

                if (entity == null) {
                    level.addFreshEntity(ZenithProjectile.of(level, player));
                } else {
                    if (entity instanceof LivingEntity) {
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 8));
                    }

                    Vec3 vec3 = entity.position();
                    level.addFreshEntity(ZenithProjectile.of(level, player, vec3));
                }
            }
            return InteractionResultHolder.success(itemStack);
        } else {
            return InteractionResultHolder.pass(itemStack);
        }
    }

//    @Override
//    public boolean onEntityItemUpdate(ItemStack stack, @NotNull ItemEntity entity) {
//        entity.setNoGravity(true);
//        entity.setGlowingTag(true);
//        return false;
//    }


    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        ItemEntity entity = (ItemEntity) location;
        entity.setNoGravity(true);
        entity.setGlowingTag(true);
        return null;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, @NotNull Enchantment enchantment) {
        return !enchantment.isCurse() && enchantment.category != EnchantmentCategory.BREAKABLE && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @Override
    public boolean canBeHurtBy(@NotNull DamageSource source) {
        return source.isBypassInvul();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        tooltip.add(Component.translatable(CMLangUtil.getCommonText("zenith"))
            .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
