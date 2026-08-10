package hua223.calamity.register.items;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JeiInfo(zh_cn = "击杀凋零骷髅概率掉落少量，凋零必定大量掉落")
public class Necroplasm extends Item {
    public Necroplasm() {
        super(RegisterList.ITEM_RARE);
    }

    @ApplyGlobalLoot
    public void onDrop(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.WITHER)) context.addLoot(this, context.getRandomCount(15, 26));
        else if (context.onlyVerification(EntityType.WITHER_SKELETON) && context.chance(0.35f))
            context.addLoot(this, context.getRandomCount(1, 3));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag) {
        tooltips.add(CMLangUtil.getTranslatable("necroplasm").withStyle(ChatFormatting.AQUA));
    }
}
