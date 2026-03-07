package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.ChangedDimensionListener;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Flask extends BaseCurio implements ICuriosStorage {
    private final ResourceKey<Level> level;
    private final String type;
    private final MobEffect effect;

    public Flask(Properties properties, ResourceKey<Level> level, String type, MobEffect effect) {
        super(properties);
        this.level = level;
        this.type = type;
        this.effect = effect;
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;

        boolean a = equipped.level.dimension() == level;
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new VariableAttributeModifier(uuid, type, a ? 0.04 : 0, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, type, a ? 6 : 0, AttributeModifier.Operation.ADDITION));
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(effect);
    }

    @ApplyEvent
    public final void onDimensionChange(ChangedDimensionListener listener) {
        AttributeInstance i = listener.player.getAttribute(CalamityAttributes.INJURY_OFFSET.get());
        AttributeInstance a = listener.player.getAttribute(Attributes.ARMOR);
        boolean active = listener.to == level;
        UUID uuid = getUUID(listener.player)[0];
        VariableAttributeModifier.updateModifierInInstance(i, uuid, active ? 0.04 : 0);
        VariableAttributeModifier.updateModifierInInstance(a, uuid, active ? 6 : 0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable(type, 1));
        tooltips.add(CMLangUtil.getTranslatable(type, 2));
        return tooltips;
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    public boolean storageCount() {
        return false;
    }
}
