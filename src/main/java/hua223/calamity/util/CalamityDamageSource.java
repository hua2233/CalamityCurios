package hua223.calamity.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CalamityDamageSource extends DamageSource {
    private ChatFormatting formatting;
    private boolean noDecay;
    private float realAmount;
    private boolean notTriggerEvent;
    private Entity owner;
    private Entity indirect;

    public CalamityDamageSource(String messageId) {
        super(messageId);
    }

    public boolean isNoDecay() {
        return noDecay;
    }

    public CalamityDamageSource setNotTriggerEvent() {
        notTriggerEvent = true;
        return this;
    }

    public static CalamityDamageSource getAbyss() {
        return (CalamityDamageSource) new CalamityDamageSource("calamity_curios.sink")
            .setStyle(ChatFormatting.BLUE).setNotTriggerEvent()
            .bypassArmor();
    }

    public static DamageSource mapToVisualizedSulfurFireSource() {
        return new CalamityDamageSource("calamity_curios.sulfur_fire")
            .setStyle(ChatFormatting.DARK_RED).setIsFire();
    }

    public static CalamityDamageSource getMagicProjectile(Projectile projectile) {
        return (CalamityDamageSource) new CalamityDamageSource("calamity_curios.magic_projectile")
            .setStyle(ChatFormatting.AQUA).setNotTriggerEvent().setOwnerAndIndirect(projectile.getOwner(), projectile)
            .bypassArmor().setProjectile().setMagic();
    }

    public static CalamityDamageSource getPlague() {
        return (CalamityDamageSource) new CalamityDamageSource("calamity_curios.plague")
            .setStyle(ChatFormatting.DARK_GREEN).setNotTriggerEvent()
            .bypassArmor().bypassEnchantments();
    }

    public static CalamityDamageSource getBloodGod() {
        return (CalamityDamageSource) new CalamityDamageSource("calamity_curios.blood_god")
            .setStyle(ChatFormatting.DARK_RED).setNotTriggerEvent()
            .bypassArmor().bypassInvul();
    }

    public static CalamityDamageSource getAstralInjection() {
        return (CalamityDamageSource) new CalamityDamageSource("calamity_curios.astral_injection")
            .setStyle(ChatFormatting.DARK_AQUA).setNotTriggerEvent()
            .bypassArmor().setMagic();
    }

    public boolean isNotTriggerEvent() {
        return notTriggerEvent;
    }

    public CalamityDamageSource setOwner(Entity entity) {
        owner = entity;
        return this;
    }

    public CalamityDamageSource setOwnerAndIndirect(Entity entity, Entity indirect) {
        setOwner(entity).indirect = indirect;
        return this;
    }

    public CalamityDamageSource setStyle(ChatFormatting formatting) {
        this.formatting = formatting;
        return this;
    }

    @Override
    public @Nullable Entity getEntity() {
        return owner;
    }

    @Override
    public @Nullable Entity getDirectEntity() {
        return indirect;
    }

    public CalamityDamageSource setNoDecay(float amount) {
        noDecay = true;
        realAmount = amount;
        return this;
    }

    @Override
    public @Nullable Vec3 getSourcePosition() {
        return owner == null ? null : owner.position();
    }

    public float getRealAmount() {
        if (noDecay) return realAmount;
        throw new IllegalStateException("Unable to obtain raw values from non attenuated source!");
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity entity) {
        MutableComponent component = (MutableComponent) super.getLocalizedDeathMessage(entity);
        return formatting == null ? component : component.withStyle(formatting);
    }
}
