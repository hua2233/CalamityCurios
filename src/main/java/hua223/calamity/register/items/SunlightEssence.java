package hua223.calamity.register.items;

import hua223.calamity.generators.VillagerProfessionMap;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ItemSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JeiInfo(line = 2, zh_cn = {"在白天击杀尸壳小概率掉落", "牧师在老手级别可以被交易"})
public class SunlightEssence extends Item {
    public SunlightEssence(Properties properties) {
        super(properties);
    }

    @ItemSupplier(villagerSupplier = VillagerProfessionMap.CLERIC, villagerLevel = 3)
    public MerchantOffer villagerTransaction(Entity entity, RandomSource source) {
        return source.nextFloat() > 0.4f ? new MerchantOffer(new ItemStack(Items.ROTTEN_FLESH, 6),
            new ItemStack(Items.EMERALD), new ItemStack(this), 12, 3, .2f) : null;
    }

    @ApplyGlobalLoot
    public void onDrop(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.HUSK) && context.entity.level().isDay() && context.chance(.1f))
            context.addLoot(new ItemStack(this));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("sunlight_essence").withStyle(ChatFormatting.GOLD));
    }
}
