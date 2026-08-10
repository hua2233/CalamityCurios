package hua223.calamity.register.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ItemPro;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.IThrowableItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SeafoamBomb extends Item implements IThrowableItem {
    public SeafoamBomb() {
        super(RegisterList.UNCOMMON_ONE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide && !player.getCooldowns().isOnCooldown(this)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            ItemPro itemPro = of(stack, level);
            itemPro.setPos(player.getEyePosition());
            itemPro.setOwner(player);
            itemPro.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(itemPro);
            player.getCooldowns().addCooldown(this, 60);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    protected int getAdhesionTime() {
        return 60;
    }

    @Override
    public void onHitEntity(ItemPro itemPro, EntityHitResult result) {
        if (!itemPro.level().isClientSide) explodeAndApplyEffect(itemPro);
    }

    @Override//(-48.43534958895708, -58.224252399579136, -15.811056730257889)
    public void onHitBlock(ItemPro itemPro, BlockHitResult result) {
        itemPro.getPersistentData().putInt("adhesion", getAdhesionTime());
        Vec3 back = result.getLocation().subtract(itemPro.getX(), itemPro.getY(), itemPro.getZ()).normalize().scale(.8);
        itemPro.setPosRaw(itemPro.getX() - back.x, itemPro.getY() - back.y, itemPro.getZ() - back.z);
        itemPro.setOnGround(true);
    }

    @Override
    public boolean destroyAfterHitting(ItemPro itemPro) {
        return !itemPro.getPersistentData().contains("adhesion");
    }

    @Override
    public boolean customTick(ItemPro pro) {
        CompoundTag tag = pro.getPersistentData();
        if (tag.contains("adhesion")) {
            //Stop Move
            int tick = tag.getInt("adhesion") - 1;
            if (!pro.level().isClientSide && (tick == 0 || (tick & 5) == 0 && !pro.level().
                getEntitiesOfClass(LivingEntity.class, pro.getBoundingBox().inflate(3)).isEmpty())) {
                explodeAndApplyEffect(pro);
                pro.discard();
            }

            tag.putInt("adhesion", tick);
            return true;
        }

        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? ImmutableMultimap.of(AttributeRegistry.ICE_SPELL_POWER.get(), new AttributeModifier(
            "seafoam_bomb", .05, AttributeModifier.Operation.MULTIPLY_BASE)) : ImmutableMultimap.of();
    }

    protected void explodeAndApplyEffect(ItemPro pro) {
        Entity owner = pro.getOwner();
        for (LivingEntity hurt : CalamityHelp.blastingTheEnemy(owner == null ? pro : owner, pro.position(), 3)) {
            hurt.invulnerableTime = 0;
            hurt.hurt(pro.level().damageSources().drown(), 3);
            CalamityHelp.addIfDoesNotExist(hurt, 100, 0, CalamityEffects.CRUSH_DEPTH.get());
        }
    }

    @Override
    public float[] box() {
        return new float[] {.75f, .75f};
    }

    @Override
    public @NotNull Component getProName() {
        return Component.translatable(getDescriptionId());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float[] scale() {
        return new float[] {1.25f, 1.25f, 1.25f};
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("seafoam_bomb"));
    }
}
