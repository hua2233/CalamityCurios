package hua223.calamity.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

public class ThrownItemPotion extends ThrownPotion {
    private final Item item;
    private boolean isLingering;
    private int color;
    private final MobEffectInstance[] effects;
    private final LivingEntity entity;
    public ThrownItemPotion(LivingEntity shooter, Item item, MobEffectInstance... instances) {
        super(shooter.level, shooter);
        this.item = item;
        entity = shooter;
        effects = instances;
    }

    @Override
    protected Item getDefaultItem() {
        return item;
    }

    public void setLingering(int color) {
        isLingering = true;
        this.color = color;
    }

    @Override
    protected boolean isLingering() {
        return isLingering;
    }

    @Override
    protected void makeAreaOfEffectCloud(ItemStack stack, Potion potion) {
        new FriendlyEffectCloudBuilder(entity, entity.position(), 200, 3f)
            .setItemStack(stack)
            .setEffects(effects)
            .setCustomColor(color)
            .build();
    }
}
