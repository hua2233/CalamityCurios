package hua223.calamity.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
/**
 * 可变属性修饰符类，扩展自原版AttributeModifier
 * 允许动态修改属性修饰符的值而无需重新创建实例
 * <p>
 * 主要功能：
 * - 在原有固定值基础上添加可变值(modifiableValue)
 * - 支持直接设置或修改属性值
 * - 自动标记属性实例为dirty以确保重新计算
 * <p>
 */
public class VariableAttributeModifier extends AttributeModifier {
    private double modifiableValue;

    public VariableAttributeModifier(UUID id, String name, double amount, Operation operation) {
        super(id, name, 0, operation);
        modifiableValue = amount;
    }

    @SuppressWarnings("ConstantConditions")
    public static void readOldValuesOfDeath(ServerPlayer _new, ServerPlayer old) {
        old.getAttributes().attributes.forEach((k, v) -> {
            AttributeInstance instance = _new.getAttribute(k);
            for (AttributeModifier modifier : v.permanentModifiers)
                if (modifier instanceof VariableAttributeModifier)
                    instance.addPermanentModifier(modifier);
        });
    }

    public static void createOrIncrease(LivingEntity entity, Attribute attribute, UUID id, String name, double base, double max, Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            if (id == null) id = UUID.nameUUIDFromBytes(name.getBytes());
            AttributeModifier modifier = instance.getModifier(id);

            if (modifier == null) {
                instance.addPermanentModifier(new VariableAttributeModifier(id, name, base, operation));
            } else {
                VariableAttributeModifier var = (VariableAttributeModifier) instance.getModifier(id);
                if (var.getAmount() < max) var.setValue(var.getAmount() + base, instance);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void updateModifierInInstance(AttributeInstance instance, UUID uuid, double value) {
        ((VariableAttributeModifier) instance.getModifier(uuid)).setValue(value, instance);
    }

    public void setBatchValue(double v, AttributeInstance... instances) {
        modifiableValue = v;
        for (AttributeInstance instance : instances) instance.setDirty();
    }

    @Override
    public double getAmount() {
        return modifiableValue;
    }

    public void setValue(double value) {
        modifiableValue = value;
    }

    @Override
    public @NotNull CompoundTag save() {
        CompoundTag tag = super.save();
        tag.putBoolean("Variable", true);
        tag.putDouble("Amount", modifiableValue);
        return tag;
    }

    public void setValue(double v, AttributeInstance instance) {
        modifiableValue = v;
        instance.setDirty();
    }

    public void addValue(double v, AttributeInstance instance) {
        modifiableValue += v;
        instance.setDirty();
    }
}
