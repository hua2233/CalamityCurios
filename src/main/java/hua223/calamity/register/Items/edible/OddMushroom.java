package hua223.calamity.register.Items.edible;

import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OddMushroom extends Item implements IDataPackResponse {
    public OddMushroom(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(3)
            .effect(() -> new MobEffectInstance(CalamityEffects.TRIPPY.get(), 1200, 0), 1f)
            .build()));
        GlobalLoot.mountTo(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        RenderUtil.Shaders.startHallucinogenic();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @ApplyGlobalLoot
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        MushroomCow cow = context.verification(EntityType.MOOSHROOM);
        if (cow != null && cow.getVariant() == MushroomCow.MushroomType.BROWN && context.chance(0.35f))
            context.addLoot(this, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag isAdvanced) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.LIGHT_PURPLE, "odd_mushroom", 1, 2);
        tooltips.add(CMLangUtil.getTranslatable("odd_mushroom", 3).withStyle(ChatFormatting.RED));
    }
}
