package hua223.calamity.register.items;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.loots.EntitiesLootContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import static hua223.calamity.register.RegisterList.ITEM_COMMON;

@JeiInfo(line = 2, zh_cn = {"猪灵概率掉落，受抢夺影响", "堡垒遗迹的战利品宝箱可以找到"})
public class HavocEssence extends TooltipItem {
    public HavocEssence() {
        super(ITEM_COMMON, "havoc_essence");
    }

    @ApplyGlobalLoot
    public void onDrop(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.PIGLIN) && context.lootingChance(.12f))
            context.addLoot(new ItemStack(this, context.getRandomCount(1, 3)));
    }

    @ApplyGlobalLoot
    public void onChest(ChestLootContext context) {
        if (context.fromFuzzyType("bastion")) context.addLoot(this, context.fromSpecificName("bastion_treasure")
            ? context.random.nextInt(4, 8) : context.random.nextInt(2, 5));
    }
}
