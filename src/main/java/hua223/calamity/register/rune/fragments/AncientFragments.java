package hua223.calamity.register.rune.fragments;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.rune.ArmorRune;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AncientFragments extends ArmorRune {
    public AncientFragments() {
        super(RegisterList.COMMON_ONE);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.calamity_curios.ancient_fragments";
    }

    @Override
    public final boolean canRemove() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    protected void preDisplayAttribute(List<Component> tooltips) {
        tooltips.add(CMLangUtil.blankLine());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("ancient_fragments").withStyle(ChatFormatting.DARK_RED));
    }
}
