package hua223.calamity.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

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
 * 注意：这个类无法被序列化。它的本意就是为了处理快速更改的值。故此它只作用于临时属性
 */
public class VariableAttributeModifier extends AttributeModifier {
    private double modifiableValue;

    public VariableAttributeModifier(UUID id, String name, double amount, Operation operation) {
        super(id, name, 0, operation);
        modifiableValue = amount;
    }

    public static VariableAttributeModifier getModifierInInstance(AttributeInstance instance, UUID uuid) {
        AttributeModifier modifier = instance.getModifier(uuid);
        return (VariableAttributeModifier) modifier;
    }

    public static void updateModifierInInstance(AttributeInstance instance, UUID uuid, double value) {
        VariableAttributeModifier modifier = getModifierInInstance(instance, uuid) ;
        if (modifier != null) modifier.setValue(value, instance);
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

    public void addValue(double v) {
        modifiableValue += v;
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
