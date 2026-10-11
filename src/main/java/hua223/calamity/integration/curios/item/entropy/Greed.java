package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.FishingLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

@JeiInfo(zh_cn = "在深暗之域钓鱼可以获得，受幸运影响")
public class Greed extends Card {
    public Greed(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        final float amplifier = equipped.calamity$Player.Calamity$Player.cardDeck ? 0.03f : 0.02f;
        CuriosApi.getCuriosInventory(equipped).ifPresent(modifiable -> {
            double amount = 0;
            for (int i = 0; i < modifiable.getSlots(); i++) {
                IItemHandlerModifiable handler = modifiable.getEquippedCurios();
                for (int j = 0; j < handler.getSlots(); j++) {
                    if (!handler.getStackInSlot(j).isEmpty())
                        amount += amplifier;
                }
            }

            modifier.put(CalamityAttributes.DAMAGE_UP.get(), new AttributeModifier(uuid, "greed", amount, AttributeModifier.Operation.MULTIPLY_BASE));
        });
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @ApplyGlobalLoot
    public void onFishing(FishingLootContext context) {
        if (context.luckChance(.03f)  && context.matchBiome(Biomes.DEEP_DARK)) context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("greed", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("greed", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
