package hua223.calamity.mixed;

import net.minecraft.world.entity.LivingEntity;

public interface ICalamityMagicExpand {
    boolean calamity$TryUseMagicItem(int level, int value, String name);

    boolean calamity$UsePotionMana(float consume, boolean sync);

    void calamity$TryUseEnchantedStarfish(LivingEntity entity, int value);

    void calamity$SetAutomaticUsePotion(boolean auto);

    float calamity$GetMana();

    void calamity$ChangeMana(float mana, boolean sync);

    boolean calamity$ConsumeMana(float mana);
}
