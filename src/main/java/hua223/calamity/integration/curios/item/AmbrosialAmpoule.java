package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = Radiance.class, node = AmbrosialAmpoule.class)
public class AmbrosialAmpoule extends BaseCurio implements ICuriosStorage {
    public AmbrosialAmpoule(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (!listener.effect.isBeneficial())
            listener.setEffectDuration(listener.instance.getDuration() / 2);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "ambrosial_ampoule", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "ambrosial_ampoule", 6, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] data = getCount(player);
        if (data[0]++ == 100) {
            data[0] = 0;
            float heal = 1 + CalamityHelp.getDebuffCount(player);
            float maxHealth = player.getMaxHealth();
            float health = player.getHealth();
            if (health < maxHealth) {
                heal += Math.max(1, (1f - health / (maxHealth * 0.9F)) * 5);
                player.heal(heal + player.walkDistO == player.walkDist ? 1.5f : 0);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "ambrosial_ampoule", 1, 2, 3, 4);
        return tooltips;
    }
}
