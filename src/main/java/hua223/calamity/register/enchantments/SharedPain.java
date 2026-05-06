package hua223.calamity.register.enchantments;

import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.DamageTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SharedPain extends Enchantment {
    public SharedPain() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public void doPostAttack(@NotNull LivingEntity attacker, @NotNull Entity target, int level) {
        if (!target.isAlive() && attacker.calamity$IsPlayer && target instanceof LivingEntity entity) {
            ItemStack stack = attacker.getMainHandItem();
            if (!attacker.calamity$Player.getCooldowns().isOnCooldown(stack.getItem())) {
                Level world = entity.level();
                List<Mob> entities = world.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(3));
                if (!entities.isEmpty()) {
                    DamageSource source = null;
                    float damageValue = 0f;
                    for (LivingEntity affect : entities) {
                        if (affect.isAlive() && !affect.calamity$IsPlayer && !affect.isAlliedTo(attacker)) {
                            if (source == null) {
                                attacker.calamity$Player.getCooldowns().addCooldown(stack.getItem(), 100);
                                source = CalamityDamageSource.source(DamageTypes.GENERIC_KILL, world).addDamageTag(DamageTags.NO_DECAY.tag);
                                damageValue = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (level * 0.3f);
                            }

                            affect.hurt(source, damageValue);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return canEnchant(stack) && EnchantmentHelper.getTagEnchantmentLevel(this, stack) == 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getFullname(int level) {
        MutableComponent component = Component.translatable(getDescriptionId());
        if (level != 1 || getMaxLevel() != 1) component.append(CommonComponents.SPACE)
            .append(Component.translatable("enchantment.level." + level));

        return component.withStyle(ChatFormatting.GOLD);
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack stack) {
        return stack.is(CalamityItems.STARLESS_NIGHT.get()) || stack.is(CalamityItems.XYTHERON.get());
    }
}
