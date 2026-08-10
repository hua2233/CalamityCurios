package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.config.AutoConfig;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(InfectedJewel.class)
@JeiInfo(zh_cn = "击败中大型史莱姆时有几率掉落")
public class CrownJewel extends BaseCurio implements ICuriosStorage {
    @AutoConfig(template = CalamityConfig.ConfigTemplate.DROP)
    public static final float DROP_PROBABILITY = CalamityConfig.value(.5f);
    public CrownJewel(Properties properties) {
        super(properties);

    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "crown_jewel", 1, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 100))
            player.heal(CalamityHelp.getDebuffCount(player) > 0 ? 2.5f : 1f);
    }

    @ApplyGlobalLoot
    public void onDrop(EntitiesLootContext context) {
        Slime slime = context.verification(EntityType.SLIME);
        if (slime != null && slime.getSize() > 4 && context.chance(DROP_PROBABILITY))
            context.addLoot(this, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("crown_jewel"));
        return tooltips;
    }
}
