package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class VexationNecklace extends BaseCurio implements ICuriosStorage {
    public VexationNecklace(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.NATURE_SPELL_POWER.get(),
            new AttributeModifier(uuid, "vexation", .08f, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        CalamityHelp.addIfDoesNotExist(listener.entity, 80, 0, CalamityEffects.ACID_VENOM.get());
        listener.amplifier += getCount(listener.player)[2];
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        float[] count = memory.count;
        if (count[0]++ == 20) {
            count[0] = 0;
           float proportion = player.getHealth() / player.getMaxHealth();
           if (Math.abs(proportion - count[1]) > 0.01f) {
               count[1] = proportion;
               count[2] = Mth.lerp(proportion, 0.3f, 0f);
           }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("vexation", 1));
        LocalPlayer player = Minecraft.getInstance().player;
        tooltips.add(CMLangUtil.getDynamic("vexation", 2, Mth.lerp(player.getHealth() / player.getMaxHealth(), .3f, 0f)));
        tooltips.add(CMLangUtil.getTranslatable("vexation", 3));
        return tooltips;
    }
}
