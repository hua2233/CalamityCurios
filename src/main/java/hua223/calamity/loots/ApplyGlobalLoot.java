package hua223.calamity.loots;

import hua223.calamity.register.items.CalamityItems;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记战利品注入回调方法，由 {@link GlobalLoot} 框架在启动时通过 ASM 扫描自动发现，
 * 并经 {@link java.lang.invoke.LambdaMetafactory} 转换为 {@link GlobalLoot.LootCall} 注册到对应的战利品类型中。
 * <p>
 * 注解方法必须为 {@code public void anyName(? extends BaseLootContextPacker packer)} 修饰的实例方法，
 * 参数类型决定战利品上下文：
 * <ul>
 *     <li>{@link ChestLootContext} — 箱子战利品（对应 {@link GlobalLoot#CHESTS_LOOTS}）</li>
 *     <li>{@link EntitiesLootContext} — 实体掉落（对应 {@link GlobalLoot#ENTITY_LOOTS}）</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @ApplyGlobalLoot
 * public void onChestLoot(ChestLootContext context) {
 *     if (context.fromSpecificName("nether_bridge") && context.chance(0.3f))
 *         context.addLoot(this, context.getRandomCount(2, 7));
 * }
 * }</pre>
 *
 * @see GlobalLoot
 * @see BaseLootContextPacker
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ApplyGlobalLoot {
    /**
     * 声明是否仅由玩家触发，默认为 {@code true}。
     * <p>
     * 框架内部采用分段布局管理回调函数：
     * 未显式声明 {@code onlyPlayer} 的函数位于玩家条件段，仅在玩家触发时执行；
     * 显式声明 {@code onlyPlayer=false} 的函数位于通用段，所有触发源均可执行。
     * <p>
     * ASM 特性：Forge 扫描时不会将注解默认值存入 {@code annotationData}，
     * 因此 {@code containsKey("onlyPlayer")} 为 {@code true} 表示显式声明了非默认值。
     */
    boolean onlyPlayer() default true;

    /**
     * 对应的物品枚举常量，默认通过类名自动推导（UpperCamel → UPPER_UNDERSCORE），
     * 例如 {@code NightmareFuel} → {@link CalamityItems#NIGHTMARE_FUEL}。
     * 仅当类名与枚举常量不匹配时需要显式指定。
     */
    CalamityItems item() default CalamityItems.CALAMITY;
}

