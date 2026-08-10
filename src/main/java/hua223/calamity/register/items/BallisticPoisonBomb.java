package hua223.calamity.register.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ItemPro;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BallisticPoisonBomb extends SeafoamBomb {
    @Override
    protected int getAdhesionTime() {
        return 100;
    }

    @Override
    protected void explodeAndApplyEffect(ItemPro pro) {
        Entity owner = pro.getOwner();
        for (LivingEntity hurt : CalamityHelp.blastingTheEnemy(owner == null ? pro : owner, pro.position(), 5)) {
            hurt.invulnerableTime = 0;
            hurt.hurt(pro.level().damageSources().wither(), 5);
            CalamityHelp.addIfDoesNotExist(hurt, 140, 0, CalamityEffects.ACID_VENOM.get());
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? ImmutableMultimap.of(AttributeRegistry.NATURE_SPELL_POWER.get(), new AttributeModifier(
            "ballistic_poison_bomb", .1, AttributeModifier.Operation.MULTIPLY_BASE)) : ImmutableMultimap.of();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientKill(ItemPro pro) {
        Level level = pro.level();
        SimpleParticleType type = ParticleRegister.BALLISTIC_POISON_CLOUD.get();
        for (int i = 0; i < level.random.nextInt(4, 7); i++)
            level.addParticle(type, pro.getX(), pro.getY(), pro.getZ(), .05F - level.random.nextFloat() * .1F,
                level.random.nextFloat() * .1f, .05F - level.random.nextFloat() * .1F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("ballistic_poison_bomb"));
    }
}
