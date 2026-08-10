package hua223.calamity.register.damage;

import net.minecraft.ChatFormatting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface DamageRequester {
    /**
     * 获取所需求的DamageTypeID, 如果不存在且是数据生成该类型会自动生成
     * @return ID Of DamageType
     */
    String key();

    /**
     * 申请引用其他mod的伤害源
     * @return 该伤害源的mod id
     */
    String id() default "";

    /**
     * 注册时返回此DamageType需求的TagId
     * @return DamageTags Id List
     */
    String[] tags() default {};

    /**
     * 仅在引用已经存在的DamageType时，使用自定义的消息
     * @return msg ID
     */
    String msg() default "";

    /**
     * 仅在msg存在或DamageType注册的情况下，zh_cn会自动生成对应的中文翻译
     * @return 实际的死亡消息
     */
    String zh_cn() default "";

    /**
     * 修改此伤害源的颜色类型
     * @return 颜色类型
     */
    ChatFormatting style() default ChatFormatting.BLACK;
}
