package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.FishingLootContext;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.UUID;

@JeiInfo(zh_cn = "在恶地钓鱼低概率获取")
public class UrsaSergeant extends BaseCurio implements ICuriosStorage {
    public UrsaSergeant(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onGetEffect(PostAttackListener listener) {
        CalamityHelp.addIfDoesNotExist(listener.entity, 0, 100, CalamityEffects.ASTRAL_INFECTION.get());
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "ursa_sergeant", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "ursa_sergeant", 10, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(uuid, "ursa_sergeant", 5, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20)) {
            float max = player.getMaxHealth();
            float health = player.getHealth();
            if (health < max * 0.7f) {
                int heal =  1;

                if (health < max * .15f)  heal = 3;
                else if (health < max * .25f) heal = 2;

                player.heal(heal);
            }
        }
    }

    @ApplyGlobalLoot
    public void onFishing(FishingLootContext context) {
        if (context.matchBiome(Tags.Biomes.IS_WASTELAND) && context.chance(0.08f))
            context.addLoot(this);
    }

    @Override

    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "ursa_sergeant", 1, 2);
        return tooltips;
    }
}
