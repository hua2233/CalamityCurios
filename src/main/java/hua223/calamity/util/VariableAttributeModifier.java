package hua223.calamity.util;

import net.minecraft.server.level.ServerPlayer;
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
 */
public class VariableAttributeModifier extends AttributeModifier {
    private double modifiableValue;
    private boolean retain;

    public VariableAttributeModifier(UUID id, String name, double amount, Operation operation) {
        super(id, name, 0, operation);
        modifiableValue = amount;
    }

    public static VariableAttributeModifier createRetainVariable(UUID uuid, String name, double amount, Operation operation) {
        VariableAttributeModifier modifier = new VariableAttributeModifier(uuid, name, amount, operation);
        modifier.retain = true;
        return modifier;
    }

    @SuppressWarnings("ConstantConditions")
    public static void readOldValuesOfDeath(ServerPlayer _new, ServerPlayer old) {
        old.getAttributes().attributes.forEach((k, v) -> {
            AttributeInstance instance = _new.getAttribute(k);
            for (AttributeModifier modifier : v.permanentModifiers)
                if (modifier instanceof VariableAttributeModifier variable && variable.retain)
                    instance.addPermanentModifier(variable);
        });
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

    public void setValue(double v, AttributeInstance instance) {
        modifiableValue = v;
        instance.setDirty();
    }

    public void addValue(double v, AttributeInstance instance) {
        modifiableValue += v;
        instance.setDirty();
    }
}
