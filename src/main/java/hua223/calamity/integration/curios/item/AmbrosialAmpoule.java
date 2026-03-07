package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
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

public class AmbrosialAmpoule extends BaseCurio implements ICuriosStorage {
    public AmbrosialAmpoule(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (!listener.effect.isBeneficial())
            listener.instance.calamity$SetDuration(listener.instance.getDuration() / 2);
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
        return 5;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] data = getCount(player);
        if (data[0]++ > 30) {
            data[0] = 0;


            if (CalamityHelp.hasDebuff(player)) {
                data[2] = 1;
            } else data[1] = 0;

            float missingHealthPercent = 1 - (player.getHealth() / player.getMaxHealth());
            data[3] = Math.min(5, Math.max(1, missingHealthPercent * 10));

            data[4] = (player.walkDistO == player.walkDist) ? 1.5f : 0;
        }

        if (data[1] > 100) {
            data[1] = 0;
            float totalHeal = 1;
            for (int i = 2; i < data.length; i++) {
                totalHeal += data[i];
            }

            player.heal(totalHeal);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ambrosial_ampoule", 1));
        tooltips.add(CMLangUtil.getTranslatable("ambrosial_ampoule", 2));
        tooltips.add(CMLangUtil.getTranslatable("ambrosial_ampoule", 3));
        tooltips.add(CMLangUtil.getTranslatable("ambrosial_ampoule", 4));
        return tooltips;
    }
}
