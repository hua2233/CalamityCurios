package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class GiantPearl extends BaseCurio implements ICuriosStorage {
    public GiantPearl(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.setGlowingTag(true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (!player.hasEffect(MobEffects.GLOWING)) player.setGlowingTag(false);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float tick = addCount(player, 0);

        if (tick % 5 == 0)
            CalamityLightBlock.placePlayerDynamicLightSource(player, 15);

        if (tick > 40) {
            zeroCount(player, 0);
            double x = player.getX(), y = player.getY(), z = player.getZ();
            AABB box = new AABB(x + 5, y + 5, z + 5, x - 5, y - 5, z - 5);
            List<LivingEntity> entities = player.level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity instanceof Enemy && entity.isAlive() && !entity.isAlliedTo(player));

            if (entities.isEmpty()) return;
            for (LivingEntity entity : entities) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("giant_pearl"));
        return tooltips;
    }
}
