package hua223.calamity.register.Items;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.ExcelsusHit;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ExProjectile;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Excelsus extends SwordItem {
    public Excelsus(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (usedHand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this)) {
            if (level.isClientSide) {
                player.swing(usedHand);
            } else {
                ExProjectile.create(player, level);
                stack.hurtAndBreak(20, player, entity -> {});
                player.getCooldowns().addCooldown(this, 40);
            }

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        MobEffect effect = CalamityEffects.GOD_SLAYER_INFERNO.get();
        if (!target.hasEffect(effect))
            target.addEffect(new MobEffectInstance(effect, 30, 0));

        if (attacker instanceof ServerPlayer player) {
            //Directly applies the original damage of the particle beam，Prioritize different targets around the attacked target
            LivingEntity hurt = CalamityHelp.getClosestTarget(target, 6, target.position());
            hurt = hurt == null ? target : hurt;
            hurt.hurt(DamageSource.MAGIC, 6);

            if (hurt.isAlive()) {
                NetMessages.sendToClient(new ExcelsusHit(hurt), player);
                if (hurt != target && !hurt.hasEffect(effect))
                    hurt.addEffect(new MobEffectInstance(effect, 40, 0));
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(CMLangUtil.getTranslatable("excelsus", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(CMLangUtil.getTranslatable("excelsus", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
