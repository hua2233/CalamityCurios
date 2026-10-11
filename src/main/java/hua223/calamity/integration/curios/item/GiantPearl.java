package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "在沉船战利品箱中可能找到")
public class GiantPearl extends BaseCurio implements ICuriosStorage {
    public GiantPearl(Properties properties) {
        super(properties);
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
            List<LivingEntity> entities = CalamityHelp.getAttackableEntity(LivingEntity.class, player, player.getBoundingBox().inflate(5));
            if (entities.isEmpty()) return;

            for (LivingEntity entity : entities) {
                if (!entity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            }
        }
    }

    @ApplyGlobalLoot
    public void onChestLoot(ChestLootContext context) {
        if (context.fromFuzzyType("shipwreck") && context.chance(0.1f))
            context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("giant_pearl"));
        return tooltips;
    }
}
