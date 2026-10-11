package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.ProjectileSpawnListener;
import hua223.calamity.generators.VillagerProfessionMap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ItemSupplier;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@JeiInfo(zh_cn = "制箭师在大师级可能交易")
public class NihilityQuiver extends BaseCurio {
    public NihilityQuiver(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void arrowSpawn(ProjectileSpawnListener listener) {
        if (listener.isArrow) {
            listener.hurtAmplifier += 0.75;
            listener.speedVectorAmplifier += 1;
        }
    }

    @ItemSupplier(villagerSupplier = VillagerProfessionMap.FLETCHER, villagerLevel = 5)
    public MerchantOffer villagerTransaction(Entity entity, RandomSource source) {
        return source.nextFloat() > 0.35f ? null : new MerchantOffer(new ItemStack(ItemRegistry.AUTOLOADER_CROSSBOW.get()),
            new ItemStack(ItemRegistry.WEAPON_PARTS.get()), new ItemStack(this), 0, 1, .3f);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.FAR_CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "quiver", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("nihility_quiver", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("nihility_quiver", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
