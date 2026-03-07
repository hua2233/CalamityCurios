package hua223.calamity.register.Items;

import com.google.common.collect.ImmutableMultimap;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.AtaraxiaHit;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Ataraxia extends SwordItem {
    public Ataraxia(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
            attackDamageModifier + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));

        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
            attackSpeedModifier, AttributeModifier.Operation.ADDITION));

        builder.put(ForgeMod.ATTACK_RANGE.get(), new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
            2d, AttributeModifier.Operation.ADDITION));

        defaultModifiers = builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (slotId < 9 && level.random.nextFloat() < 0.01f && entity instanceof LivingEntity living) {
            float health = living.getHealth();
            float max = living.getMaxHealth();
            if (health < max * 0.8F && !living.hasEffect(MobEffects.REGENERATION)) {
                float missingRatio = (max - health) / max;
                int quarters = Mth.floor(missingRatio * 4.0f);
                living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, quarters));
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player && player.calamity$TargetAtaraxiaHit()) {
            NetMessages.sendToClient(new AtaraxiaHit(target), player);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltipComponents, TooltipFlag advanced) {
        tooltipComponents.add(CMLangUtil.getTranslatable("ataraxia").withStyle(ChatFormatting.AQUA));
    }
}
