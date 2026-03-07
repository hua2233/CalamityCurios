package hua223.calamity.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FriendlyEffectCloudBuilder {
    private boolean customColor = false;
    private ItemStack stack;
    private int waitTime = 10;
    @NotNull
    private final Entity entity;
    private Potion potion;
    private final int duration;
    private final float radius;
    private int color;
    private final AreaEffectCloud cloud;
    private MobEffectInstance[] effects;

    public FriendlyEffectCloudBuilder(@NotNull Entity entity, Vec3 pos, int duration, float radius) {
        this.entity = entity;
        this.duration = duration;
        this.radius = radius;
        cloud = new AreaEffectCloud(entity.level, pos.x, pos.y, pos.z);
        if (entity instanceof LivingEntity living)
            cloud.setOwner(living);
    }

    public FriendlyEffectCloudBuilder setItemStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public FriendlyEffectCloudBuilder setCustomColor(int color) {
        if (!customColor) {
            customColor = true;
            this.color = color;
        }

        return this;
    }

    public FriendlyEffectCloudBuilder setPotion(Potion potion) {
        this.potion = potion;
        return this;
    }

    public FriendlyEffectCloudBuilder setEffects(MobEffectInstance... instances) {
        this.effects = instances;
        return this;
    }

    public FriendlyEffectCloudBuilder setWaitTime(int waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    public void build() {
        cloud.setRadius(radius);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(waitTime);
        cloud.setDuration(duration);
        cloud.setRadiusPerTick(-cloud.getRadius() / 200f);

        if (customColor) cloud.setFixedColor(color);
        boolean immunity = false;
        if (effects != null)
            for (MobEffectInstance instance : effects) {
                cloud.addEffect(instance);
                immunity = true;
            }

        if (stack != null)
            for(MobEffectInstance effect : PotionUtils.getCustomEffects(stack)) {
                cloud.addEffect(new MobEffectInstance(effect));
                if (!immunity && !effect.getEffect().isBeneficial())
                    immunity = true;
            }

        if (potion != null) {
            cloud.setPotion(potion);
            for (MobEffectInstance instance : potion.getEffects())
                if (!immunity || !instance.getEffect().isBeneficial()) {
                    immunity = true;
                    break;
                }
        }

        //Set the owner as processed to deceive it and prevent it from having a negative impact on the owner
        //Why would I poison myself?
        if (immunity) cloud.victims.put(entity, duration + waitTime + 20);
        entity.level.addFreshEntity(cloud);
    }
}
