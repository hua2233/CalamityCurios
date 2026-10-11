package hua223.calamity.register.items;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.RegisterList;
import net.minecraft.world.entity.EntityType;

@JeiInfo(zh_cn = "在夜晚击杀流浪者掉落，受抢夺影响")
public class EleumEssence extends TooltipItem {
    public EleumEssence() {
        super(RegisterList.ITEM_COMMON, "eleum_essence");
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.STRAY) &&
            context.lootingChance(0.1f)) context.addLoot(this);
    }
}
