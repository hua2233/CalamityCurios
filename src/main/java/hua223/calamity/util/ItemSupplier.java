package hua223.calamity.util;

import hua223.calamity.mixins.PriestMixin;
import hua223.calamity.register.items.CalamityItems;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物品交易供应注解，标注于方法上，声明该物品在指定商人处的交易注册逻辑。
 * <p>
 * 支持两种注册模式：
 * <ul>
 *     <li>ISS商人模式：通过 {@link #ISSMerchant()} 指定 Iron's Spellbooks 的自定义商人类型</li>
 *     <li>原版村民模式：通过 {@link #villagerSupplier()} + {@link #villagerLevel()} 指定村民职业与交易等级</li>
 * </ul>
 * 注解在启动阶段由 {@code ITransaction.findTransactionList} 扫描处理，
 * 通过 LambdaMetafactory 将标注方法适配为函数式接口实例并写入交易表。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ItemSupplier {
    /**
     * ISS商人实体类型，指定交易注册到哪个 Iron's Spellbooks 商人。
     * <p>
     * 注解方法必须为 修饰符与参数限定为 pulic void anyName({@link #ISSMerchant()})
     * <p>
     * 默认值为 {@code PriestMixin.class} 表示未显式指定（走村民分支或抛出异常）。
     */
    @SuppressWarnings("ALl")
    Class<? extends IMerchantWizard> ISSMerchant() default PriestMixin.class;

    /**
     * 原版村民职业名称（如 {@code "armorer"}），为空字符串时表示不使用村民交易模式。
     * 关于村民职业:
     * @see hua223.calamity.generators.VillagerProfessionMap
     */
    String villagerSupplier() default "";

    /**
     * 村民交易解锁等级（1~5），仅在 {@link #villagerSupplier()} 非空时生效。
     */
    int villagerLevel() default 0;

    /**
     * 对应的物品枚举常量，默认通过类名自动推导（UpperCamel → UPPER_UNDERSCORE）。
     */
    CalamityItems item() default CalamityItems.CALAMITY;
}
