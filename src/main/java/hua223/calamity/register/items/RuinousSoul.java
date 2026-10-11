package hua223.calamity.register.items;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.RegisterList;

@JeiInfo(zh_cn = "末地城的战利品箱中可能找到")
public class RuinousSoul extends TooltipItem {
    public RuinousSoul() {
        super(RegisterList.ITEM_RARE, "ruinous_soul");
    }

    @ApplyGlobalLoot
    public void onChest(ChestLootContext context) {
        if (context.fromSpecificName("end_city_treasure") && context.chance(.12f))
            context.addLoot(this, context.getRandomCount(1, 6));
    }
}
