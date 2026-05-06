package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class ManaPolarizer extends BaseCurio implements ICuriosStorage {
    public ManaPolarizer(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.isSpell) return;

        float[] count = getCount(listener.player);
        if (count[1] > 0) {
            listener.player.heal((listener.baseAmount / 0.2f) * count[1]);
            count[1] -= 0.1f;
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(uuid, "mana_polarizer", 50, AttributeModifier.Operation.ADDITION));
        modifier.put(AttributeRegistry.SPELL_POWER.get(),
            new VariableAttributeModifier(uuid, "mana_polarizer", 0.15f, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0] % 30 == 0 && count[1] < 1f)
            count[1] += 0.1f;

        if (count[0]++ == 60) {
            count[0] = 0;

            double manaRatio = MagicData.getPlayerMagicData(player).getMana()
                / player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
            float heal = (float) (-2 + (4 * (1 - manaRatio)));
            if (heal > 0) player.heal(heal);
            else player.hurt(CalamityDamageSource.source(CalamityDamageTypes.POLARIZER_HURT, player.level()), Math.abs(heal));

            VariableAttributeModifier.updateModifierInInstance(player.getAttribute(AttributeRegistry.SPELL_POWER.get()),
                getFirstUUID(player), 0.15 - (0.10 * manaRatio));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "mana_polarizer", 1, 2, 3, 4);
        return tooltips;
    }
}
